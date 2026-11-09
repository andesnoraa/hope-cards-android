# Quality observation, October 9

Google Play reports no policy issues. The last-28-day crash, ANR and slow-cold-start rates are unavailable, not zero. No affected-user issue rows are displayed for release 47. This is insufficient to assert defect-free operation.

The existing R8 recommendation is now attached to 1.0.14 (47): optimized resource shrinking is not enabled and Google recommends Android Gradle plugin 9.0 or higher. This is a configuration advisory, not an observed memory leak, battery-drain measurement or confirmed regression. See `quality-r8-advisory.txt`. Defer any implementation to the existing release gates: SDK/dependency audit, version increment, applicable regression tests/lint, signed local artifact and emulator validation. No binary or code changes were made in this monitoring run.

Google warns that some seven-day device-retention data is unavailable. The displayed one retained device on September 17 is a count, not a retention percentage.
