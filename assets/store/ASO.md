# Hope Cards ASO plan

## Positioning

Hope Cards is positioned as a calm daily Bible verse app built around beautiful Scripture cards, Daily Hope, Bible quiz, a quiz participation certificate generator, shareable verse images, favorites, verse-linked journal notes, reminders, multiple Bible translations, and visual themes.

The listing must not imply that Hope Cards is a complete chapter-by-chapter Bible reader, devotional library, prayer community, or fully localized user interface. Bible content is available in the listed languages. Core navigation and settings intentionally stay in English, as implemented in `Localization.kt`; the Bible quiz follows the selected Bible language through `QuizLocalization.kt`. Localized store listings are not a claim that the entire app interface is localized.

## Search intent priorities

1. Daily Bible verse / verse of the day (primary acquisition intent)
2. Hope Cards / Hopecards (brand discovery, measured separately)
3. Bible quiz
4. Bible quiz certificate generator / quiz participation certificates (specific feature intent; demand not yet measured)
5. Bible verse images / Malayalam Bible verses
6. Bible verse app / daily Bible reminder
7. Bible journal / verse notes / favorite Bible verses
8. Scripture cards (useful differentiator, lower search priority)
9. KJV and multiple Bible translations
10. Christian encouragement and reflection

These phrases are used naturally in the title, short description, and opening paragraphs. Do not add repetitive keyword blocks: Google Play advises that unnecessary keywords do not improve ranking and create a poor user experience.

## Standing listing rules

- Recheck live Google Play results and Play Console search-term data before every
  meaningful metadata update; do not rely on an old keyword list indefinitely.
- Lead each localized title with the strongest natural, high-intent phrase for
  that market, while keeping the Hope Cards brand visible where the 30-character
  limit permits.
- Use native, idiomatic wording written for that locale. Never translate English
  keywords word-for-word when local search phrasing differs.
- Give the title, short description, and opening paragraphs distinct jobs. Cover
  the main intent early, then add accurate long-tail benefits without repetition.
- Keep every claim faithful to the shipped app. Never use ranking claims,
  promotional superlatives, competitor names, or repetitive keyword blocks.
- Preserve approved localized screenshots, feature graphics, and promo videos
  unless the UI or positioning has materially changed.
- Validate the 30-character title, 80-character short description, 4,000-character
  full description, artwork, and locale files before saving in Play Console.
- After publication, measure search terms, conversion, ratings, retention and
  Android vitals. Test one conversion variable at a time once traffic is sufficient.
- Do not restart an active app-release review solely to submit metadata. Save the
  listing changes and submit them after the active review completes unless there
  is a time-critical correction.

## September 2026 keyword review

Live Google Play searches were reviewed for the United States, Germany, Mexico,
France, Italy, the Philippines and India. The highest-intent recurring phrases
were moved to the front of each localized title:

- English and Filipino market: Daily Bible Verse / Bible verse of the day
- German: Bibelvers des Tages / tägliche Bibelverse
- Spanish: Versículo del día / versículos bíblicos diarios
- French: Verset du jour / versets bibliques quotidiens
- Italian: Versetto del giorno / versetti biblici quotidiani
- Malayalam market: Malayalam Bible Verse / ഇന്നത്തെ ബൈബിൾ വാക്യം

“Bible cards” was not selected as a primary English keyword because live results
were dominated by Bible-themed card games. “Scripture cards” remains useful in
the full description as an accurate, differentiating secondary phrase. The
September 27 English short description instead highlights the quiz and verse images.

Ranking first cannot be guaranteed by metadata alone. Search position also
depends on install velocity, ratings, retention, listing conversion and app
quality. Review Google Play search-term and conversion reports after the new
metadata is indexed, then test one conversion variable at a time.

## September 28, 2026 certificate-generator update

The short and full descriptions in all seven listing languages are prepared for
submission alongside the app release. The existing titles are preserved. The
English and Malayalam titles retain the daily Bible verse purpose and Hope Cards
brand. The new copy adds the Bible Quiz
Certificate Generator as a concrete feature: A4 participation PDFs, multiple
participant names, output-language selection, a date and optional organization,
and sharing one or all certificates. It also describes the optional 30-second
countdown for each question, automatic advancement when time expires, and the
complete PDF answer report.

The descriptions no longer advertise the removed print action or an outdated
background count. Core navigation remains English; the copy only describes the
supported Bible, quiz and certificate languages. Certificate generation is a
participation feature, with no claim of institutional accreditation or scoring.

| Locale | Short description | Characters |
| --- | --- | --- |
| en-US | Daily Bible verses, verse images, Bible quiz and a quiz certificate generator. | 78/80 |
| ml-IN | മലയാളം ബൈബിൾ വചനങ്ങൾ, വചനചിത്രങ്ങൾ, ക്വിസ്, പങ്കാളിത്ത സർട്ടിഫിക്കറ്റുകൾ. | 73/80 |
| de-DE | Tägliche Bibelverse, Bibelquiz, Versbilder und Teilnahmeurkunden. | 65/80 |
| es-419 | Versículos diarios, imágenes bíblicas, quiz bíblico y certificados. | 67/80 |
| fr-FR | Versets du jour, images bibliques, quiz et attestations de participation. | 73/80 |
| it-IT | Versetti quotidiani, immagini bibliche, quiz e attestati di partecipazione. | 75/80 |
| fil-PH | Talata ng araw, Bible quiz, mga larawan ng talata at sertipiko ng pakikilahok. | 78/80 |

See the [prepared metadata record](../../docs/aso/2026-09-28/certificate-generator-update.md)
for exact file paths, limits, sources and publication status. This is a local
preparation record; submission and approval must be confirmed in Console.
The September 27 record below remains a historical account of that earlier copy.

## September 27, 2026 execution

Work records and the search baseline are under
[`docs/aso/2026-09-27`](../../docs/aso/2026-09-27/BASELINE.md).
The initial market for measurement is India, with English and Malayalam
observations recorded separately. “Daily Bible verse” is the primary intent
because it accurately describes the app; no reliable keyword search-volume
data was available to establish that it is the category's highest-volume term.
“Verse” and “Daily Bible” are broader queries to observe, not reasons to imply
that Hope Cards provides a complete Bible reader.

The live Console audit found the English title “Daily Bible Verse & Bible Quiz,”
which omitted the Hope Cards brand. The following targeted title and short
description changes were saved and submitted during this task and synchronized
to the tracked locale files:

| Locale | Title | Title length | Short description | Short length |
| --- | --- | --- | --- | --- |
| en-US | Daily Bible Verse: Hope Cards | 29/30 | Verse of the day, Bible quiz and verse images. Read, reflect and share. | 71/80 |
| ml-IN | ബൈബിൾ വചനം: Hope Cards | 22/30 | മലയാളം ബൈബിൾ വചനങ്ങൾ, വചനചിത്രങ്ങൾ, ബൈബിൾ ക്വിസ്. | 49/80 |

The existing live English and Malayalam full descriptions already describe the
quiz, verse-image gallery and PDF sharing, and were unchanged in Console. The
tracked `full-description.txt` files were synchronized to those exact pre-edit
live values: 2,425 English characters and 2,108 Malayalam characters, with string
hash matches verified. This is local archive alignment, not a submitted
full-description change. Alternate full drafts under
`docs/aso/2026-09-27/candidates/` are not the selected or published copy; see that
folder's README before reusing any candidate.

Eight opaque 1080 × 1920 phone images per locale were saved in the verified order
for the English and Malayalam listings. At **2026-09-27T15:19:59.614Z**, Publishing
overview accepted exactly six changes: title, short description and phone
screenshots for each locale. It showed **“Changes in review”** and **“Running
quick checks” at 11%**, estimated up to 13 minutes, with text explaining that the
changes would be sent when checks succeeded. This confirms submission acceptance
into the publishing/review queue while quick checks remained pending. Approval
and public visibility are not confirmed. Managed publishing is off. Existing
full descriptions, videos, icons, other locales and tablet screenshots were
unchanged in Console, and no binary was released. See
[`EXECUTION.md`](../../docs/aso/2026-09-27/EXECUTION.md) for the exact record.

The daily ASO heartbeat `improve-hope-cards-search-visibility` was successfully
created, is ACTIVE and is attached to the current task. It runs daily at
10:30 a.m. Asia/Kolkata under `docs/aso/ASO-OPERATIONS.md`, with quiet reporting
while unchanged or non-actionable. Google Ads and its INR 2,500 allowance remain
unchanged.

Top-five placement remains an objective, not a completed result or guarantee.
Measure organic positions for a fixed query, country, language, device/surface
and account state; exclude sponsored results and record the observation date.
Check exact-brand searches for both “Hope Cards” and “Hopecards” separately.
Report search position together with actual acquisition and retention trends,
not as a substitute for them.

## Conversion priorities

The September 27 English and Malayalam ASO sets lead with the primary daily Bible verse purpose. This order was verified in the saved Console listings and accepted submission:

1. Scripture reading proves clear verse typography and translation detail.
2. Daily Hope communicates the daily-use habit.
3. A Bible Quiz question demonstrates the question and answer interaction.
4. Quiz results show the score, answer review and export options.
5. Drawing a card explains the card interaction.
6. Favorites show that meaningful verses can be kept.
7. Journal notes explain reflection linked to a verse.
8. Themes demonstrate personalization without suggesting feature gating.

The historical six-image set led with drawing a card; its filenames and older README order are not the submitted eight-image ASO order. The feature graphic and promo video retain the app's navy, ivory and gold identity. Public visibility of the submitted order remains unverified while quick checks and review are pending.

## Measurement after publication

- Track store-listing visitors, install/open clicks and CTR by country, language and traffic source. Current Play Console listing reports emphasize clicks; use Grow users overview, Statistics or downloadable reports for completed acquisitions.
- Compare listing conversion by locale after enough traffic accumulates. Do not describe an install click as a completed installation.
- Keep Google Play Search, Explore, and Ads and referrals separate. Category searches can appear under Explore; branded search traffic alone is not the total organic search opportunity.
- Run one store-listing experiment at a time: first screenshot wording, then screenshot order, then feature graphic.
- Treat ratings, retention, Android vitals, app size, and crash-free performance as ASO inputs—not only metadata.
- Refresh screenshots whenever the app UI changes so the listing remains accurate.

## Official guidance

- https://support.google.com/googleplay/android-developer/answer/4448378
- https://support.google.com/googleplay/android-developer/answer/9866151
- https://support.google.com/googleplay/android-developer/answer/13393723
- https://support.google.com/googleplay/android-developer/answer/9958766
- https://support.google.com/googleplay/android-developer/answer/9042516
- https://support.google.com/googleplay/android-developer/answer/9859173
- https://support.google.com/googleplay/android-developer/answer/12053285
- https://support.google.com/googleplay/android-developer/answer/9898842
- https://support.google.com/googleplay/android-developer/answer/9859152
