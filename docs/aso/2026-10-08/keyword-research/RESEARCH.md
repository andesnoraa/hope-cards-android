# Hope Cards keyword research and Gemini migration

Research date: October 8, 2026. Existing app: `com.aaronsedna.hopecards`. This is an intraday action record, separate from the morning's read-only rank samples.

## Evidence and limits

Google Ads Keyword Planner was accessed in the existing signed-in Chrome account. Scope: India, English targeting, Google network, September 2025 through August 2026. The Malayalam-script seeds below were queried in that same English-targeted context; they are not a Malayalam-targeted demand study. Planner displays monthly ranges, includes close variants, and measures Google Search demand. Its competition column measures advertiser competition, not Google Play ranking difficulty. See [Google's metric definitions](https://support.google.com/google-ads/answer/3022575?hl=en-2).

The raw dated snapshots and [structured observations](keywords.json) preserve the source of each range. The suggestions snapshot captured during page navigation has a transitional footer; use `planner-india-en-page2-loaded.txt` for the actual loaded second page, without asserting a page position for the transitional rows. No proprietary ASO subscription or keyword-volume service was purchased. Existing Google Play daily samples remain the source for Play ranking, not these search ranges.

| Keyword | Planner average monthly range | Role |
| --- | --- | --- |
| daily bible verse | 1K–10K | Primary intent; preserve title |
| verse of the day | 10K–100K | Strong secondary intent |
| bible verse of the day | 10K–100K | Natural variant |
| bible quiz | 1K–10K | Existing feature |
| bible trivia | 100–1K | Quiz synonym |
| bible quiz questions and answers | 100–1K | Review-answer benefit |
| bible verse images | 100–1K | Gallery/sharing benefit |
| malayalam bible | 10K–100K | Supported Scripture language |
| malayalam bible verses | 10K–100K | Localized feature intent |
| malayalam bible quiz | 100–1K | Localized quiz intent |
| bible quiz certificate | 10–100 | Distinctive niche feature |
| random bible verse | 1K–10K | Draw-card feature |
| bible verses about hope | 1K–10K | Existing topic category |
| daily scripture | 10K–100K | Relevant secondary wording |
| Hope Cards | 10–100 | Brand; not a Play ranking estimate |
| ഇന്നത്തെ വചനം | 10–100 | Natural Malayalam daily-verse phrase |
| സത്യവേദപുസ്തകം | 100–1K | Existing MAL1910 edition |
| ദൈവവചനം | 100–1K | Relevant Malayalam phrase |

Planner did not expose explicit rows for `hopecards`, `മലയാളം ബൈബിൾ വചനങ്ങൾ`, or `ബൈബിൾ ക്വിസ്` in the inspected output. Their volume is unavailable, not zero. Avoid attributing another row's volume to these terms.

Other observed suggestions include encouraging/inspirational/uplifting Bible verses, verses about strength, faith and love, today's Bible verse, Bible trivia questions and Scripture for encouragement. Existing hope/faith/love/peace/strength categories support natural topic wording. Audio Bible, NIV study Bible, Good News Bible, Catholic Bible, Hebrew Bible and full reading-plan/reader claims were excluded because the production app does not support those specific promises. Healing/anxiety search demand does not justify medical claims.

## Competitor wording

These are phrases visible in public titles/descriptions, not access to competitors' hidden keywords, conversion, bids or ranking strategies.

| Official Play listing | Visible relevant wording |
| --- | --- |
| [DailyVerses Bible verse of the day](https://play.google.com/store/apps/details?hl=en-US&id=com.dailyverses) | Daily Bible verse, inspiring verse, daily notification, offline |
| [Bible – Daily Bible Verse](https://play.google.com/store/apps/details?hl=en_US&id=com.bible.holybible.nkjv.dailyverse) | Daily Bible verse, verse of the day, daily picture |
| [Supergonk Bible Quiz: Trivia Game](https://play.google.com/store/apps/details?hl=en_US&id=org.supergonk.bible) | Bible quiz, trivia, Old/New Testament, Sunday School |
| [Dozcore Bible Quiz](https://play.google.com/store/apps/details?hl=en&id=com.dozcore.mathew.biblequiz) | Questions, references, Bible competitions, Sunday School |
| [Biblica Malayalam Bible](https://play.google.com/store/apps/details?hl=en&id=org.fcbh.mjsbib.malayalambible.biblica.mcv) | Malayalam Bible, Verse of the Day, reminder, verse wallpaper, sharing |
| [Love Fellowship Malayalam Bible](https://play.google.com/store/apps/details?hl=en&id=org.ips.mal1bible.malayalam) | Malayalam Bible, sharing Bible verses, bookmarks |
| [Arise Solo Daily Bible](https://play.google.com/store/apps/details?hl=en&id=com.dailybible.app) | Daily Bible verse, reminders, shareable verse posts, favorites |

Use the shared category vocabulary where accurate. Do not insert competitor names or copy their unsupported translation/audio/full-reader benefits. Google's [store-listing guidance](https://support.google.com/googleplay/android-developer/answer/13393723?hl=en) supports clear, relevant descriptions over repetition.

## ASO action

The user explicitly requested Planner-driven optimization on October 8. Before editing, Publishing overview had no pending changes and production 1.0.13 (45) was already live. This directed research-based update supersedes the prior plan to wait until October 13; it was not a reaction to a daily rank fluctuation.

Only English (en-US) and Malayalam (ml-IN) full descriptions changed. Title, short descriptions, approved images/videos, other locales, application ID and campaign settings remain unchanged. Before/after copies are retained beside this record.

English now names Bible trivia, quiz questions and answers, Bible verse images and random/encouraging verses about supported categories. Malayalam now uses ഇന്നത്തെ വചനം, a quiz questions/answers heading and the supported സത്യവേദപുസ്തകം edition. Existing certificate, timer, review and sharing descriptions remain.

Console initially accepted **two full-description changes for review**, preserved in `publication-after-submission.txt`. **Submission 38 was published on October 8, 2026 at 9:55 PM (Console display)**. `aso-published-submission38.txt` confirms publication. Both public India desktop listings were subsequently checked with `gl=IN`, `hl=en_US` and `hl=ml`; each contains the exact new full-description wording. Evidence is `public-english-after-aso.txt` and `public-malayalam-after-aso.txt`. Managed publishing remains off. Metadata publication is separate from the later binary release.

The new minimum fourteen-day observation window starts October 8; the next planned metadata evaluation is no earlier than **October 22**, with sufficient traffic and stable quality. Retain daily rank context and distinguish paid acquisition, product updates and language availability as confounders. Continue the existing automation rather than creating another one. No first-five placement is promised.

## Google Gemini app translations

The approved Google terms were accepted, and Console confirms six automatically translated language codes: **pt, de, fr, it, ml, es**. Portuguese (`pt`) is the code offered by Google; the picker does not expose a separate Brazil option. Google now manages German, French, Italian, Malayalam and Spanish alongside Portuguese. Some existing wording remains identical to the source translations; activation is not proof that every string was freshly reworded. Tagalog/Filipino is not offered; its existing resources remain.

This is the Play Console app-string translation service, not a runtime Gemini API or translation of JSON Scripture/quiz content. Bible text, questions, answers and certificate-language selection remain tied to the supported Bible editions. Hope Cards' brand resource is explicitly non-translatable. Gallery controls are eligible for translation; the Scripture printed on existing artwork remains its original edition.

Code inspection found the general UI context forced `Locale.US`. The local fix preserves the device configuration, refreshes remembered contexts when the device locale changes, and chooses the bundled Malayalam font for Malayalam UI. The separate quiz resource context continues to follow the chosen Bible language. The Google-generated Portuguese strings are now retained locally for offline/local builds, with reviewed card-drawing corrections. Only unused resources stripped from Google's APK retain the default English wording.

Console requires a bundle in a draft release before translation preview/editing is available; already published bundles are not retroactively translated. See [Google's app translation documentation](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en). Configuration is enabled. Google generated Portuguese and the new gallery controls; the signed preview APK preserves Scripture and quiz assets byte-for-byte. Testing caught Portuguese “Draw a Card” rendered as “Comprar uma carta” (buy a card). The five related card controls were corrected in Google's catalog and the local export. The changed Bible-picker explanation also needed five reviewed catalog corrections. Gemini is therefore not represented as automatically higher quality than every existing translation.

Google's preview APK did not contain a Portuguese About feature array. That list now references the same individually translated UI strings, and the array itself is non-translatable because it contains resource references. This avoids a separate conflicting wording set. The corrected release candidate is **1.0.14 (47)**, incremented above the accepted draft 46. See [TRANSLATION-PREVIEW.md](TRANSLATION-PREVIEW.md) for the failures, corrections and final validation status.

## Boundaries

Google Ads was used for read-only Planner research. No campaign, bid, budget, dates, INR 2,500 ceiling, conversion goals or advertisement assets were changed. No traffic, installs, reviews or paid tools were purchased. Future app testing uses the emulator. Existing unrelated working-tree changes are preserved.
