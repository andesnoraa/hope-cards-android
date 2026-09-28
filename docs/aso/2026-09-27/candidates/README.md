# Alternate ASO candidates, not published

The title and short-description files in this folder supplied the selected
September 27 edits. Their presence here does not confirm that Play Console has
saved, approved or published those edits. Refer to the task's final publication
record for the external state.

The two `full-description.txt` files are alternate drafting material only. They
were not selected or submitted. The live English and Malayalam descriptions
already include the quiz, verse images and PDF sharing, so this ASO pass preserves
that live copy. Do not replace the live descriptions with these alternate files.

The alternate full drafts also need a language-claim correction before any future
reuse: their broad app-language wording must not imply a fully localized product
interface. `Localization.kt` intentionally keeps core navigation and settings in
English. Bible content supports the advertised languages, and `QuizLocalization.kt`
makes the quiz follow the selected Bible language. A translated store listing
does not mean that every app screen uses that language.

Daily Hope's daily-verse description was checked against `AppRepository.dailyHopeVerse`:
the app selects and stores one verse for the user's local calendar day and passes
it to `DailyHopeScreen`. It is accurate to describe Daily Hope as presenting today's
Bible verse.

Draft character counts, excluding the terminal newline:

| Locale | Title | Short description | Alternate full description |
| --- | --- | --- | --- |
| en-US | 29/30 | 71/80 | 1826/4000 |
| ml-IN | 22/30 | 49/80 | 2134/4000 |
