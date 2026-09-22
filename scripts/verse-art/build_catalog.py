"""Rebuild the curated Kotlin catalog and Android artwork-generation manifest."""
import json
from pathlib import Path
root = Path(__file__).resolve().parents[2]
verses = {v['id']: v for v in json.loads((root/'android/app/src/main/assets/verses/en-web.json').read_text())}
backgrounds = {
 'hope': ['reference-stars','reference-forest','reference-canyon','reference-leaf','reference-coast','hope-horizon'],
 'peace': ['peace-ocean','peace-moon','peace-mist','reference-coast','reference-leaf','reference-waterfall'],
 'strength': ['strength-cliffs','reference-canyon','strength-peaks','reference-stars','reference-forest','gratitude-autumn'],
 'joy': ['reference-meadow','joy-blossom','reference-forest','hope-dawn','reference-waterfall','gratitude-wheat'],
 'comfort': ['reference-leaf','reference-bench','comfort-shore','reference-forest','reference-waterfall','strength-forest'],
 'gratitude': ['gratitude-wheat','gratitude-autumn','reference-leaf','reference-meadow','reference-canyon','reference-forest'],
}
# Every current backdrop is intentionally rich and dark for cream lettering.
light = set()
rows=[]
for line in (root/'scripts/verse-art/curation.txt').read_text().splitlines():
 if not line: continue
 if line.startswith('['): category=line[1:-1]; index=0; continue
 verse_id, emphasis=line.split('|'); verse_id=verse_id.lower()
 verse=verses[verse_id]; text=verse['verse'].strip('“”')
 assert emphasis in text, verse_id
 background=backgrounds[category][index%6]
 rows.append(dict(id=f'{category}-{verse_id}',verseId=verse_id,category=category,title=emphasis[0].upper()+emphasis[1:],reference=verse['reference'],text=text,emphasis=emphasis,background=background,light=background in light,style=(index+index//6)%6))
 index+=1
assert len(rows)==171 and len({r['verseId'] for r in rows})==171
assert len({r['text'] for r in rows})==171
j=lambda s:json.dumps(s,ensure_ascii=False).replace('$','\\$')
lines=['package com.aaronsedna.hopecards.model','','// Generated from scripts/verse-art/curation.txt and the bundled WEB Bible.', 'internal fun expandedVerseArt(): List<VerseArtwork> = listOf(']
for r in rows:
 lines.append('    VerseArtwork('+', '.join(j(r[k]) for k in ['id','verseId','title','reference','text','category'])+'),')
lines.append(')')
(root/'android/app/src/main/java/com/aaronsedna/hopecards/model/ExpandedVerseArt.kt').write_text('\n'.join(lines)+'\n')
# Render all original designs from verified text as well, preserving their stable app IDs.
for art_id, verse_id, category, emphasis, background, style in [
 ('peace', 'psalm-46-10', 'peace', 'Be still', 'peace-ocean', 1),
 ('strength', 'nahum-1-7', 'strength', 'The LORD is good', 'reference-canyon', 2),
 ('joy', 'psalm-118-24', 'joy', 'We will rejoice', 'reference-meadow', 0),
 ('peace-safe-at-night', 'psalm-4-8', 'peace', 'In peace', 'peace-moon', 5),
 ('strength-through-christ', 'philippians-4-13', 'strength', 'through Christ', 'strength-cliffs', 0),
 ('comfort-he-cares', '1-peter-5-7', 'comfort', 'he cares for you', 'reference-leaf', 5),
 ('hope-abound', 'romans-15-13', 'hope', 'abound in hope', 'reference-stars', 0),
 ('joy-path-of-life', 'psalm-16-11', 'joy', 'fullness of joy', 'reference-forest', 5),
 ('gratitude-loving-kindness', 'psalm-100-5', 'gratitude', 'His loving kindness', 'gratitude-wheat', 3),
]:
 verse = verses[verse_id]
 rows.append(dict(id=art_id, verseId=verse_id, category=category,
   title=emphasis[0].upper()+emphasis[1:], reference=verse['reference'],
   text=verse['verse'].strip('“”'), emphasis=emphasis, background=background, light=False, style=style))
# Compare all card words to the independent publisher snapshot before rendering.
import re
verified = json.loads((root/'scripts/verse-art/verified-web-verses.json').read_text())
by_id = {v['id']: v for v in verified['verses']}
normalize = lambda text: re.sub(r'[^\w]+', '', text.lower())
assert len(rows) == len(by_id) == 180
for row in rows:
 assert row['reference'] == by_id[row['verseId']]['reference'], row['id']
 assert normalize(row['text']) == normalize(by_id[row['verseId']]['text']), row['id']
manifest=root/'android/app/src/androidTest/assets/verse-art-generation.json'; manifest.parent.mkdir(parents=True,exist_ok=True)
manifest.write_text(json.dumps(rows,ensure_ascii=False,indent=2)+'\n')
print('Generated 171 catalog entries and', len(rows), 'artwork designs')
