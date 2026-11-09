# Quality and acquisition observation — October 20, 2026

## Quality

Monitor and improve says **No policy issues found**. Android vitals did not provide crash, ANR, or slow-start values; the dashboard shows null rates and the detailed view showed **Data unavailable**. Battery drain is not measured in the inspected views.

The Ratings page shows **5.000 Default Google Play rating**, **— Average rating (last 28 days)**, and **1 user**. The last-28-day ratings breakdown has no matching ratings, so the sample is too small for a trend.

The R8 advisory for release **1.0.14 (47)** remains: configuration could cause higher memory use and lower performance; optimized resource shrinking is not enabled; AGP 9.0+ is recommended. This is not measured memory regression evidence. Local Gradle uses AGP 8.13.2, `minifyEnabled true`, and `shrinkResources = true`. No source changes or new bundle were made.

Grow overview now shows **7-day device retention = 4**, **−56%** versus previous period, under **Last 28 days**. Yesterday it showed 1 (−89%). These small device counts are worth monitoring but do not establish a product cause.

## Acquisition and listing

- **Grow users → Your performance on Play**, Metrics by Device, **Last 28 days** (relative label; exact bounds not exposed): 5.19K device impressions (>+999%), 259 device acquisitions (+936%), 39 device first opens (+70%), 43 monthly active devices (+139%). It also shows +18 explore user acquisitions in the last 90 days. All-market device metrics; not query attribution. Paid activity and other concurrent changes can confound the increase.
- **Statistics → App statistics**, all device acquisitions, all events, per interval daily, Traffic source:
  - **Oct 10–16, 2026:** Oct 13 (Google Play explore 1, Not attributed 1), Oct 15 (Not attributed 1), Oct 16 (Google Play explore 1, Paid and direct 2): 6 recorded acquisitions across three dates.
  - **Previous interval Oct 3–9, 2026:** Oct 5, 6, 7 and 8 rows total 90 recorded acquisitions: Google Play explore 2, Paid and direct 30, Not attributed 58. Paid and direct combines mechanisms; it is not Ads-only.
  - Source-table counts are sparse and do not reconcile to Grow's 259. Dates without displayed rows are not treated as zero. Treat the difference as a follow-up signal, not a complete or causal comparison.
- **Store listings → Your performance on Play**, Installs metric, **Sep 19–Oct 16, 2026** (28 days): 525 visitors (+1,282%), 119 unique user install clicks (+561%), 23% click-through rate (+52%). Clicks are not completed installs. Default listing: 518 visitors, 19.1% conversion, Live, last updated Oct 8. Not query-attributed.
- No query-level acquisition or keyword volume was available. No metadata, binary, or Ads settings were changed.
