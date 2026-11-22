# Testing

Every suite with its command and where it runs, the testing deviations, and
the gates that only the owner of the project can clear. Commands run from the
repository root.

## Automated suites

| Suite | Command | Runs where | What it covers |
|---|---|---|---|
| Everything below | `./gradlew check assembleDebug` | Mac and CI | The whole automated gate |
| Domain unit tests | `./gradlew :domain:test` | Mac and CI, JVM only | Domain rules; the import boundary that keeps Android out of `domain` |
| Typography scanner tests | `./gradlew :typography-check:test` | Mac and CI, JVM only | Each typography rule and exemption, against planted violations |
| Typography check | `./gradlew :typography-check:checkTypography` | Mac and CI | Every tracked and untracked file except the explicit list in `config/typography-excluded-paths.txt` |
| ktlint | `./gradlew ktlintCheck` | Mac and CI | Kotlin sources and build scripts, official Kotlin style |
| Android lint | `./gradlew :app:lintDebug :domain:lint` | Mac and CI | App and domain, warnings as errors |
| Release signing guard | `./gradlew :app:assembleRelease` with the variables unset | Mac | Fails before compiling, naming each missing input |

CI is `.github/workflows/ci.yml`; it runs `./gradlew check assembleDebug` on
JDK 25 on every push and pull request.

## Proof that each check can fail

Datum 0.4 requires every check to be seen failing on a planted violation
before it is trusted.

| Check | Seen failing |
|---|---|
| Domain import boundary | Planted `android` and `androidx` imports in temporary source trees, in its own tests |
| Typography scanner | Twelve planted cases in its own tests; on first run it also caught real violations in new files |
| Typography check task | A planted untracked file with an em dash and a hyphen run, 22 November 2026, then deleted |
| ktlint | Reported real formatting violations in existing sources on first run, 22 November 2026, before they were fixed |
| Android lint | Reported three real issues on first run, 22 November 2026, before they were fixed |
| Release signing guard | Run without the variables, 22 November 2026 |

## Testing deviations

- Memory and undefined behaviour analysis does not apply: Kotlin on the JVM is
  memory safe and has no sanitiser (CWD20).
- Fuzzing uses a seeded mutation test over recorded fixtures in plain JUnit
  instead of a fuzzing engine (CWD20). It arrives with the parser in CWE5.

## Gates belonging to the owner

These cannot be cleared by an automated run or by the agent (datum 0.17,
items 19 and 20). Results are recorded in docs/HARDWARE_COMPATIBILITY.md, where only
the owner adds a row saying a gate passed.

- Every Done when check that needs a phone, judged on the Pixel 10 Pro.
- Appearance, legibility and TalkBack judgements.
- The 24 hour unattended refresh soak.
- Installing a release build over a previous release build without losing
  settings, which needs the signing passwords.
- Every Xiaomi 11T Pro check. None is available before delivery (CWD30); real
  use is recorded in docs/FIELD_NOTES.md.
