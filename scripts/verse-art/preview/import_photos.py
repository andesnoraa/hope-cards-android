"""Crop/resize original photos before WebP encoding; preserve original photo colors without filters."""
import hashlib,json,re,subprocess,shutil
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
OUT=ROOT/'android/app/src/main/assets/verse-art-renderer'
# name, filename, vertical crop position, verse, exact emphasis, highlight font, text box
PHOTOS=[
 ('alpine-lake','pexels-alizain-hirani-274856182-12905885.jpg',.5,'psalm-121-2','My help comes from the LORD,','caveatbrush',dict(centerX=690,centerY=225,height=320,width=640,ink='#142B36')),
 ('foggy-pines','pexels-amel-uzunovic-440739273-35451886.jpg',.5,'psalm-147-3','He heals','caveatbrush',dict(centerY=470,height=620,width=800,ink='#FFFFFF',shadow=True)),
 ('quiet-reservoir','pexels-cecile-hournau-419900900-15499497.jpg',.5,'psalm-56-3','I will put my trust in you.','caveatbrush',dict(centerY=230,height=330,width=840,ink='#263A36')),
 ('shadow-ivy','pexels-damianapanasowicz-4165348.jpg',.55,'john-15-9','Remain in my love.','caveatbrush',dict(centerY=490,height=620,width=780,ink='#FFF8E9',shadow=True)),
 ('woodland-deer','pexels-davidohboy-6468236.jpg',.5,'psalm-23-1','The LORD is my shepherd;','caveatbrush',dict(centerX=270,centerY=445,height=580,width=430,ink='#FFF8E9',shadow=True)),
 ('still-alpine-lake','pexels-enric-cruz-lopez-6272229.jpg',.5,'philippians-4-13','through Christ','caveatbrush',dict(centerY=245,height=360,width=770,ink='#19313D')),
 ('forest-mist','pexels-jaymantri-4827.jpg',.5,'lamentations-3-23','Great is your faithfulness.','caveatbrush',dict(centerY=210,height=280,width=800,ink='#263A36')),
 ('warm-highlands','pexels-markarronsmith-37022885.jpg',0,'psalm-33-20','our help and our shield.','caveatbrush',dict(centerY=245,height=350,width=820,ink='#302F2A')),
 ('misty-jetty','pexels-simon-s-574087187-30521727.jpg',.5,'psalm-62-5','my expectation is from him.','caveatbrush',dict(centerY=325,height=460,width=830,ink='#FFF8E9',supportingInk='#1D302F',shadow=True)),
 ('turquoise-mist','pexels-theshuttervision-9651387.jpg',.5,'numbers-6-26','give you peace.’','caveatbrush',dict(centerY=230,height=330,width=740,ink='#173D3F')),
 ('shore-waves','pexels-eliezer-20527722.jpg',.75,'psalm-56-3','I will put my trust in you.','caveatbrush',dict(centerY=620,height=380,width=820,ink='#FFF8E9',shadow=True)),
 ('tree-bokeh','pexels-jhonny-mages-2149929716-31006696.jpg',.5,'psalm-33-20','our help and our shield.','caveatbrush',dict(cropX=.90,centerX=650,centerY=610,height=350,width=670,ink='#FFF8E9',shadow=True)),
 ('coastal-dawn','pexels-randa-aulia-3546528-5600480.jpg',.03,'psalm-121-2','My help comes from the LORD,','caveatbrush',dict(centerY=230,height=310,width=820,ink='#193C43')),
 ('quiet-branch','pexels-raymond-petrik-1448389535-28458039.jpg',.5,'1-peter-5-7','he cares for you.','caveatbrush',dict(centerY=265,height=350,width=800,ink='#FFF8E9')),
 ('teal-bark','pexels-wyxina-tresse-311038210-27595107.jpg',.5,'psalm-147-3','He heals','caveatbrush',dict(centerY=450,height=650,width=820,ink='#FFF8E9',shadow=True)),
 ('golden-field','pexels-gerzon-pinata-470468947-32210525.jpg',.24,'psalms-100-2','Serve the LORD with gladness.','caveatbrush',dict(centerY=665,height=300,width=820,ink='#FFF8E9',shadow=True,citationInk='#FFF8E9')),
 ('amber-flower','pexels-helen1-10081300.jpg',.5,'psalm-37-4','delight yourself in the LORD,','caveatbrush',dict(cropX=.20,centerY=430,height=620,width=820,ink='#4B321B',footerInk='#4B321B')),
 ('forest-road','pexels-jacint-bofill-1745787-36501165.jpg',.35,'philippians-4-13','through Christ','caveatbrush',dict(centerY=660,height=280,width=780,ink='#FFF8E9',shadow=True,citationInk='#FFF8E9')),
 ('sunbeam-field','pexels-jplenio-18867528.jpg',.5,'psalm-23-1','The LORD is my shepherd;','caveatbrush',dict(cropX=.30,centerY=200,height=280,width=780,ink='#40351C')),
 ('golden-grass','pexels-katlovessteve-545313.jpg',.5,'psalm-56-3','I will put my trust in you.','caveatbrush',dict(cropX=.78,centerY=275,height=400,width=800,ink='#463019',footerInk='#463019')),
]
profile_file=OUT/'photo-profiles.json'
profiles={k:v for k,v in json.loads(profile_file.read_text()).items() if not k.startswith('photo-')} if profile_file.exists() else {}
manifest=[]
for name,filename,ypos,verse,emphasis,font,box in PHOTOS:
 source=Path.home()/'Downloads'/filename
 info=subprocess.check_output(['sips','-g','pixelWidth','-g','pixelHeight',str(source)],text=True)
 w,h=[int(re.search(key+r': (\d+)',info).group(1)) for key in ('pixelWidth','pixelHeight')]
 side=min(w,h);x=int((w-side)*box.get('cropX',.5));y=int((h-side)*ypos)
 dest=OUT/'backgrounds'/('photo-'+name+'.webp')
 subprocess.run(['cwebp','-quiet','-q','88','-m','6','-metadata','icc','-crop',str(x),str(y),str(side),str(side),'-resize','1080','1080',str(source),'-o',str(dest)],check=True)
 profiles['photo-'+name]=dict(centerX=540,**{k:v for k,v in box.items() if k not in ('centerX','cropX')})
 profiles['photo-'+name]['centerX']=box.get('centerX',540)
 manifest.append(dict(name='photo-'+name,sourceFile=filename,sourceSha256=hashlib.sha256(source.read_bytes()).hexdigest(),crop=[x,y,side,side],outputSize=[1080,1080],webpQuality=88,bytes=dest.stat().st_size,verseId=verse,emphasis=emphasis,highlightFont=font))
# Scene families drive stable gallery ordering, keeping related scenes apart.
families={
 'lake':['alpine-lake','quiet-reservoir','still-alpine-lake','turquoise-mist','misty-jetty','warm-highlands'],
 'forest':['foggy-pines','forest-mist','forest-road'],
 'foliage':['shadow-ivy'], 'wildlife':['woodland-deer'],
 'coast':['shore-waves','coastal-dawn'], 'wood':['tree-bokeh','quiet-branch','teal-bark'],
 'golden':['golden-field','amber-flower','sunbeam-field','golden-grass'],
}
for family,names in families.items():
 for name in names: profiles['photo-'+name]['family']=family
profiles['photo-misty-jetty'].update(ink='#1D302F',supportingInk='#1D302F',shadow=False)
for name,family,ink in [('misty-forest','forest','#FFF8E9'),('moonlit-mist','night','#FFF8E9'),('misty-mountains','forest','#FFF8E9'),('dark-dew-leaf','foliage','#FFF8E9'),('muted-wheat','golden','#403125'),('muted-flowers','flowers','#473445')]:
 profiles[name]=dict(centerX=540,centerY=470,height=660,width=860,ink=ink,footerInk=ink,family=family)
for name,family in [('bright-meadow','flowers'),('bright-wildflowers','flowers'),('greenery-sunlit-ferns','foliage'),('greenery-fresh-leaves','foliage')]:
 profiles[name]['family']=family
# Preserve approved generated replacements when reimporting the original photos.
approved=Path(__file__).parent/'approved-backgrounds'
for name,replacement in json.loads((approved/'overrides.json').read_text()).items():
 shutil.copy2(approved/replacement['asset'],OUT/'backgrounds'/(name+'.webp'))
 profiles[name]=replacement['profile']
 next(item for item in manifest if item['name']==name).update(replacement['metadata'])
(OUT/'photo-profiles.json').write_text(json.dumps(profiles,indent=2)+'\n')
(OUT/'photo-sources.json').write_text(json.dumps(manifest,indent=2,ensure_ascii=False)+'\n')
print('Imported',len(manifest),'photo slots (including approved replacements);',sum(x['bytes'] for x in manifest),'bytes; crop/resize only.')
