# exense-commons

This project contains functionality reused across a variety of applications built internally at exense GmbH and is open
for external collaboration purposes.

## Instructions for developers

### Code formatting (indentation and line breaks)

Use an IDE that supports the `.editorconfig` standard and make sure that it is enabled. This will automatically
apply the recommended settings to all files.

### Git blame history

To ensure correct handling of "reformat-only" commits, use a recent git version (>= 2.23) with the following
configuration. You can do this per-repository (in that case, omit the `--global` flag), but the filename used
is a de-facto standard, and this way you'll only need to do this once, not for every repository:

```
git config --global blame.ignoreRevsFile .git-blame-ignore-revs
```
