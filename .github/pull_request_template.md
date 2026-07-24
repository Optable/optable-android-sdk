## Why

<!-- What problem does this solve? Link Slack/Sentry/ClickUp/issue if relevant -->

## What Changed

<!-- Bullet points: [component]: change + reason if non-obvious.
     Components: android_sdk (public API / core / network / edge), DemoAppJava, DemoAppKotlin, CI, docs -->

-

## API Surface

<!-- Does this touch the public SDK API (OptableSDK, targeting/identify/witness, config)? -->

- [ ] No public API change
- [ ] Public API changed — README.md updated to match
- [ ] Backwards compatible with the currently released version
- [ ] Deprecations added instead of removals (where possible)

## How to Test

<!-- How to QA/Test this PR. Note which demo app you exercised it in. -->

- [ ] `./gradlew test` passes locally
- [ ] Ran DemoAppJava against a real host/slot and verified behavior
- [ ] Ran DemoAppKotlin against a real host/slot and verified behavior
- [ ] Tested

<!-- Screenshot or recording from DemoApp/ if behavior is visible -->

## Compatibility

<!-- Callers on older Android versions / consumers pinned to older SDK versions -->

- [ ] minSdk / target API unchanged
- [ ] No new dependency added (or new dep is justified below and pulls no surprising transitive deps)
- [ ] GAID / consent / privacy behavior unchanged

## Notes

<!-- Tradeoffs, edge cases, migration steps, follow-ups -->

- [ ] Breaking change <!-- consumers must change code to upgrade -->
- [ ] Requires release <!-- a version bump + `vX.Y.Z` tag should be published after merge (triggers CI publish) -->
