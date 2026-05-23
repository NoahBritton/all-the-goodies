#!/usr/bin/env bash
# One-shot install: petition-tracker API on a Debian/Ubuntu VPS.
#
# Run as root on a fresh box:
#   curl -fsSL https://raw.githubusercontent.com/NoahBritton/petition-tracker/main/deploy/install.sh | sudo bash
# or:
#   sudo bash deploy/install.sh
#
# Prereqs:
#  - DNS A record: petition-api.axogames.dev -> this server's public IP
#  - Ports 80 + 443 open in your firewall / cloud security group
#  - Run as root (or pass DOMAIN= / EMAIL= env vars; see below)

set -euo pipefail

DOMAIN="${DOMAIN:-petition-api.axogames.dev}"
EMAIL="${EMAIL:-noahseslar@gmail.com}"
REPO_URL="${REPO_URL:-https://github.com/NoahBritton/petition-tracker.git}"
APP_USER="${APP_USER:-petition}"
APP_DIR="${APP_DIR:-/opt/petition-tracker}"
NGINX_SITE="/etc/nginx/sites-available/petition-api.conf"
NGINX_LINK="/etc/nginx/sites-enabled/petition-api.conf"

log() { printf '\033[1;36m[install]\033[0m %s\n' "$*"; }
require_root() { [[ $EUID -eq 0 ]] || { echo "Run as root (sudo bash $0)"; exit 1; }; }

require_root

log "Updating apt + installing system deps"
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq \
  python3 python3-venv python3-pip git nginx certbot python3-certbot-nginx ufw curl ca-certificates

log "Creating service user $APP_USER if missing"
if ! id "$APP_USER" >/dev/null 2>&1; then
  useradd --system --create-home --shell /usr/sbin/nologin "$APP_USER"
fi

log "Cloning / updating repo into $APP_DIR"
if [[ -d "$APP_DIR/.git" ]]; then
  git -C "$APP_DIR" fetch --quiet origin main
  git -C "$APP_DIR" reset --hard origin/main
else
  rm -rf "$APP_DIR"
  git clone --depth 1 "$REPO_URL" "$APP_DIR"
fi
chown -R "$APP_USER:$APP_USER" "$APP_DIR"

log "Creating Python venv and installing requirements"
if [[ ! -d "$APP_DIR/.venv" ]]; then
  sudo -u "$APP_USER" python3 -m venv "$APP_DIR/.venv"
fi
sudo -u "$APP_USER" "$APP_DIR/.venv/bin/pip" install --quiet --upgrade pip
sudo -u "$APP_USER" "$APP_DIR/.venv/bin/pip" install --quiet -r "$APP_DIR/requirements.txt"

log "Installing systemd unit"
install -m 0644 "$APP_DIR/deploy/petition-tracker.service" /etc/systemd/system/petition-tracker.service
systemctl daemon-reload
systemctl enable petition-tracker
systemctl restart petition-tracker
sleep 2
if ! systemctl is-active --quiet petition-tracker; then
  echo "petition-tracker service failed to start. Showing recent logs:"
  journalctl -u petition-tracker --no-pager -n 40
  exit 1
fi

log "Installing nginx vhost"
install -m 0644 "$APP_DIR/deploy/nginx.conf" "$NGINX_SITE"
ln -sf "$NGINX_SITE" "$NGINX_LINK"
mkdir -p /var/www/letsencrypt

# Temporarily disable the HTTPS server block so the initial HTTP-only check passes
# before we have a cert. We do this by writing a stub HTTP-only config.
log "Writing HTTP-only stub for initial certbot run"
cat >"$NGINX_SITE" <<EOF
server {
    listen 80;
    listen [::]:80;
    server_name $DOMAIN;
    location /.well-known/acme-challenge/ { root /var/www/letsencrypt; }
    location / { return 200 "ok"; }
}
EOF
nginx -t
systemctl reload nginx

log "Requesting Let's Encrypt cert for $DOMAIN"
certbot certonly --non-interactive --agree-tos --email "$EMAIL" \
  --webroot -w /var/www/letsencrypt -d "$DOMAIN" \
  || { echo "certbot failed. Check DNS for $DOMAIN -> this server's IP."; exit 1; }

log "Swapping in the full HTTPS nginx config"
install -m 0644 "$APP_DIR/deploy/nginx.conf" "$NGINX_SITE"
nginx -t
systemctl reload nginx

log "Allowing 80/443/22 through ufw (no-op if ufw inactive)"
if ufw status | grep -q "Status: active"; then
  ufw allow 80/tcp || true
  ufw allow 443/tcp || true
fi

log "Enabling certbot auto-renew timer"
systemctl enable --now certbot.timer || true

log "Smoke test:  curl https://$DOMAIN/healthz"
sleep 2
if curl -fsS "https://$DOMAIN/healthz" | grep -q '"ok":true'; then
  log "DONE — API is live at https://$DOMAIN/api/state.json"
else
  echo "Healthz didn't return ok. Check:  journalctl -u petition-tracker -f"
  exit 1
fi
