"""Produce a self-contained, searchable before/after review in every edition."""
import csv
import json
from pathlib import Path
HERE = Path(__file__).resolve().parent
OUT = HERE.parents[1] / 'docs/daily-verses'
OUT.mkdir(parents=True, exist_ok=True)
rows = json.loads((HERE / 'review.json').read_text())
sources = json.loads((HERE / 'sources.json').read_text())
checks = json.loads((HERE / 'verification.json').read_text())
with (OUT / 'changes.csv').open('w', newline='') as file:
    writer = csv.writer(file)
    writer.writerow(['Change', 'Edition', 'Before reference', 'Before text', 'After reference', 'After text', 'Reason', 'Source'])
    for row in rows:
        for edition, pair in row['editions'].items():
            before, after = pair['before'], pair['after']
            if row['kind'] == 'replacement' or before['verse'] != after['verse'] or before['reference'] != after['reference']:
                writer.writerow([row['kind'], sources[edition]['edition'], before['reference'], before['verse'], after['reference'], after['verse'], row['reason'], sources[edition]['url']])
html = '''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Daily Hope · Verse changes</title>
<style>
:root{color-scheme:light;--ink:#142d44;--muted:#536875;--line:#dce3e6;--paper:#f6f7f5;--accent:#276558}*{box-sizing:border-box}body{margin:0;background:var(--paper);color:var(--ink);font:16px/1.55 system-ui,-apple-system,sans-serif}main{max-width:1160px;margin:auto;padding:44px 24px 80px}a{color:#236054;text-underline-offset:3px}.eyebrow{font-size:12px;font-weight:700;letter-spacing:.14em;text-transform:uppercase;color:var(--accent)}h1{font:500 44px/1.15 Georgia,serif;margin:12px 0 20px}h2{font-size:20px}p{max-width:880px}.lede{font-size:19px;color:var(--muted)}.stats{display:flex;gap:30px;flex-wrap:wrap;margin:28px 0}.stat b{display:block;font-size:26px}.stat span{font-size:13px;color:var(--muted)}.toolbar{position:sticky;top:0;z-index:2;display:grid;grid-template-columns:1.2fr 1fr 1fr;gap:14px;padding:20px 0;background:var(--paper);border-bottom:1px solid var(--line)}label{font-size:12px;font-weight:650;color:var(--muted)}select,input{display:block;width:100%;min-height:44px;border:1px solid #c3cfd3;border-radius:8px;background:white;color:var(--ink);font:15px system-ui;padding:10px;margin-top:6px}.count{color:var(--muted);font-size:14px}.card{background:white;border:1px solid var(--line);border-radius:14px;margin:18px 0;padding:24px}.card h2{margin:0 0 8px}.reason{margin:8px 0 22px;font-size:15px;color:var(--muted)}.pair{display:grid;grid-template-columns:1fr 1fr;gap:28px}.label{font-size:11px;font-weight:750;letter-spacing:.12em;color:var(--muted);text-transform:uppercase}.ref{font-size:17px;font-weight:650;margin:8px 0}.verse{font-size:18px;line-height:1.7;margin:8px 0 14px;overflow-wrap:anywhere}.after{border-left:3px solid #a9c9bd;padding-left:22px}.meta{font-size:12px;color:var(--muted)}details{border:1px solid var(--line);padding:18px 22px;border-radius:12px;margin:24px 0;background:white}summary{cursor:pointer;font-weight:650}li{margin:9px 0}table{width:100%;border-collapse:collapse;font-size:14px}td,th{text-align:left;padding:9px;border-bottom:1px solid var(--line)}.empty{padding:40px;text-align:center}.tag{display:inline-block;border-radius:4px;padding:3px 7px;background:#e9f2ec;font-size:11px;font-weight:650;color:#286050;margin-bottom:12px}@media(max-width:700px){main{padding:26px 16px}h1{font-size:34px}.toolbar,.pair{grid-template-columns:1fr}.toolbar{position:static}.after{padding-left:0;padding-top:20px;border-left:0;border-top:2px solid #a9c9bd}.card{padding:20px}}@media print{.toolbar{position:static}details{display:none}.card{break-inside:avoid}body{background:white}}
</style><main>
<div class="eyebrow">Hope Cards · Scripture review · 22 September 2026</div>
<h1>A thoughtful verse for every day</h1>
<p class="lede">Every replacement, its reason, and the exact before-and-after text. Choose a Bible edition to review the wording in that language.</p>
<div class="stats"><div class="stat"><b>365</b><span>daily cards per edition</span></div><div class="stat"><b>83</b><span>passage replacements</span></div><div class="stat"><b>10</b><span>editions verified</span></div><div class="stat"><b>830</b><span>replacement entries checked</span></div></div>
<p>New passages are selected for hope, comfort, faith, kindness, prayer and encouragement. Complete verses are copied from the named edition, not rewritten or shortened. A few adjacent verses are kept together to preserve their meaning. References follow the edition’s numbering.</p>
<details><summary>Sources, accuracy checks and what changed</summary><ul>
<li>Eight editions were extracted from eBible’s USFX files and compared against its separate verse-per-line exports. Notes and separately marked section headings are excluded; the Scripture wording is retained.</li>
<li>Malayalam Sathyavedapusthakam 1910 was checked against the Free Bible Foundation’s original USFM transcription. Two layout spaces before “Selah” differ; the words agree. Ang Dating Biblia 1905 replacements were additionally checked against separate chapter exports from GetBible.</li>
<li>French Psalm numbering, Malayalam Philippians 1:4 (corresponding to English 1:6), and the closing blessing of 2 Corinthians in French, Spanish and Italian are corrected. German references follow Luther numbering, checked against CrossWire’s mapping, rather than eBible’s English-numbered source keys.</li>
<li>Formatting changes are limited to layout whitespace, paragraph marks, and an explicit non-verse colophon/numbering annotation. German Psalm 46 displays verse 2 without its separately numbered verse-1 superscription. No biblical wording is paraphrased.</li>
<li>The 83 retired IDs remain available to saved favorites, journals, existing notifications and all 180 Verse Art designs. They are excluded from the new random/daily selection.</li>
<li>“Text / reference corrections” shows the existing passages corrected to match source wording, punctuation or numbering. The before column is the old app text, which can contain errors.</li>
</ul><div id="sources"></div><p><a href="changes.csv" download>Download the complete change list (CSV)</a></p></details>
<div class="toolbar"><label>Bible edition<select id="edition" aria-label="Bible edition"></select></label><label>Show<select id="kind" aria-label="Change type"><option value="replacement">Passage replacements</option><option value="source-correction">Text / reference corrections</option><option value="all">All changes</option></select></label><label>Find a verse<input id="query" type="search" placeholder="Reference, wording or reason" aria-label="Search changes"></label></div>
<p class="count" id="count" aria-live="polite"></p><div id="results"></div>
<script>const rows=__ROWS__,sources=__SOURCES__,checks=__CHECKS__;
const el=id=>document.getElementById(id),escape=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
for(const [id,s] of Object.entries(sources)){const option=document.createElement('option');option.value=id;option.textContent=s.edition;el('edition').append(option)}
el('sources').innerHTML='<table><thead><tr><th>Edition / source download</th><th>Active cards</th><th>Longest card</th></tr></thead><tbody>'+Object.entries(sources).map(([id,s])=>`<tr><td><a href="${escape(s.url)}">${escape(s.edition)}</a></td><td>365</td><td>${checks[id].maxCharacters} characters</td></tr>`).join('')+'</tbody></table>';
function render(){const edition=el('edition').value,kind=el('kind').value,q=el('query').value.toLocaleLowerCase();const visible=rows.filter(row=>{const {before:b,after:a}=row.editions[edition];return(kind==='all'||kind===row.kind)&&(row.kind==='replacement'||b.verse!==a.verse||b.reference!==a.reference)&&[b.reference,a.reference,b.verse,a.verse,row.reason].join(' ').toLocaleLowerCase().includes(q)});el('count').textContent=`${visible.length} changes · ${sources[edition].edition}`;el('results').innerHTML=visible.map((r,i)=>{const {before:b,after:a}=r.editions[edition];return `<article class="card"><span class="tag">${r.kind==='replacement'?'Passage replacement':'Source correction'}</span><h2>${i+1}. ${escape(b.reference)} → ${escape(a.reference)}</h2><p class="reason">${escape(r.reason)}</p><div class="pair"><div><div class="label">Before · old app text</div><div class="ref">${escape(b.reference)}</div><p class="verse">${escape(b.verse)}</p><div class="meta">${[...b.verse].length} characters</div></div><div class="after"><div class="label">After · source-checked text</div><div class="ref">${escape(a.reference)}</div><p class="verse">${escape(a.verse)}</p><div class="meta">${[...a.verse].length} characters · <a href="${escape(sources[edition].url)}">Edition source</a></div></div></div></article>`}).join('')||'<p class="empty">No changes match your search.</p>'}
for(const id of ['edition','kind','query'])el(id).addEventListener('input',render);render();</script></main></html>'''
for token, data in [('__ROWS__',rows),('__SOURCES__',sources),('__CHECKS__',checks)]:
    html=html.replace(token,json.dumps(data,ensure_ascii=False).replace('</','<\\/'))
(OUT/'review.html').write_text(html)
# A plain Markdown version makes the entire editorial rationale easy to review in git.
lines=['# Daily Hope passage replacements', '', '83 replacements applied consistently across 10 Bible editions. All quoted text below is Berean Standard Bible; open review.html for full before/after text in every edition.', '']
for index,row in enumerate((r for r in rows if r['kind']=='replacement'),1):
    b,a=row['editions']['en-bsb'].values()
    lines += [f'## {index}. {b["reference"]} → {a["reference"]}', '', row['reason'], '', '**Before:** '+b['verse'], '', '**After (BSB):** '+a['verse'], '']
(OUT/'replacements.md').write_text('\n'.join(lines))
print('Wrote review.html, changes.csv and replacements.md')
