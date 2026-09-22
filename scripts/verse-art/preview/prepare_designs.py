"""Build preview specifications only after comparing text against edition sources."""
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
sys.path.insert(0, str(ROOT / 'scripts/daily-verses'))
from sources import load_sources, reference_parts

sources = load_sources(sys.argv[1])
assets = ROOT / 'android/app/src/main/assets/verses'
proofs = json.loads((ROOT / 'scripts/daily-verses/reference-map.json').read_text())
editions = {'bsb':'en-bsb','bbe':'en-bbe','kjv':'en-kjv','web':'en-web',
            'mal1910':'ml-mal1910','rv1909':'es-rv1909','lsg1910':'fr-lsg1910',
            'lut1912':'de-lut1912','riv1927':'it-riv1927','adb1905':'tl-adb1905'}
designs = []

def add(id, edition, font, background, label, group, verse_id='hebrews-10-23', emphasis='', **style):
    asset = editions[edition]
    verses = json.loads((assets / (asset + '.json')).read_text()) + json.loads((assets / 'archive' / (asset + '.json')).read_text())
    verse = next(v for v in verses if v['id'] == verse_id)
    proof = proofs[asset][verse_id]
    book, chapter, first, last = reference_parts(proof['sourceReference'])
    source = ' '.join(sources[asset][book,chapter,n] for n in range(first,last+1))
    assert verse['verse'] == source, (asset,verse_id,'Source mismatch')
    designs.append(dict(id=id,edition=edition,font=font,background=background,label=label,
                        group=group,verseId=verse_id,emphasis=emphasis,verifiedText=source,
                        sourceReference=proof['sourceReference'],
                        textSha256=hashlib.sha256(source.encode()).hexdigest(), **style))

add('en-forest-handlettered','web','bebas_neue','misty-forest','Caveat Brush · faithful','english',emphasis='faithful.',highlightFont='caveatbrush',highlightSize=205)
add('en-wheat-serif','web','bebas_neue','muted-wheat','Caveat Brush · rejoice','english',verse_id='psalm-118-24',emphasis='rejoice',highlightFont='caveatbrush',highlightSize=166)
add('en-flowers-modern','web','bebas_neue','muted-flowers','Caveat Brush · goodness','english',verse_id='nahum-1-7',emphasis='The LORD is good,',highlightFont='caveatbrush',highlightText='The LORD\nis good,',highlightSize=129)
add('en-forest-lora','web','bebas_neue','misty-forest','Caveat Brush · faithful','english',emphasis='faithful.',highlightFont='caveatbrush',highlightSize=180)
add('en-forest-dm-sans','web','dm_sans','misty-forest','Caveat Brush · bold brush','english',emphasis='faithful.',highlightFont='caveatbrush',highlightSize=158,bodySize=33)
add('en-forest-oswald','web','bebas_neue','misty-forest','Caveat Brush · faithful','english',emphasis='faithful.',highlightFont='caveatbrush',highlightSize=148)
add('en-moonlight','web','bebas_neue','moonlit-mist','Caveat Brush · Be still','landscapes',verse_id='psalm-46-10',emphasis='“Be still,',highlightFont='caveatbrush',highlightSize=166)
add('en-mountains','web','bebas_neue','misty-mountains','Caveat Brush · goodness','landscapes',verse_id='nahum-1-7',emphasis='The LORD is good,',highlightFont='caveatbrush',highlightText='The\nLORD\nis good,',highlightSize=104)
add('en-dew-leaf','web','bebas_neue','dark-dew-leaf','Caveat Brush · greenery','landscapes',emphasis='faithful.',highlightFont='caveatbrush',highlightSize=205)
for font,label,bg in [('manjari','Manjari · expressive','misty-forest'),('baloo_chettan','Baloo Chettan 2 · emphasis','muted-flowers'),('noto_malayalam','Noto Sans Malayalam · contrast','moonlit-mist')]:
    add('ml-'+font,'mal1910',font,bg,label,'malayalam',emphasis='വിശ്വസ്തനല്ലോ.',highlightFont=font,highlightSize=104,bodySize=52)
for id,bg,label,emphasis,face,size in [
    ('rv1909','muted-wheat','Spanish · Caveat Brush','fiel es el que prometió:','caveatbrush',99),
    ('lsg1910','muted-flowers','French · Caveat Brush','est fidèle.','caveatbrush',126),
    ('lut1912','misty-forest','German · Caveat Brush','er ist treu,','caveatbrush',133),
    ('riv1927','muted-flowers','Italian · Caveat Brush','fedele','caveatbrush',180),
    ('adb1905','moonlit-mist','Tagalog · Caveat Brush','tapat ang nangako:','caveatbrush',113)]:
    add('language-'+id,id,'bebas_neue',bg,label,'languages',emphasis=emphasis,highlightFont=face,highlightSize=size)
for id,emphasis in [('bsb','faithful.'),('bbe','true'),('kjv','faithful')]:
    add('edition-'+id,id,'bebas_neue','moonlit-mist' if id=='kjv' else 'dark-dew-leaf',id.upper()+' · Caveat Brush','editions',emphasis=emphasis,highlightFont='caveatbrush',highlightSize=190)

photos = json.loads((ROOT / 'android/app/src/main/assets/verse-art-renderer/photo-sources.json').read_text())
for p in photos:
    name = p['name']
    add(name, 'web', 'bebas_neue', name, name.removeprefix('photo-').replace('-', ' ').title(),
        'photo-new' if photos.index(p) >= 10 else 'photo-woodland' if name in ['photo-foggy-pines','photo-shadow-ivy','photo-woodland-deer','photo-forest-mist','photo-warm-highlands'] else 'photo-lakes',
        verse_id=p['verseId'], emphasis=p['emphasis'], highlightFont=p['highlightFont'],
        highlightSize=110, bodySize=40)

add('approved-bright-flowers','web','bebas_neue','bright-wildflowers','Sunlit flowers · Caveat Brush','approved-bright',verse_id='philippians-4-4',emphasis='Rejoice in the Lord always!',highlightFont='caveatbrush',highlightSize=140)
add('approved-bright-meadow','web','bebas_neue','bright-meadow','Green meadow · Caveat Brush','approved-bright',verse_id='psalms-100-2',emphasis='Serve the LORD with gladness.',highlightFont='caveatbrush',highlightSize=145)

# User approved greenery options 1 and 2; preserve the second card's Caveat Brush style.
add('approved-sunlit-ferns','web','bebas_neue','greenery-sunlit-ferns','Sunlit ferns · Caveat Brush','approved-greenery',verse_id='psalm-37-4',emphasis='delight yourself in the LORD,',highlightFont='caveatbrush',highlightSize=140)
add('approved-fresh-leaves','web','bebas_neue','greenery-fresh-leaves','Fresh leaves · Caveat Brush','approved-greenery',verse_id='1-peter-5-7',emphasis='he cares for you.',highlightFont='caveatbrush',highlightSize=164)

out = ROOT / 'android/app/src/androidTest/assets/verse-art-preview/designs.json'
out.write_text(json.dumps(designs,ensure_ascii=False,indent=2)+'\n')
print(f'PASS: {len(designs)} preview specifications copied verbatim from 10 edition sources.')

# Original three-option comparison remains reproducible; woodland clearing is not approved.
start = len(designs)
add('preview-sunlit-ferns','web','bebas_neue','preview-sunlit-ferns','1 · Sunlit ferns','greenery',verse_id='psalm-37-4',emphasis='delight yourself in the LORD,',highlightFont='caveatbrush',highlightSize=140)
add('preview-fresh-leaves','web','bebas_neue','preview-fresh-leaves','2 · Fresh leaves','greenery',verse_id='1-peter-5-7',emphasis='he cares for you.',highlightFont='caveatbrush',highlightSize=164)
add('preview-woodland-clearing','web','bebas_neue','preview-woodland-clearing','3 · Woodland clearing','greenery',verse_id='psalm-121-2',emphasis='My help comes from the LORD,',highlightFont='caveatbrush',highlightSize=145)
(out.parent / 'greenery-designs.json').write_text(json.dumps(designs[start:], ensure_ascii=False, indent=2)+'\n')
