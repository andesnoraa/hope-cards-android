"""Download OFL fonts and pin static instances for the opt-in design preview."""
import hashlib, json, urllib.request
from pathlib import Path
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont
ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / 'android/app/src/androidTest/assets/verse-art-preview/fonts'
OUT.mkdir(parents=True, exist_ok=True)
specs = [
 ('caveatbrush', 'CaveatBrush-Regular.ttf', 'caveatbrush', {}),
 ('leckerlione', 'LeckerliOne-Regular.ttf', 'leckerlione', {}),
 ('lora', 'Lora[wght].ttf', 'lora', {'wght':500}),
 ('dmsans', 'DMSans[opsz,wght].ttf', 'dm_sans', {'wght':600,'opsz':36}),
 ('oswald', 'Oswald[wght].ttf', 'oswald', {'wght':500}),
 ('caveat', 'Caveat[wght].ttf', 'caveat', {'wght':600}),
 ('manjari', 'Manjari-Regular.ttf', 'manjari', {}),
 ('baloochettan2', 'BalooChettan2[wght].ttf', 'baloo_chettan', {'wght':500}),
]
manifest=[]
for family, filename, name, axes in specs:
 listing=json.load(urllib.request.urlopen('https://api.github.com/repos/google/fonts/contents/ofl/'+family))
 item=next(x for x in listing if x['name']==filename)
 raw=urllib.request.urlopen(item['download_url']).read()
 dest=OUT/(name+'.ttf'); dest.write_bytes(raw)
 if axes:
  font=TTFont(dest); instantiateVariableFont(font,axes,inplace=True); font.save(dest)
 license_item=next(x for x in listing if x['name']=='OFL.txt')
 (OUT/(name+'-OFL.txt')).write_bytes(urllib.request.urlopen(license_item['download_url']).read())
 manifest.append({'family':family,'filename':filename,'source':item['download_url'],'sourceGitBlobSha':item['sha'],'downloadSha256':hashlib.sha256(raw).hexdigest(),'instanceAxes':axes,'file':dest.name,'sha256':hashlib.sha256(dest.read_bytes()).hexdigest()})
 print(dest.name, dest.stat().st_size)
(OUT/'sources.json').write_text(json.dumps(manifest,indent=2)+'\n')
