# Quality and acquisition observation — October 18, 2026

## Play Console quality

Monitor and improve reports **No policy issues found**. For **Android vitals, Last 28 days**, user-perceived crash rate, user-perceived ANR rate and slow cold start rate each showed **Data unavailable**. These are unavailable measurements, not zero rates.

Ratings reports showed a **5.000 Default Google Play rating**, **— Average rating (last 28 days)**, and **1 user** in the overview. The Ratings breakdown said no ratings matched the selected filters. This small count does not support a trend.

The Console surfaced an R8 optimization advisory for release **1.0.14 (47)**: “Your R8 configuration could be causing higher memory usage and lower performance.” It specifically says optimized resource shrinking is not enabled and recommends upgrading the Android Gradle Plugin to 9.0 or higher. This is a recommendation attached to that release, not evidence that elevated memory use was measured. Local project configuration currently uses AGP **8.13.2**, with `minifyEnabled true` and `shrinkResources = true`; the project has not been changed and no new bundle was built. Review the optimized shrinking/AGP recommendation under the existing release gates before the next release.

## Acquisition and listing reports

- **Grow users → Your performance on Play**, “Metrics by: Device”, **Last 28 days** (Console label; exact date bounds were not shown): 4.46K device impressions (>+999% vs previous period), 254 device acquisitions (>+999%), 39 device first opens (+70%), 43 monthly active devices (+139%). Seven-day device retention displayed **Data unavailable**. These are all-market device metrics, not India-only or query-attributed.
- **Statistics → App statistics**, device acquisitions, all events, per interval, daily, **Oct 6–12, 2026**, dimension **Traffic source**. The report had rows only for Oct 6, 7 and 8 when inspected; Oct 9–12 were not present, so this is incomplete for the selected week. On Oct 6 it showed Google Play explore 1, Paid and direct 25, Not attributed 58. Oct 7: 0, 2, 0. Oct 8: 1, 2, 0. Google Play Search was not a separate row. Paid and direct combines sources and cannot be attributed solely to the Ads campaign. Do not compare this partial week as a completed seven-day period.
- **Store listings → Your performance on Play**, selected “Installs” metric page, **Sep 19–Oct 12, 2026** (24 calendar days despite page control's “Last 28 days” label): 501 visitors (+1,254% vs previous period), 114 unique user install clicks (+571%), and 23% click-through rate (+50%). Install clicks are not completed installs. Default listing was Live, last updated Oct 8; its row's visitor/conversion cells were blank.
- Grow users also showed **+16 explore user acquisitions in the last 90 days**; the range was displayed as last 90 days, with no exact date bounds.

Search-term-level acquisition and keyword volume were not available in these views. No changes were made to the listing, binary, or Google Ads settings.
