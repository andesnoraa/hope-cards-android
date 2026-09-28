# Bible Quiz

Bible Quiz is available from the navigation drawer. It follows the existing Bible translation setting, including changes made through Settings. There is no language switcher in the quiz or its title bar. Product navigation continues to use English; all quiz content and controls use the selected Bible's language.

The bank contains 400 multiple-choice questions in each of English, Malayalam, German, French, Italian, Spanish, and Filipino/Tagalog (2,800 localized entries). There are 330 original story questions and 70 passage-recognition questions using scripture already bundled in the app. All four English editions share the English quiz. Ten distinct questions are drawn per round from a shuffled, persisted language-specific deck. Each 400-question cycle is exhausted before refilling, with the last round deferred at cycle boundaries. Answers lock when checked. Feedback shows a localized scripture reference and an explanation where one is provided; reference-only feedback avoids filler text. Results show the score and a review of every answer.

There is no quiz network request, runtime generation, account, API key, or additional SDK. A round is retained per language through navigation, translation changes, and saved-instance-state restoration. It is not a permanent score history or part of the app's backup format. Starting another round or confirming “End quiz” resets that language's round.

## Content and source research (2026-09-26)

Online quizzes are available in all five languages initially requested:

- English: https://bibletrivia.co.uk/assets/1001-Bible-Trivia-Questions-v1_04.pdf
- Malayalam: https://www.malayalamcatholicbiblequiz.com/
- German: https://quizado.com/de/blog/bibelquiz-fragen-und-antworten
- French: https://www.interbible.org/responsive/index.html
- Italian: https://quizado.com/it/blog/domande-e-risposte-quiz-bibbia

These links document availability, not permission to copy the question banks. No public quiz API or clearly reusable ready-made corpus covering the requested languages was verified. **None of those question banks was imported.** The shipped questions, distractors, and explanations are authored for Hope Cards from biblical facts, with references for checking answers. The story questions are not quotations of a particular Bible edition or translations of another site’s quiz bank. Passage-recognition questions reuse the app’s existing licensed/public-domain verse assets and their existing edition attribution. No additional Bible text is fetched at runtime.

The free Bible-text API at https://bible.helloao.org/docs/ serves scripture rather than quiz questions. The initial references were checked against returned chapter content using BSB, German Luther 1912, French Louis Segond, Italian Riveduta, and Malayalam contemporary-orthography 1910 data. See `references.json` for chapter URLs and reference identifiers. This is development research only: the app does not call that API or ship its returned verse text. API availability does not replace each Bible edition's own licensing terms. In particular, the revised Malayalam text has CC BY-SA terms: https://ebible.org/mal2015/copyright.htm.

Translations and factual wording were checked during implementation; no independent human editorial or native-speaker review is claimed. Before expanding the bank, verify the answer and distractors against the cited passage and review every localization. Avoid denomination-dependent answers, unqualified Bible-book counts, disputed authorship, and details absent from the passage.

## Editing the bank

Edit the multilingual source files under `scripts/quiz/` and run `python3 scripts/quiz/build_bank.py` to regenerate `android/app/src/main/assets/quiz/questions.json`. The initial 30 questions are retained from the asset itself. Preserve stable IDs for existing questions. Each question requires a reference, a zero-based correct answer index, and all seven locale objects, each with a question, four distinct options, and an explanation. The correct option has the same index across locales. Increment `contentVersion` after content changes. Preserve enough entries to create a full round. Retired or invalid IDs invalidate a restored round safely.

Quiz language mapping and scoring live in `model/BibleQuiz.kt`. `data/BibleQuizRepository.kt` validates and loads the bundled asset on an IO dispatcher. `ui/screens/BibleQuizScreen.kt` uses a quiz-specific resource context so global UI language behavior is unchanged. `HopeCardsApp.kt` owns a saveable state holder keyed by quiz language. Existing reference formatting and local Malayalam fonts are reused.

## Verification

- JVM tests: language mapping for every edition, unique rounds, score accuracy, answer locking, invalid/restored state handling.
- Instrumented tests: all localized assets and references, live language changes, per-language saved progress, a complete scored round, saved-state restoration, large Malayalam text, and end-round cancellation/confirmation.
- App integration test: navigation drawer, changing Bible translation in Settings, and returning to an unfinished round.
- Build and lint: debug only. No Play release bundle, release version increment, publication, or signing change is part of this feature implementation.

Verified on 2026-09-26: all 49 JVM tests passed; `lintDebug` passed (existing warnings and two pluralization suggestions for quiz counts); debug app and instrumented-test APKs built. All four quiz instrumented tests passed on the phone emulator. The full-app navigation/translation integration test passed on both phone and tablet emulators. Phone screenshots were visually inspected, including Malayalam; the tablet screenshot was obscured by an Android system tutorial, so no tablet visual sign-off is claimed. A normal debug APK was subsequently built for device installation.


## Refined quiz UI and completion ads

The quiz uses the app's HopeTheme colors, Poppins and Noto Sans Malayalam, pill buttons and a puzzle outline drawer icon. Correct answers use green and incorrect selected answers use red, with check/cross symbols and spoken state descriptions so feedback does not depend on color alone. The full-width answer feedback card aligns with the answer choices. Its result label and localized Bible reference share a row when they fit and wrap for longer translations or larger fonts. Incorrect responses show a red status and the correct answer in green. Available explanations appear below without repeating the reference; reference-only questions stay compact. The answer review retains its inline references. Intro and result wording is concise, and the results page offers a separate answer review. Inline middle-dot separators and the offline footer are omitted.

“Enable sound” appears only in the start screen’s grouped setup card. It is off by default and is remembered on the device. Two original 300 ms PCM chimes are bundled in `res/raw/quiz_correct.wav` and `quiz_incorrect.wav` (22,050 Hz mono, 16 bit). SoundPool uses one stream, plays only on an explicit answer submission, honors silent/vibrate and system volume, pauses when the app is backgrounded, and releases when disabled or the quiz leaves composition. Recreating or reviewing a round does not replay sounds.

Each completed ten-question quiz displays the score immediately. The ad opportunity is deferred until the user starts a new quiz or leaves the completed quiz using Back or the navigation drawer. Opening answer review and returning to results never show an ad. A saveable per-round marker consumes the exit opportunity once, and repeated taps are ignored while the transition is pending. Incomplete rounds and app backgrounding do not trigger it.

The existing AdsManager, consent handling, ad-free entitlement and app-wide ten-minute cooldown are retained. A loaded ad must already exist at the exit tap; absent/expired ads are skipped and late loads cannot interrupt the next screen. No new advertising SDK or ad unit is introduced.

AdMob’s interstitial guidance (including its preference for ads before break pages) is documented at: https://support.google.com/admob/answer/6201350 . Cadence and transition behavior are covered by `QuizInterstitialPolicyTest` and `ContentBreakTest`; the UI tests check score-first presentation, ad-free review, one exit opportunity, repeated-tap protection, and saved-state restoration. Actual ad availability depends on consent, connectivity and inventory.


Final verification on 2026-09-26: 53 JVM tests passed and `lintDebug` passed. All 12 focused instrumentation checks passed on the tablet emulator (11 in the combined run, followed by the corrected gallery-navigation test). Coverage includes all seven quiz languages, scoring, saved-state restoration, sound preference persistence, translation changes through the app, large Malayalam text, all eight selected image backgrounds across editions, the complete eligible verse-art rendering regression, bounded caches, matching exports, and rotation between gallery visits. Android's tablet letterbox education was disabled on the test emulator because its overlay intercepted navigation.

The normal debug APK (1.0.8-debug, versionCode 35) was installed successfully on the Samsung SM-A165F and launched successfully. This is a local QA installation, not a Play release. Live ad inventory and battery consumption were not benchmarked.

Quiz refinement: removed the round-length/time-limit promotional text, used a puzzle outline drawer icon, tightened answer spacing, and grouped the score. Wrong answer submission plays two 90 ms vibration pulses separated by 70 ms. It respects the app’s Enable Haptics setting and Android’s HAPTIC_FEEDBACK_ENABLED setting, uses sonification audio attributes, does not loop, and cancels on background/disposal. This works on basic motors without predefined-effect or amplitude-control support. The Samsung SM-A165F rejected the earlier semantic REJECT effect as ignored_unsupported; a real-device answer submission with the fix was recorded by Android as a finished 257 ms TOUCH vibration with the expected pulse pattern. Correct answers, review and restoration do not request haptics.

The follow-up quiz polish passed 53 JVM tests, lintDebug and seven focused quiz instrumentation tests, including wrong-only haptics, disabled-haptics behavior, sound limited to setup, score-first ad timing, review, repeated exit taps, and restoration.

## Expanding and validating the bank

`scripts/quiz/story-additions.tsv` is the multilingual source for the first 100 added story questions. `answer-labels.tsv` supplies their localized options. The three `story-v3-*.tsv` files add 200 more story questions (70 from Genesis–2 Kings, 60 from 1 Chronicles–Malachi, and 70 from the New Testament); their corresponding `labels-v3-*.tsv` files supply additional localized options. Run `python3 scripts/quiz/build_bank.py` to rebuild the 400-question asset deterministically, retaining the existing 200 entries unchanged. Passage questions are selected across Bible books from the already bundled verse files. Their options exclude identical passages and repeat references. Quotes use body typography below the question heading.

`expanded-references.json` records the 100 checked BSB chapter URLs and local sources for all 70 passage questions. The German Jacob-name and Jonah-duration references use Luther’s printed numbering (Genesis 32:29 and Jonah 2:1). The generator does not translate or call a quiz API.

The three `references-v3-*.json` files record the 200 additional questions, their checked source passages, answers, distractors and review notes. Five new German references use Luther’s printed numbering: Exodus 8:2, Numbers 17:23, 1 Samuel 24:5, Nehemiah 4:12 and Daniel 6:11. These overrides are applied by the generator. The added questions use reference-only feedback, as the existing story additions do.

The generator rejects duplicate IDs, repeated passage references, normalized duplicate question text in each language, normalized duplicate answer choices, and story references overlapping an existing passage-recognition quotation. Normalization ignores capitalization, punctuation, whitespace and Unicode compatibility variants. Content review also checks for the same tested fact under different wording or in parallel Bible accounts; distinct facts within the same episode can remain. The source records describe the model-led fact and translation review; no independent human or native-speaker review is claimed.

`QuizQuestionRotationTest` covers a full 400-question cycle, expansion from a saved 200-question deck without repeats, boundary repeats, small banks, corrupt IDs and content updates. `QuizRoundHistoryInstrumentedTest` checks persistence across repository recreation and independent language queues. The full-round UI test checks wrong-only vibration dispatch, disabled haptics, no replay on restoration, and score-first ad timing. Actual vibration playback was verified using the Samsung vibrator service log, beyond mocking the feedback callback.

Version 2 verification (2026-09-26): 57 JVM tests and nine focused emulator instrumentation tests passed. Debug build and lint passed. The generator produces the same asset on repeated runs. A separate wrong-answer submission test passed on the Samsung SM-A165F, and its vibrator service reported the double-pulse effect as finished.

Version 3 verification (2026-09-28): all 87 JVM tests, `lintDebug`, the debug build, and four focused tests on an isolated Android 15 tablet emulator passed. The app loaded all 400 questions for every Bible edition; the tests checked a full 400-question persisted cycle, scoring/restoration and large Malayalam text. Fifty missing book-name mappings were added across German, French, Italian, Spanish and Filipino; an asset-driven JVM test covers every reference. Repeated generation is byte-identical, the debug APK contains the verified asset, and every original question object is unchanged. The final bank has no normalized duplicate prompts/options or story-reference overlap with recognition quotations. See `expansion-v3-verification.json` for counts, content hash and review limits. No Play release was produced.

## Sharing, PDF export, and printing

The completed score screen has a single “Share or export” menu. “Share results” opens Android's text share sheet with the localized score, quiz language/edition, and Hope Cards link. “Save as PDF” generates an A4 report containing the score, every question, the selected answer, correctness, corrections, available explanations, and localized Bible references. The user chooses the destination through Android's document picker. “Print” opens the native print preview for the same report and supports paper changes, page selection, and cancellation. These actions keep the completed round open and do not consume its completion-ad opportunity.

All seven quiz languages have localized controls and report labels. PDFs use bundled Poppins or Noto Sans Malayalam fonts. The Android-owned picker, chooser, and print dialogs follow the device language. Rendering and file IO run off the main thread. Reports are drawn as text into native PDFs, with page breaks between questions where possible and line-level continuation for oversized passages. Only requested pages are written for print jobs. A temporary PDF path is saved while the document picker is open so the exported content remains a snapshot of the completed round.

Verification: `QuizReportTest` opens generated PDFs for all seven languages at A4, landscape, and small paper sizes, validates layout bounds, selected-page output, and cancellation. Quiz UI tests check the menu, saved score, external share/save/print flows, file-picker cancellation, and zero completion-ad callbacks during export. English and Malayalam report pages and the Android print preview were visually reviewed. Printing was checked through preview; no physical printer job was submitted.
