# Quiz review and PDF consistency

The answer review and shared PDF now consume the same immutable, localized `QuizReviewContent` snapshot. Both show a navy and gold score summary, numbered questions, outcome pills, explicit selected and correct answers, an explanation label when present, and a separate Bible-reference panel. The original session order, answer choices, selection and score remain unchanged. Unanswered questions have a neutral label and still show the correct answer.

Native review retains the selected Hope theme, Poppins/Noto Sans Malayalam, scalable text and semantic question headings. At the user's further request, questions now use the actual Bold (700) font in both native review and PDF; answer emphasis remains SemiBold (600). Correct, incorrect and unanswered states have explicit text as well as color. Their ink/background contrast ratios are 6.26:1, 6.57:1 and 5.44:1 respectively. The shared PDF uses the established print palette. The Share action is also available at the bottom of the review.

PDF cards that fit on a page stay together. A larger card may continue on another page, but each ordinary answer panel moves intact when it fits on a clean page. Truly oversized paragraphs remain line-paginated without truncation or repeated lines. No share promotion, install link or recipient information is inserted into PDF pages.

## Verification

- Debug app and test APK build: passed. Release configuration and version were not changed.
- JVM unit tests: 82 passed.
- Debug lint: zero errors, 51 pre-existing warnings. Warning-ID counts match the v40 baseline.
- PDF/shared-content instrumentation: 9 passed. Covers seven languages, A4 portrait/landscape and A6, cancellation, readable shared attachments, original answer order, skipped questions, and oversized content.
- All four rendered pages of English and Malayalam sample PDFs were inspected. No clipped text, missing glyphs, overlap or footer collision was found.
- Initial native checks passed Malayalam at normal and 200% font size. English and Midnight checks exposed a measured text-width mismatch: a Text node used 851px while its paragraph retained 860px. The displayed glyphs ended at 851px. Explicit full-width text columns correct the mismatch; the strict overflow assertion remains unchanged.
- The coordinated version-41 audit passed all four final native checks after both the width fix and Bold font change: English, Malayalam, Malayalam at 200% font scale, and Midnight theme. Strict no-overflow assertions were retained. Both shared-content parity tests also passed.
- All seven final PDF tests passed after updating the oversized-content test's explicit font expectation from SemiBold to the requested Bold. Pagination and line-preservation assertions remain unchanged. All four regenerated English/Malayalam PDF pages were visually inspected again with no clipped text, missing glyphs, overlap or footer collision.

Final build evidence and SHA256s are in `review-pdf-parity-2026-09-28.json`. Local full logs, PDFs and rendered previews are under `output/quiz-review-2026-09-28/` in the isolated `codex/quiz-review-pdf-parity` worktree. English and Malayalam samples are `quiz-review/english.pdf` and `quiz-review/malayalam.pdf`; they use test answers, not a user's quiz history.

## Reproducing native verification

Install the final debug app and instrumentation APK from this source, then run:

```sh
adb -s EMULATOR_SERIAL shell am instrument -w -r \
  -e class com.aaronsedna.hopecards.ui.QuizReviewScreenTest \
  com.aaronsedna.hopecards.debug.test/androidx.test.runner.AndroidJUnitRunner
```

This exercises English, Malayalam, Malayalam at 200% font scale, and the Midnight theme. It checks all question/answer/explanation/reference text for visual overflow, captures previews, verifies direct sharing is available, and returns to the same score. The app currently implements light color schemes for all named themes; Midnight is not a separate system-dark implementation.

The change is based on v40 commit `ba5687c1176f704bfb97f2e6a47e36fe06c7c9e8`, integrated as `61ba366`. Its question-specific Compose item keys remain intact. The isolated task did not create a release bundle or submit a Play release. The coordinated release includes the subsequent Bold change in version 41. Its final captures, sample PDFs, rendered page previews and test logs are under `output/quality-2026-09-28/`. Version 41 was installed and opened on the Samsung SM-A165F; all functional testing remained on the emulator as requested.
