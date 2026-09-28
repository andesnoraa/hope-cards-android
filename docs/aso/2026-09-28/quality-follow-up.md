# Product-quality follow-up from ASO monitoring

Observed September 28, 2026 at 05:56:38.192 UTC in Play Console Monitor and improve for `com.aaronsedna.hopecards`.

Console displayed two memory recommendations explicitly attached to **36 (1.0.9)**:

1. BitmapFactory usage without downsampling; consider decoding only the resolution needed for the UI.
2. R8 configuration could cause higher memory usage and lower performance.

These are verified Console recommendations, not reproduced defects, measured crash/ANR changes or conclusions about version 40. The overview's last-28-day crash, ANR and slow-cold-start rates were unavailable. No policy issues were shown.

Defer investigation to the existing app release workflow. Compare current image decoding and R8 configuration with the exact flagged artifact, reproduce any suspected issue on an emulator, and change code only with evidence. Preserve signing, application ID, SDK/dependency checks and all existing release gates. Do not interrupt version 40's active review for these recommendations. This ASO heartbeat made no app changes and published no binary.
