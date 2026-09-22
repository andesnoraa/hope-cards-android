"""List the actual reusable WebPs in a scene-separated local review gallery."""
import collections,html,json,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
source=ROOT/'android/app/src/main/assets/verse-art-renderer'
out=ROOT/'output/verse-art-background-library';out.mkdir(parents=True,exist_ok=True)
profiles=json.loads((source/'photo-profiles.json').read_text())
photos={p['name']:p for p in json.loads((source/'photo-sources.json').read_text())}
remaining=list(profiles);ordered=[]
while remaining:
 counts=collections.Counter(profiles[n]['family'] for n in remaining)
 def score(n):
  family=profiles[n]['family']
  penalty=sum((1000 if i==0 else 350 if i==1 else 40) for i,x in enumerate(ordered[-4:][::-1]) if profiles[x]['family']==family)
  return penalty,-counts[family],n
 n=min(remaining,key=score);remaining.remove(n);ordered.append(n)
items=[];cards=[]
for i,n in enumerate(ordered):
 file=n+'.webp';shutil.copy2(source/'backgrounds'/file,out/file)
 label=n.removeprefix('photo-').replace('-',' ').title()
 size=(out/file).stat().st_size
 items.append(dict(number=i+1,id=n,label=label,file=file,family=profiles[n]['family'],bytes=size,source=photos.get(n,{}).get('sourceFile','Previously approved generated background')))
 cards.append(f'<article><a href="{file}"><img loading="lazy" src="{file}" alt="{label}" width="1080" height="1080"></a><h2>{i+1}. {label}</h2><p>{size/1024:.0f} KB · 1080 × 1080</p><a href="{file}" download>Download WebP</a></article>')
(out/'backgrounds.json').write_text(json.dumps(items,indent=2)+'\n')
(out/'index.html').write_text('''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Hope Cards · Background library</title><style>*{box-sizing:border-box}body{max-width:1480px;margin:30px auto;padding:24px;background:#f6f3ec;color:#243b30;font:16px/1.5 system-ui}h1{font-size:38px}main{display:grid;grid-template-columns:repeat(auto-fit,minmax(270px,1fr));gap:24px}article{background:white;padding:12px}img{width:100%;height:auto;display:block}h2{font-size:18px}a{color:#245d40}</style><h1>'''+str(len(items))+''' reusable backgrounds</h1><p>Eighteen original photographs, the approved Quiet Branch replacement, and ten existing backgrounds. Original photos are cropped and resized, without washout filters or color changes. Tap any image to inspect it at full size.</p><p>Use the numbers below when choosing replacements.</p><main>'''+''.join(cards)+'</main></html>')
print(f'{len(items)} backgrounds; {sum(x["bytes"] for x in items):,} bytes total.')
