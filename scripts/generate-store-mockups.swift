#!/usr/bin/env swift
import AppKit
import CoreText
import ImageIO

// Review-only compositions. App pixels come exclusively from real captures.
let root = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
let out = URL(fileURLWithPath: CommandLine.arguments.dropFirst().first ?? "/Users/aaronsedna/Desktop/Hope Cards - Store Mockups - 2026-09-23")
let fm = FileManager.default
for name in ["poppins_regular", "poppins_semibold", "source_serif_regular", "source_serif_semibold"] {
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
    let name = serif ? "SourceSerif4-SemiBold" : "Poppins-Regular"
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
let appCrop=CGRect(x:0,y:130,width:1080,height:1730)
let card=cropped("02-card-front.png",CGRect(x:135,y:367,width:810,height:1285))
let daily=cropped("03-daily-hope.png",appCrop)
let gallery=cropped("08-art-gallery.png",appCrop)
let art1=cropped("08-art-gallery.png",CGRect(x:53,y:485,width:471,height:471))
let art2=cropped("08-art-gallery.png",CGRect(x:556,y:485,width:471,height:471))
let art3=cropped("08-art-gallery.png",CGRect(x:53,y:1146,width:471,height:471))
let art4=cropped("08-art-gallery.png",CGRect(x:556,y:1146,width:471,height:471))
func brand(_ c:NSColor, _ y:CGFloat=76) { text("H O P E   C A R D S",68,y,440,30,17,c) }
func accent(_ c:NSColor,_ y:CGFloat=330){fill(c,70,y,46,2)}
save("feature-graphics/01-morning-light.png",1024,500) {
    background("ivory");brand(gold)
    text("A little hope.\nEvery day.",65,126,485,158,58,navy,serif:true)
    text("Daily Bible verses for the moments\nthat matter.",70,292,445,64,20,navy)
    accent(gold,374);text("DRAW  ·  REFLECT  ·  KEEP",70,397,450,28,13,navy,tracking:1)
    draw(daily,574,105,200,320.37,radius:10,shadow:true,angle:9)
    draw(card,713,56,231,366.46,radius:19,shadow:true,angle:-7)
}
save("feature-graphics/02-evergreen.png",1024,500) {
    background("evergreen");brand(paleGold)
    text("Keep Scripture\nclose.",66,132,490,154,56,ivory,serif:true)
    text("Verses to read, save and share.",70,301,470,35,20,ivory)
    accent(paleGold,364);text("FAITH FOR EVERYDAY LIFE",70,389,450,28,13,paleGold,tracking:1)
    draw(cropped("02-card-front.png",appCrop),674,47,246,394.05,radius:12,shadow:true,angle:-5)
    draw(art1,542,240,184,184,radius:12,shadow:true,angle:8)
}
save("feature-graphics/03-quiet-dawn.png",1024,500) {
    background("dawn");fill(color(0xFFF5E6,0.15))
    NSGradient(starting:color(0xFFF5E6,0.8),ending:color(0xFFF5E6,0))!.draw(in:rect(0,0,690,500),angle:0)
    brand(navy)
    text("Begin with\nhope.",65,131,505,155,62,navy,serif:true)
    text("A daily moment in God’s Word.",70,306,490,36,20,navy)
    accent(gold,367);text("DAILY VERSES  ·  GENTLE REMINDERS",70,391,490,28,12,navy,tracking:0.8)
    draw(daily,686,49,245,392.45,radius:12,shadow:true,angle:-4)
}
save("feature-graphics/04-verse-gallery.png",1024,500) {
    fill(color(0xF1EBDC));brand(gold)
    text("Beautiful verses.\nMade to share.",65,132,492,150,48,navy,serif:true)
    text("Find encouragement in Verse Art.",70,299,495,36,19,navy)
    accent(gold,365);text("HOPE  ·  PEACE  ·  STRENGTH",70,391,490,28,13,navy,tracking:0.8)
    draw(art1,597,43,179,179,radius:13,shadow:true,angle:5)
    draw(art2,795,60,178,178,radius:13,shadow:true,angle:-5)
    draw(art3,589,247,179,179,radius:13,shadow:true,angle:-5)
    draw(art4,789,259,179,179,radius:13,shadow:true,angle:5)
}
save("feature-graphics/05-midnight-gold.png",1024,500) {
    fill(color(0x152740))
    let gradient=NSGradient(starting:color(0x203D52),ending:color(0x15243E))!;gradient.draw(in:rect(0,0,1024,500),angle:15)
    color(0xD8B979,0.23).setStroke()
    for offset in [0.0,34.0,68.0] { let p=NSBezierPath(ovalIn:rect(564-offset,36-offset,390+offset*2,390+offset*2));p.lineWidth=0.6;p.stroke() }
    brand(paleGold)
    text("A quiet moment.\nA lasting word.",65,132,516,148,50,ivory,serif:true)
    text("Scripture to return to, day after day.",70,302,525,35,19,ivory)
    accent(paleGold,368);text("SAVE VERSES  ·  WRITE REFLECTIONS",70,393,500,27,12,paleGold,tracking:0.7)
    draw(card,686,55,243,385.5,radius:20,shadow:true,angle:-5)
}
let specs:[(String,String,String,String)] = [
    ("01-daily-bible-verses","02-card-front.png","A little hope.\nEvery day.","Draw a Bible verse. Take a quiet moment."),
    ("02-verse-art","08-art-gallery.png","Beautiful verses.\nMade to share.","Explore Scripture through Verse Art."),
    ("03-daily-hope","03-daily-hope.png","Begin your day\nwith Scripture.","A fresh Daily Hope verse to reflect on."),
    ("04-save-and-share","09-art-detail.png","Pass a little\nhope along.","Save verse artwork or share it with someone."),
    ("05-favorites","04-favorites.png","Keep the words\nyou need.","Save meaningful verses to your favorites."),
    ("06-journal","05-journal.png","Make space\nfor reflection.","Keep your thoughts beside the verse."),
    ("07-themes","06-themes.png","A quiet space.\nYour own style.","Choose a theme that feels like you.")
]
for style in ["morning-light","evergreen"] {
    for (i,spec) in specs.enumerated() {
        save("screenshots/\(style)/\(spec.0).png",1080,1920) {
            let dark=style == "evergreen", fg=dark ? ivory:navy, muted=dark ? paleGold:gold
            background(dark ? "evergreen":"ivory",1080,1920)
            text("H O P E   C A R D S",86,39,790,43,24,muted)
            text(String(format:"%02d",i+1),917,39,80,43,24,muted,center:true)
            text(spec.2,81,99,935,211,83,fg,serif:true)
            text(spec.3,86,313,940,53,27,fg)
            let screen=cropped(spec.1,appCrop)
            draw(screen,90,398,900,1441.67,radius:22,shadow:true)
            fill(muted,495,1878,90,3)
        }
    }
}
let options=["01-morning-light","02-evergreen","03-quiet-dawn","04-verse-gallery","05-midnight-gold"]
let labels=["01 / MORNING LIGHT","02 / EVERGREEN","03 / QUIET DAWN","04 / VERSE GALLERY","05 / MIDNIGHT GOLD"]
save("FEATURE-GRAPHICS-5-OPTIONS.png",1600,1510) {
    fill(color(0xEEECE5),0,0,1600,1510)
    text("Hope Cards",56,37,1450,73,49,navy,serif:true)
    text("Five feature graphic directions  /  Real app captures  /  Review mockups",59,108,1480,40,21,navy)
    for i in 0..<5 {
        let x:CGFloat=i==4 ? 418:(i%2==0 ? 54:827), y=CGFloat(i/2)*437+183
        text(labels[i],x,y,720,32,18,navy,tracking:1)
        draw(imageAt("feature-graphics/\(options[i]).png"),x,y+44,720,351.56,radius:5,shadow:true)
    }
}
for style in ["morning-light","evergreen"] {
    save("SCREENSHOTS-\(style.uppercased()).png",1890,670) {
        fill(color(0xEEECE5),0,0,1890,670)
        text("Hope Cards / \(style == "morning-light" ? "Morning Light":"Evergreen")",28,20,1800,60,36,navy,serif:true)
        text("Seven real app screenshots · Suggested store order",30,83,1800,33,19,navy)
        for (i,spec) in specs.enumerated() {draw(imageAt("screenshots/\(style)/\(spec.0).png"),CGFloat(i)*264+28,137,250,444.44,radius:4,shadow:true)}
        text("REVIEW MOCKUPS  /  ENGLISH  /  ADS SUPPRESSED DURING CAPTURE",30,620,1810,30,16,navy,tracking:1)
    }
}
