#!/usr/bin/env swift
import AppKit
import CoreText
import ImageIO

// Review-only compositions. App pixels come exclusively from real captures.
let root = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let out = URL(fileURLWithPath: CommandLine.arguments.dropFirst().first ?? "/Users/aaronsedna/Desktop/Hope Cards - Store Mockups - 2026-09-23")
let fm = FileManager.default
for name in ["poppins_bold", "poppins_regular", "poppins_semibold", "source_serif_regular", "source_serif_semibold"] {
    CTFontManagerRegisterFontsForURL(root.appendingPathComponent("android/app/src/main/res/font/\(name).ttf") as CFURL, .process, nil)
}
func color(_ hex: UInt32, _ alpha: CGFloat = 1) -> NSColor {
    NSColor(srgbRed: CGFloat((hex >> 16) & 255)/255, green: CGFloat((hex >> 8) & 255)/255, blue: CGFloat(hex & 255)/255, alpha: alpha)
}
let navy = color(0x1C2949), ivory = color(0xFAF5E9), gold = color(0xB38A3D), paleGold = color(0xE8CF91)
var canvasH: CGFloat = 500
func rect(_ x: CGFloat, _ y: CGFloat, _ w: CGFloat, _ h: CGFloat) -> CGRect { CGRect(x: x, y: canvasH-y-h, width: w, height: h) }
func fill(_ c: NSColor, _ x: CGFloat = 0, _ y: CGFloat = 0, _ w: CGFloat = 1024, _ h: CGFloat = 500) { c.setFill(); rect(x,y,w,h).fill() }
func text(_ s: String, _ x: CGFloat, _ y: CGFloat, _ w: CGFloat, _ h: CGFloat, _ size: CGFloat, _ c: NSColor, serif: Bool = false, center: Bool = false, tracking: CGFloat = 0) {
    let p = NSMutableParagraphStyle(); p.alignment = center ? .center : .left; p.lineSpacing = 0
    let name = serif ? "SourceSerif4-SemiBold" : "Poppins-Bold"
    var fittedSize = size
    func attributed(_ pointSize: CGFloat) -> NSAttributedString {
        let font = NSFont(name:name,size:pointSize)!
        return NSAttributedString(string:s, attributes:[.font:font,.foregroundColor:c,.paragraphStyle:p,.kern:tracking])
    }
    while attributed(fittedSize).boundingRect(with:NSSize(width:w,height:10000),options:[.usesLineFragmentOrigin,.usesFontLeading]).height > h && fittedSize > size * 0.75 { fittedSize -= 0.5 }
    let a = attributed(fittedSize)
    a.draw(with:rect(x,y,w,h),options:[.usesLineFragmentOrigin,.usesFontLeading])
}
func imageAt(_ path: String) -> NSImage { NSImage(contentsOf:out.appendingPathComponent(path))! }
func cropped(_ name: String, _ crop: CGRect) -> NSImage {
    let img=imageAt("source-captures/en-US/"+name)
    let cg=img.cgImage(forProposedRect:nil,context:nil,hints:nil)!.cropping(to:crop)!
    return NSImage(cgImage:cg,size:NSSize(width:cg.width,height:cg.height))
}
func draw(_ img: NSImage, _ x:CGFloat, _ y:CGFloat, _ w:CGFloat, _ h:CGFloat, radius:CGFloat=0, shadow:Bool=false, angle:CGFloat=0) {
    NSGraphicsContext.saveGraphicsState()
    let dest=rect(x,y,w,h)
    if angle != 0 {
        let t=AffineTransform(translationByX:dest.midX,byY:dest.midY); (t as NSAffineTransform).concat()
        let r=NSAffineTransform();r.rotate(byDegrees:angle);r.concat()
        let b=NSAffineTransform();b.translateX(by:-dest.midX,yBy:-dest.midY);b.concat()
    }
    let path=NSBezierPath(roundedRect:dest,xRadius:radius,yRadius:radius)
    if shadow {
        NSGraphicsContext.saveGraphicsState()
        let sh=NSShadow();sh.shadowColor=color(0x07141E,0.27);sh.shadowBlurRadius=25;sh.shadowOffset=NSSize(width:0,height:-12);sh.set()
        ivory.setFill();path.fill();NSGraphicsContext.restoreGraphicsState()
    }
    path.addClip();img.draw(in:dest,from:.zero,operation:.sourceOver,fraction:1)
    NSGraphicsContext.restoreGraphicsState()
}
func background(_ name:String, _ w:CGFloat=1024, _ h:CGFloat=500) {
    let img=imageAt("design-source/\(name)-background.png")
    // Aspect-fill backgrounds; never stretch app screenshots.
    let ratio=max(w/img.size.width,h/img.size.height), dw=img.size.width*ratio, dh=img.size.height*ratio
    draw(img,(w-dw)/2,(h-dh)/2,dw,dh)
}
func save(_ name:String,_ w:Int,_ h:Int,body:()->Void) {
    let bmp=NSBitmapImageRep(bitmapDataPlanes:nil,pixelsWide:w,pixelsHigh:h,bitsPerSample:8,samplesPerPixel:4,hasAlpha:true,isPlanar:false,colorSpaceName:.deviceRGB,bytesPerRow:0,bitsPerPixel:0)!
    NSGraphicsContext.saveGraphicsState();NSGraphicsContext.current=NSGraphicsContext(bitmapImageRep:bmp);NSGraphicsContext.current!.imageInterpolation = .high
    canvasH=CGFloat(h);body();NSGraphicsContext.current!.flushGraphics();NSGraphicsContext.restoreGraphicsState()
    let url=out.appendingPathComponent(name);try! fm.createDirectory(at:url.deletingLastPathComponent(),withIntermediateDirectories:true)
    let ctx=CGContext(data:nil,width:w,height:h,bitsPerComponent:8,bytesPerRow:w*4,space:CGColorSpace(name:CGColorSpace.sRGB)!,bitmapInfo:CGImageAlphaInfo.noneSkipLast.rawValue)!
    ctx.draw(bmp.cgImage!,in:CGRect(x:0,y:0,width:w,height:h))
    let dst=CGImageDestinationCreateWithURL(url as CFURL,"public.png" as CFString,1,nil)!
    CGImageDestinationAddImage(dst,ctx.makeImage()!,nil);precondition(CGImageDestinationFinalize(dst));print(name)
}
func cropPath(_ path:String,_ r:CGRect)->NSImage{
 let cg=imageAt(path).cgImage(forProposedRect:nil,context:nil,hints:nil)!.cropping(to:r)!
 return NSImage(cgImage:cg,size:NSSize(width:cg.width,height:cg.height))
}
let appCrop=CGRect(x:0,y:130,width:1080,height:1730)
let deckRect=CGRect(x:135,y:367,width:810,height:1285)
let realDeck=cropped("02-card-front.png",deckRect)
let greenDeck=cropPath("02-modern/source/evergreen/02-card-front.png",deckRect)
let darkDeck=cropPath("02-modern/source/midnight/02-card-front.png",deckRect)
let artA=cropped("08-art-gallery.png",CGRect(x:53,y:485,width:471,height:471))
let artB=cropped("08-art-gallery.png",CGRect(x:556,y:485,width:471,height:471))
let artC=cropped("08-art-gallery.png",CGRect(x:53,y:1146,width:471,height:471))
let artD=cropped("08-art-gallery.png",CGRect(x:556,y:1146,width:471,height:471))
let sourceBG=imageAt("02-modern/source/background-triptych.png").cgImage(forProposedRect:nil,context:nil,hints:nil)!
let third=sourceBG.width/3
let plates=(0..<3).map{ i -> NSImage in
 let cg=sourceBG.cropping(to:CGRect(x:i*third,y:0,width:third,height:sourceBG.height))!
 return NSImage(cgImage:cg,size:NSSize(width:cg.width,height:cg.height))
}
func plate(_ n:Int){draw(plates[n],0,0,1080,1920)}
func native(_ image:NSImage,_ x:CGFloat,_ y:CGFloat,_ w:CGFloat,_ angle:CGFloat=0,_ round:CGFloat=26){
 draw(image,x,y,w,w*image.size.height/image.size.width,radius:round,shadow:true,angle:angle)
}
func header(_ label:String,_ a:String,_ b:String,_ dark:Bool){
 let c=dark ? color(0xFAF9F5):color(0x081932)
 text(label.uppercased(),63,43,950,45,24,c,tracking:2)
 text(a,57,78,970,200,112,c,tracking:-3)
 text(b,57,210,970,200,112,dark ? color(0xFFCB83):c,tracking:-3)
}
let spec:[(String,String,String,String,Int)] = [
 ("01-hope-every-day","Daily Bible verses","A little hope.","Every day.",0),
 ("02-verse-art","Verse Art","Scripture.","Made to share.",1),
 ("03-daily-hope","Daily Hope","Your daily","quiet moment.",2),
 ("04-share-hope","Save & share artwork","Pass a little","hope along.",0),
 ("05-favorites","Your saved verses","Keep what","speaks to you.",1),
 ("06-journal","Your Bible journal","A verse.","Your reflection.",2),
 ("07-themes","Choose your theme","Make it","feel like you.",0)
]
for style in ["spectrum","afterglow"] {
 for (i,s) in spec.enumerated(){
  save("02-modern/screenshots/\(style)/\(s.0).png",1080,1920){
   let bg=style=="afterglow" ? 0:s.4
   plate(bg);header(s.1,s.2,s.3,bg==0)
   switch i {
   case 0:
    // Deck dimensions remain the native 810:1285. Never widen this card.
    native(realDeck,110,425,860,-5,80)
   case 1:
    native(cropped("08-art-gallery.png",appCrop),130,414,880,4,38)
    native(artA,-38,1410,490,-9,38)
    native(artB,650,1540,480,10,38)
   case 2:
    native(cropped("03-daily-hope.png",appCrop),90,410,900,-4,40)
   case 3:
    native(cropped("09-art-detail.png",appCrop),90,420,900,4,40)
    // The image tile remains square; it is a crop of real app content.
    native(artD,720,1000,430,-8,34)
   case 4:
    native(cropped("04-favorites.png",appCrop),80,430,920,-3,38)
   case 5:
    native(cropped("05-journal.png",CGRect(x:0,y:130,width:1080,height:1040)),80,465,930,3,40)
    native(realDeck,523,1270,410,-7,39)
    native(cropped("05-journal.png",CGRect(x:51,y:941,width:979,height:198)),38,1370,785,5,17)
   default:
    native(darkDeck,38,437,500,10,48)
    native(greenDeck,531,460,500,-9,48)
    native(cropped("06-themes.png",CGRect(x:47,y:232,width:986,height:1530)),184,970,740,0,60)
   }
  }
 }
 save("02-modern/SCREENSHOTS-\(style.uppercased()).png",2240,830){
  fill(color(0x101A30),0,0,2240,830)
  text("HOPE CARDS  /  \(style.uppercased())",34,27,2150,75,34,ivory,tracking:-0.6)
  text("Google Play screenshot mockups · Actual app pixels · Original deck proportions",36,88,2150,38,19,color(0xC8D3E8))
  for(i,s) in spec.enumerated(){draw(imageAt("02-modern/screenshots/\(style)/\(s.0).png"),CGFloat(i)*315+25,157,300,533.33,radius:5,shadow:false)}
  text("1080 × 1920  /  ADS SUPPRESSED FOR CAPTURE  /  REVIEW ONLY",36,743,2150,40,18,color(0xC8D3E8),tracking:1)
 }
}
let names=["01-amber-energy","02-sky-optimism","03-coral-pause","04-art-wall","05-fresh-mint"]
for n in names {
 save("02-modern/feature-graphics/\(n).png",1024,500){draw(imageAt("02-modern/imagegen-originals/\(n).png"),0,0,1024,500)}
}
save("02-modern/FIVE-FEATURE-GRAPHICS.png",1600,1510){
 fill(color(0x101A30),0,0,1600,1510)
 text("HOPE CARDS / FIVE DIRECTIONS",53,34,1490,68,38,ivory,tracking:-1)
 text("Google Play feature graphics · 1024 × 500 · Choose your direction",56,110,1480,34,20,color(0xBACBE8))
 for(i,n) in names.enumerated(){
  let x:CGFloat=i==4 ? 420:(i%2==0 ? 50:825),y=CGFloat(i/2)*438+179
  text(n.replacingOccurrences(of:"-",with:" ").uppercased(),x,y,730,34,19,ivory)
  draw(imageAt("02-modern/feature-graphics/\(n).png"),x,y+45,724,353.52,radius:3)
 }
}
