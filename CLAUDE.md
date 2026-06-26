# Project: ATM10 Wiki / Helper-Mod design

This repo holds a builder's wiki for the All The Mods 10 (ATM10) modpack (`docs/`),
intended as reference for building an ATM10 quality-of-life helper add-on.

Owner: Noah (noahseslar@gmail.com).

## Standing authorization: PRs & merges at Claude's discretion

Noah has granted standing permission for Claude to **open pull requests and merge them on its
own judgment** — no need to ask first each time. This overrides the default "don't create a PR
unless explicitly asked" behavior **for this repository**.

Operate by this policy:

**Open a PR** when a change is a meaningful, self-contained unit of work (a feature, a doc
set, a fix). Always work on a branch — never commit directly to `main`. Write a clear PR
title/body describing what and why.

**Merge a PR yourself** when ALL of these hold:
- It's a PR you authored on a feature branch.
- The change is low-risk and self-contained (docs, content, additive/isolated code).
- Any applicable checks/CI pass and the work is verified (built/tested where relevant).
- Nothing about it is destructive or surprising.

Prefer **squash merge**, and delete the feature branch after merging.

**Hold for Noah's review instead of merging** when:
- The change is large, architectural, or hard to reverse.
- It deletes or overwrites existing user content, touches secrets/CI/permissions, or changes
  data/DB schema.
- You're genuinely uncertain whether it's what Noah wants.
- Noah has asked to review that particular piece of work.

When in doubt, open the PR and **leave it for review** rather than merging — opening is always
safe; merging is the irreversible step. Noah can always say "just merge stuff like this from
now on" to widen the latitude, or "stop auto-merging" to revoke it.

## Conventions

- Develop on feature branches; the current working branch is `claude/repo-cleanup-rename-zorby9`.
- Recipe/data facts in the wiki are version-sensitive — keep the "verify in JEI" flags intact.
