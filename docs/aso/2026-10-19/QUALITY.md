# Quality and acquisition observation — October 19, 2026

## Quality

Monitor and improve reports **No policy issues found**. For **Android vitals, Last 28 days**, user-perceived crash rate, user-perceived ANR rate, and slow cold start rate each showed **Data unavailable**. These are not zero measurements. Play Console provides no battery-drain measurement in the inspected views.

The Ratings page still showed **5.000 Default Google Play rating**, **— Average rating (last 28 days)**, and **1 user**; the selected ratings breakdown had no matching ratings. The sample is too small to show a rating trend.

The R8 advisory attached to release **1.0.14 (47)** remains present: it warns R8 configuration “could be causing higher memory usage and lower performance,” says optimized resource shrinking is not enabled, and recommends AGP 9.0+. This is not a measured memory regression. Local Gradle uses AGP 8.13.2 and has both `minifyEnabled true` and `shrinkResources = true`. No project change or new bundle was made.

Grow overview now displays **7-day device retention = 1**, **−89%** versus its previous-period comparison. The card is under **Last 28 days** and reports a device count. This is a notable decline, but the absolute count is very small; investigate with cohort-level evidence before attributing a product cause.

## Acquisition and listing metrics

- **Grow users → Your performance on Play**, “Metrics by: Device”, **Last 28 days** (Console shows a relative window, not exact date bounds): 4.79K device impressions (+980%), 257 device acquisitions (+928%), 39 device first opens (+70%), and 43 monthly active devices (+139%). It also shows +16 explore user acquisitions in the last 90 days. These are all-market device metrics; the detailed source-level report does not reconcile to this aggregate in the inspected view. Paid activity and other changes can confound the increases.
- **Statistics → App statistics**, all device acquisitions, all events, per interval, daily, Traffic source:
  - **Oct 9–15, 2026**: the table displayed rows for Oct 13 (Google Play explore 1, Not attributed 1) and Oct 15 (Not attributed 1), for 3 recorded events across two listed days. No Paid and direct row appeared. Missing days are not treated as zero.
  - **Previous interval Oct 2–8, 2026**: rows totaled 91 recorded events across five listed days: Google Play explore 2, Paid and direct 31, Not attributed 58. “Paid and direct” combines different mechanisms and is not Ads-only. The sparse date rows and differing source coverage mean these figures are not a clean full-week acquisition comparison.
- **Store listings → Your performance on Play**, Installs metric, **Sep 18–Oct 15, 2026**: 520 visitors (+1,386% vs previous period), 117 unique user install clicks (+588%), and 23% click-through rate (+54%). Clicks are not completed installs. Default listing row: 518 visitors, 19.1% conversion, Live, last updated Oct 8. These are not query-attributed.
- The listing report provides the exact 28-day dates above. Grow overview provides only “Last 28 days” and “Last 90 days” labels; do not infer exact bounds from those cards.

No query-level search-term acquisition, keyword volume, Android vitals trend, or battery measurement was available. No metadata, binary, or Google Ads setting was changed.
