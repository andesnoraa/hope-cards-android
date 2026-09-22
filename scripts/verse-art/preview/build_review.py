"""Create a local, self-contained review page for the native-rendered cards."""
import html
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / 'output/verse-art-design-preview'
specs = json.loads((ROOT / 'android/app/src/androidTest/assets/verse-art-preview/designs.json').read_text())
reports = {r['id']: r for r in json.loads((OUT / 'render-report.json').read_text())}
fonts = dict(leckerlione='Leckerli One', caveatbrush='Caveat Brush', oleoscript='Oleo Script Bold', lora='Lora Medium', dm_sans='DM Sans Semibold', oswald='Oswald Medium',
             manjari='Manjari Regular', baloo_chettan='Baloo Chettan 2 Medium',
             noto_malayalam='Noto Sans Malayalam Regular',kaushan_script='Kaushan Script',
             permanent_marker='Permanent Marker',bebas_neue='Bebas Neue',knewave='Knewave',
             berkshireswash='Berkshire Swash')
languages = dict(web='English', bsb='English', bbe='English', kjv='English', mal1910='Malayalam',
                 rv1909='Spanish', lsg1910='French', lut1912='German', riv1927='Italian', adb1905='Tagalog')
cards = []
for d in specs:
    r = reports[d['id']]
    font = fonts[d['highlightFont']] + (' + ' + fonts[d['font']] if d['highlightFont'] != d['font'] else ' · contrasting sizes')
    esc = html.escape
    cards.append(f'''<article data-language="{languages[d['edition']]}">
      <a href="{d['id']}.jpg" target="_blank"><img loading="lazy" src="{d['id']}.webp" width="1080" height="1080" alt="{esc(d['verifiedText'])}"></a>
      <div class="caption"><h2>{esc(d['label'])}</h2><p>{languages[d['edition']]} · {esc(r['reference'])}</p>
      <p class="font">{font}</p><div class="links"><a href="{d['id']}.jpg" download>JPEG · {round(r['jpegBytes']/1024)} KB</a>
      <a href="{d['id']}.webp" download>WebP · {round(r['webpBytes']/1024)} KB</a></div>
      <details><summary>Exact verse text</summary><p lang="{'ml' if d['edition']=='mal1910' else 'und'}">{esc(d['verifiedText'])}</p></details></div></article>''')
buttons = ''.join(f'<button type="button" data-filter="{l}" aria-pressed="{str(l=="All").lower()}">{l}</button>' for l in ['All',*dict.fromkeys(languages.values())])
page = '''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Hope Cards · Verse image review</title><style>
*{box-sizing:border-box}body{margin:0;background:#f6f2ec;color:#24362e;font-family:system-ui,sans-serif;line-height:1.5}
header,main{max-width:1440px;margin:auto;padding:32px}header{padding-bottom:12px}h1{font-family:Georgia,serif;font-size:clamp(32px,4vw,52px);font-weight:400;margin:8px 0}
.intro{max-width:850px;color:#556259}.eyebrow{font-size:12px;letter-spacing:.16em;text-transform:uppercase}.filters{display:flex;flex-wrap:wrap;gap:8px;margin-top:24px}
button{font:inherit;background:transparent;color:#24362e;border:1px solid #b7bfb7;padding:8px 16px;border-radius:24px;cursor:pointer}button[aria-pressed=true]{color:#fff;background:#274638;border-color:#274638}
main{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:26px}article{background:#fff;border:1px solid #e0e4dc;border-radius:10px;overflow:hidden}article[hidden]{display:none}
img{display:block;width:100%;height:auto}h2{font-size:17px;margin:0}.caption{padding:18px}.caption p{margin:6px 0;font-size:14px}.font{color:#607063}.links{display:flex;flex-wrap:wrap;gap:18px;margin:14px 0}a{color:#275c44}details{font-size:13px;color:#536259}summary{cursor:pointer}details p{line-height:1.8}
footer{max-width:1440px;margin:auto;padding:0 32px 40px;color:#687369;font-size:13px}@media(max-width:1000px){main{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:620px){main{grid-template-columns:1fr}header,main{padding:20px}}
</style><header><div class="eyebrow">Hope Cards · Artistic collection</div><h1>Scripture in expressive lettering.</h1>
<p class="intro">{CARD_COUNT} redesigned cards across seven languages and ten Bible editions. Brush lettering, distinctive display fonts, and carefully spaced supporting lines over quiet nature backgrounds. Every card keeps the full verse and a subtle Hope Cards wordmark at the bottom right. Tap an image to inspect the full-size JPEG.</p>
<div class="filters">''' + buttons + '</div></header><main>' + '\n'.join(cards) + '''</main>
<footer>Original backgrounds and font licences are saved with this project. The Android gallery now renders artwork locally using the selected Bible edition and a subtle text-only watermark at the bottom right.</footer>
<script>document.querySelectorAll('[data-filter]').forEach(button=>button.addEventListener('click',()=>{const selected=button.dataset.filter;document.querySelectorAll('[data-filter]').forEach(b=>b.setAttribute('aria-pressed',String(b===button)));document.querySelectorAll('article').forEach(card=>card.hidden=selected!=='All'&&card.dataset.language!==selected)}));</script></html>'''
(OUT / 'index.html').write_text(page.replace('{CARD_COUNT}', str(len(specs))))
(OUT / 'designs.json').write_text(json.dumps(specs,ensure_ascii=False,indent=2)+'\n')
print(OUT / 'index.html')
