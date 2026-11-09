# ASO observation — October 14, 2026

Samples were captured in signed-in Google Chrome, profile “Your Chrome,” Google Play desktop web, `gl=IN`, English `hl=en`, Malayalam `hl=ml`. The account fingerprint matches the established baseline context. Search samples were captured October 14 between 05:11:30 and 05:11:57 UTC. App identity was checked by package ID `com.aaronsedna.hopecards`. No Sponsored labels were seen. Up to 50 returned app results were inspected; Malayalam results exposed destination package IDs in this capture.

| Exact query | hl | Hope Cards organic position | Results inspected |
|---|---|---:|---:|
| daily bible verse | en | Absent | 30 |
| Hope Cards | en | 1 | 30 |
| Hopecards | en | 2 | 30 |
| verse of the day | en | Absent | 30 |
| bible quiz | en | Absent | 30 |
| daily bible | en | Absent | 30 |
| verse | en | Unavailable | 20 non-comparable app links |
| മലയാളം ബൈബിൾ വചനങ്ങൾ | ml | Absent | 50 |
| ഇന്നത്തെ വചനം | ml | Absent | 50 |
| ബൈബിൾ ക്വിസ് | ml | Absent | 50 |

For `verse`, Play rendered the featured HCL Verse recommendation layout, not an ordinary organic search list, so no rank is assigned. The primary `daily bible verse` target remains outside the first 30 observed organic results. “Hope Cards” ranked #1 and “Hopecards” #2 for nine consecutive days, October 6–14, in this exact signed-in India English desktop-web context. The prior seven-day brand milestone continues; the primary-category goal remains unmet.

## Console reports

Grow users, Metrics by Device, Last 28 days versus the previous 28 days, shows 3.53K device impressions (>+999%), 254 device acquisitions (+535%), 38 device first opens (+12%), 45 monthly active devices (+88%) and 7-day device retention of 1 device (-88%). The acquisition detail link carries `dateRange=2026_9_15-2026_10_12`. Traffic-source totals are Google Play explore 13 (0%), Paid and direct 96 (+336%), and Not attributed 145 (>+999%). These global numbers do not show query-level installs. Explore can include generic category searches; Paid and direct is mixed and cannot attribute its 96 acquisitions to the Ads campaign. The 7-day retention figure is a device count, not a rate; its one-device sample is sparse.

The Store listings page labels its report “Last 28 days” and shows the live default listing at 491 visitors and 19.3% conversion, last updated October 8. The Grow overview's separate 90-day conversion rate is 24.14%; these are different windows/scopes. Unique-user install clicks and CTR were not exposed in today's listing capture. Install clicks are not completed installs. No listing experiments are running, ended or applied.

The ratings page's selected last-28-day breakdown says no ratings match its filters. The expanded overall breakdown contains one India/English rating, 5.000, attributed to older version 1.0.2 (28). Android vitals remain unavailable for crashes, ANRs and slow cold starts; no policy issues are shown. The R8 advisory is not measured memory or battery evidence. See [quality record](QUALITY.md).

The acquisition/retention figures warrant continued monitoring but are confounded by Paid and direct traffic and the small retention count. No code, listing, assets, Ads settings, tests or binary changed. The October 8 listing update's minimum fourteen-day observation window ends October 22; do not run a planned metadata evaluation before then.
