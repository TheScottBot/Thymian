# Implementation deviations

Every departure from the engineering datum (version 2), from the
specification, or from a contract: the rule, what is done instead, why, the
decision that allowed it, and the covering test. Newest last.

## D1. XML comment delimiters exempt from the hyphen run rule

- **Rule:** datum 0.8 bans double and triple hyphen runs, exempting only Markdown table separator rows and command line flag tokens.
- **Instead:** the typography check also exempts the XML comment opener and closer.
- **Why:** Android layout and resource files can only carry comments with that syntax, and datum 0.6 requires comments that explain why. A hyphen run inside a comment is still reported.
- **Decision:** CWD17 (datum typography scope) adopted 22 November 2026; this exemption is recorded here for confirmation.
- **Covering test:** `TypographyScannerTest`, `xml comment delimiters are exempt but a hyphen run inside the comment is not`.

## D2. Unknown fields in Open-Meteo responses are tolerated

- **Rule:** datum 0.10 and 0.11: reject unknown fields.
- **Instead:** responses over 64 KiB are refused before parsing; the fields used are required and range checked; unknown fields are tolerated and counted in the log.
- **Why:** Open-Meteo is a third party contract that already returns metadata Thymian does not use and may add more; rejecting them would blank the weather until a new build is sideloaded.
- **Decision:** CWD19, 22 November 2026.
- **Covering test:** arrives with the parser in CWE5.

## D3. No sanitiser or fuzzing engine

- **Rule:** datum 0.10: memory and undefined behaviour analysis in CI; guidance for a fuzz harness.
- **Instead:** memory analysis is recorded as not applicable; a seeded mutation test over recorded fixtures in plain JUnit runs on every push.
- **Why:** Kotlin on the JVM is memory safe and has no sanitiser; a fuzzing engine would add a dependency for a small parser.
- **Decision:** CWD20, 22 November 2026.
- **Covering test:** arrives with the parser in CWE5.

## D4. Spikes not deleted before CWE1 began

- **Rule:** the specification's Plan stage allows spikes only if deleted or fenced off; `spikes/README.md` promised deletion before CWE1.
- **Instead:** `spikes/` stays, fenced and excluded from ktlint, until the Pixel soak is read after 23 November 2026 12:17, then is deleted in one change.
- **Why:** deleting the soak spike would stop the 24 hour background refresh measurement (CWP7) that is still running on the Pixel.
- **Decision:** stated when CWE1 began, 22 November 2026.
- **Covering test:** none; the deletion itself closes this entry.

## D5. App icon added in CWE1 instead of CWE7

- **Rule:** the specification places the app icon in CWE7.
- **Instead:** the Thyme icon (CWD33) is added in CWE1.
- **Why:** Android lint, with warnings as errors, fails the build without an application icon. Suppressing the check until CWE7 would have been churn.
- **Decision:** made during CWE1 batch 3, 22 November 2026.
- **Covering test:** `./gradlew :app:lintDebug` (MissingApplicationIcon).

## D6. Lint checks for newer dependency versions disabled

- **Rule:** datum 0.10: the strictest lint setting, warnings as errors.
- **Instead:** `AndroidGradlePluginVersion`, `GradleDependency` and `NewerVersionAvailable` are disabled in `app/build.gradle.kts`.
- **Why:** they compare against whatever was published today, so an unchanged commit would pass one day and fail the next, breaking the datum's requirement that automated criteria reproduce on a clean machine. Versions are pinned in the catalogue and reviewed deliberately.
- **Decision:** made during CWE1 batch 3, 22 November 2026.
- **Covering test:** none needed; every other lint check remains an error.

## D7. Domain refresh constants introduced in CWE1

- **Rule:** the specification places the refresh policy in CWE6.
- **Instead:** `RefreshPolicy` (30 minute interval, 60 minute staleness threshold) is added in CWE1.
- **Why:** lint on the domain module fails when it has no compiled classes; this was the smallest real domain value the specification defines. The staleness decision itself still arrives in CWE6.
- **Decision:** made during CWE1 batch 3, 22 November 2026.
- **Covering test:** `RefreshPolicyTest`.

## D8. Device checks and the Xiaomi

- **Rule:** specification Part 4: every Done when check runs on both phones, recorded for each.
- **Instead:** device checks are gates for the owner; the Pixel and an API 34 emulator are used; the Xiaomi is not available before delivery and real use is recorded in FIELD_NOTES.md.
- **Why:** datum 0.17 items 19 and 20, and the Xiaomi being with its user in Germany, who will not run test builds.
- **Decision:** CWD18 and CWD30, 22 November 2026.
- **Covering test:** none; HARDWARE_COMPATIBILITY.md records what ran where.

## D9. Build, install and release steps live outside the README

- **Rule:** specification CWE7: README sections covering how to build, sign and install over USB, and how to get the debug APK from the workflow.
- **Instead:** those steps are in `docs/BUILDING.md`, linked from a lean README.
- **Why:** directed on 22 November 2026 that the README stays lean, with anything further in its own linked document.
- **Decision:** that direction, 22 November 2026.
- **Covering test:** none; CWE7's check that the steps work from a clean checkout follows `docs/BUILDING.md`.
