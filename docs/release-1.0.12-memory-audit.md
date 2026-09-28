# Version 43 memory audit

The original accumulating Activity and banner retention is resolved on the tested lifecycle paths. The raw allocation-slope test still **fails**; paired heaps attribute most of that increase to test-mode Ad Inspector history. This is evidence for a documented diagnostic exception, not a relabelled automated pass or a claim that every SDK path is leak-free.

## Artifacts and scope

The Android 15 tablet paired run used debug APK SHA-256 `f7014b0beadd7dc50674993620e7b4db585e940f7e5b30dfd12de26e77570f07`. It loaded five warmup banners and 20 measured banners, each with a distinct successful SDK response. Baseline and endpoint heaps came from the same process after more than 310 seconds in `CREATED`, followed by a heap after closing the host.

The final reviewed debug APK is `240ff38d6a629cf0568eae16b327093eef907921c43ce3c9d90eab55b9ee9066`. Its changes concern callbacks beyond 60 seconds and in-flight retries: the deadline releases strong ownership without destroying a still-loading view; a terminal callback performs the first and only destroy. These rare branches were reviewed but not deterministically exercised. The ordinary successful-load path is shared with the paired build. The final APK passed its Samsung test with three actual rendered banners, resume/navigation/recreation checks, zero earlier banners and zero destroyed hosts retained. The paired results do not claim sustained testing of the exact final binary or Play-signed artifact.

## Measured attribution

All values below are bytes. Inspector values use strong dominators with Shark's Android weak-reference exclusions; Java object-size modeling and mapped native allocations are kept separate.

| Measurement | Settled baseline | Settled endpoint | Change over 20 loads |
| --- | ---: | ---: | ---: |
| Runtime Java allocation | 12,774,832 | 16,891,264 | +4,116,432 |
| Runtime native allocation | 31,582,048 | 31,960,768 | +378,720 |
| Inspector Java ownership | 768,928 | 4,641,862 | +3,872,934 |
| Inspector mapped native ownership | 2,000 | 12,000 | +10,000 |
| Inspector request records | 6 | 26 | +20 |
| Platform pooled connections | 0 | 0 | 0 |

Inspector accounts for 94.1% of the measured Java increase. Subtracting its measured ownership change leaves an estimated 243,498 Java bytes and 368,720 native bytes, 612,218 combined (29.9 KiB per measured load). This endpoint average is not the test's fitted slope, and the subtraction is an attribution estimate rather than exact reconciliation of different memory-accounting methods.

The original assertions were unchanged: late growth must be at most 8 MiB and the fitted Java/native trend at most 128 KiB per cycle. Raw late growth was 2,306,160 bytes; the fitted trend was **226,314.743 bytes/cycle (221.0 KiB)**, so the test remains failed in its original evidence.

## Retention and diagnostic bounds

Whole heaps contain one AdView and four SDK WebViews at both endpoints. Response identity changes from banner 5 at baseline to banner 25 at the endpoint; the previous banners collected. After closing, there are **zero MainActivity instances**, still only banner 25 and four SDK WebViews. The surviving banner is detached, uses Application context and is rooted through an SDK native `HashMap[view] → zzdxj → FrameLayout → AdView`; it does not retain an Activity. This is bounded latest-only retention in the observed runs, not zero retained SDK objects.

Both platform connection pools are empty in all three settled heaps. This confirms cleanup after their observed five-minute keepalive and separates stale socket buffers in the earlier rapid test from persistent app ownership. No other large root owner grew comparably to Inspector. After-close runtime Java/native allocation was 16,485,296 / 30,893,520 bytes; Inspector ownership remained unchanged.

The Inspector owner is SDK singleton `zzeu → zzcrj → zzedp`, with request records and ResponseInfo objects below its maps. SDK 25.4 runtime configuration and published artifact bytecode show a 1,000-record limit that drops new records, plus a 20,971,520-character response-log threshold. The latter is checked before appending, can overshoot one payload and does not cap all metadata. Collection is gated by test mode or explicitly linked Ad Manager debugging. Ordinary non-test, non-debug-linked devices skip this history. Google documents [automatic inspection on test devices](https://support.google.com/admob/answer/13063114?hl=en) and [increased test-device memory use](https://developers.google.com/admob/android/ad-inspector). App production configuration does not enable test-device IDs or invoke Inspector.

## Decision and limits

The original severe leak gate—destroyed hosts and an accumulating banner per navigation—is resolved by the combined rapid-navigation and actual-load evidence. The remaining raw slope failure has a measured diagnostic cause and does not establish continued production Activity/banner accumulation. Accepting this memory fix should retain that explicit exception and the raw failure, rather than raise thresholds or claim an unmodified test pass.

PSS rose 22,682,624 bytes between settled checkpoints. Of that, 15,389,696 appeared between the first heap capture and the first two-load sample; heap capture confounds a clean resident-memory comparison. These snapshots do not prove all PSS growth is capture overhead. Long automatic refresh, server-driven retries, a deliberately delayed callback beyond 60 seconds, mediated inventory and ad-click/return were not covered. No creative was clicked and no private SDK flags or account settings were modified.

Full local evidence remains ignored under `output/release-1.0.12/final-candidate/tablet-settled/`: `settled-comparison.json`, original `evidence.json`, artifact hashes, frozen sources, three HPROFs, per-checkpoint owner sizes, strong dominators, network counts and banner root/identity traces. Final phone evidence is under `output/release-1.0.12/final-candidate/phone/`. Earlier failures remain under `output/release-1.0.12/deferred-load/` and the preceding candidate folders.

## Test fixture scope

The opt-in lifecycle stress fixture is intended for disposable debug profiles. It restores existing settings, reminders, rotation and an existing Daily Hope record; on a profile without an original Daily Hope record, the record created by that run remains. Production installation and focused release navigation do not run this stress fixture.
