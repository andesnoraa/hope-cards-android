"""Review the regenerated collection, preserving the excluded complete verses for audit."""
import collections,html,json,re,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];out=ROOT/'output/verse-art-regenerated'
items=json.loads((out/'manifest.json').read_text());excluded=json.loads((out/'excluded-long-verses.json').read_text())
# Recheck the exact complete wording against the respective edition export.
sys.path.insert(0,str(ROOT/'scripts/daily-verses'))
from sources import load_sources,reference_parts
sources=load_sources('/tmp/hope-verse-sources');proofs=json.loads((ROOT/'scripts/daily-verses/reference-map.json').read_text())
editions={'bsb':'en-bsb','bbe':'en-bbe','kjv':'en-kjv','web':'en-web','mal1910':'ml-mal1910','rv1909':'es-rv1909','lsg1910':'fr-lsg1910','lut1912':'de-lut1912','riv1927':'it-riv1927','adb1905':'tl-adb1905'}
ids={}
for file in ['VerseArt.kt','ExpandedVerseArt.kt']:
 ids.update(re.findall(r'VerseArtwork\("([^"]+)", "([^"]+)"', (ROOT/'android/app/src/main/java/com/aaronsedna/hopecards/model'/file).read_text()))
for item in items:
 asset=editions[item['edition']];proof=proofs[asset][ids[item['id']]];book,ch,first,last=reference_parts(proof['sourceReference'])
 expected=' '.join(sources[asset][book,ch,n] for n in range(first,last+1))
 # Preserve the already cross-checked edition numbering/heading rules from daily-verses/build.py.
 if asset=='de-lut1912' and (book,ch,first)==('PSA',46,1):
  expected=expected.removeprefix('Ein Lied der Kinder Korah, von der Jugend, vorzusingen. ')
 if asset=='it-riv1927':expected=expected.removeprefix('(G13-13) ')
 if asset=='es-rv1909' and (book,ch,first)==('2CO',13,13):expected=expected.removesuffix(' La segunda Epístola á los Corintios fué enviada de Filipos de Macedonia con Tito y Lucas.')
 assert item['text']==expected,(asset,item['id'])
 assert (out/item['edition']/(item['id']+'.jpg')).exists()
kept=collections.Counter(x['edition'] for x in items);removed=collections.Counter(x['edition'] for x in excluded)
style='<meta name="viewport" content="width=device-width,initial-scale=1"><style>body{max-width:1400px;margin:30px auto;padding:20px;background:#f5f3ed;color:#233b30;font:16px system-ui}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:24px}img{width:100%;height:auto}article{background:white;padding:12px}p{line-height:1.5}a{color:#24533f}</style>'
links=[]
for edition,count in kept.items():
 cards=[]
 for x in (x for x in items if x['edition']==edition):
  ref=html.escape(x['reference']);name=x['id']+'.jpg'
  cards.append(f'<article><a href="{name}"><img loading="lazy" src="{name}" width="1080" height="1080" alt="{ref}"></a><p>{ref}</p><details><summary>Complete verse</summary><p>{html.escape(x["text"])}</p></details></article>')
 (out/edition/'index.html').write_text('<!doctype html><meta charset="utf-8">'+style+f'<a href="../index.html">All editions</a><h1>{edition.upper()} · {count} verse images</h1><main>'+''.join(cards)+'</main>')
 links.append(f'<li><a href="{edition}/index.html">{edition.upper()}: {count} cards</a> · {removed[edition]} lengthy entries omitted</li>')
(out/'index.html').write_text('<!doctype html><meta charset="utf-8">'+style+f'<h1>{len(items):,} regenerated verse images</h1><p>Complete, source-verified verses. Upright hand-lettered/display fonts. Subtle bottom-right Hope Cards wordmark. Select an edition to review its collection.</p><ul>'+''.join(links)+'</ul><p><a href="excluded-long-verses.md">Length selection report</a></p>')
lines=['# Verse image length selection','',f'{len(items):,} complete cards retained across 10 editions; {len(excluded)} lengthy edition-specific entries omitted from the sharing gallery. No Bible text was edited, paraphrased, or abbreviated. The source verses remain in the app.','', 'Limit: 130 characters and 22 words for Latin scripts; 140 Unicode code units and 16 words for Malayalam. Category counts follow the selected edition.','', '| Edition | Cards | Omitted |','|---|---:|---:|']
lines += [f'| {e.upper()} | {kept[e]} | {removed[e]} |' for e in kept]
for e in kept:
 lines += ['',f'## {e.upper()}','', '| Reference | Characters | Words | Reason |','|---|---:|---:|---|']
 for x in (x for x in excluded if x['edition']==e):
  lines.append(f'| {x["reference"]} | {x["characters"]} | {x["words"]} | Too long for the card readability limits; complete text preserved in excluded-long-verses.json. |')
(out/'excluded-long-verses.md').write_text('\n'.join(lines)+'\n')
(out/'verification.json').write_text(json.dumps(dict(images=len(items),editions=len(kept),excluded=len(excluded),counts=kept,sourceComparison='Every rendered complete verse matches its corresponding cached Bible edition source export, retaining the documented Luther Psalm-heading/numbering and Italian/Spanish non-verse annotation rules.',fonts='Upright Caveat Brush / Bebas Neue; Bebas Neue/DM Sans supporting text. Malayalam uses Manjari/Baloo Chettan 2/Noto Sans Malayalam.',watermark='Hope Cards text, bottom right, 22px / 60% opacity at 1080px.'),indent=2)+'\n')
print(f'PASS: {len(items)} images, {len(kept)} editions; all exact texts source-verified. {len(excluded)} long entries excluded from gallery.')
