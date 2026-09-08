#!/usr/bin/env swift

import AppKit
import CoreText
import Foundation

private let canvasWidth = 1920
private let canvasHeight = 1080
private let canvas = NSRect(x: 0, y: 0, width: canvasWidth, height: canvasHeight)

guard CommandLine.arguments.count == 4 else {
    fputs("Usage: localize-supplied-promo.swift <locale> <source-frame-directory> <output-directory>\n", stderr)
    exit(2)
}

private let locale = CommandLine.arguments[1]
private let sourceDirectory = URL(fileURLWithPath: CommandLine.arguments[2], isDirectory: true)
private let outputDirectory = URL(fileURLWithPath: CommandLine.arguments[3], isDirectory: true)
try FileManager.default.createDirectory(at: outputDirectory, withIntermediateDirectories: true)

private let projectRoot = URL(fileURLWithPath: FileManager.default.currentDirectoryPath, isDirectory: true)
private let fontDirectory = projectRoot.appendingPathComponent("android/app/src/main/res/font", isDirectory: true)
for fontName in [
    "poppins_regular.ttf",
    "poppins_semibold.ttf",
    "poppins_bold.ttf",
    "noto_sans_malayalam_regular.ttf",
    "noto_sans_malayalam_semibold.ttf",
    "noto_sans_malayalam_bold.ttf"
] {
    CTFontManagerRegisterFontsForURL(fontDirectory.appendingPathComponent(fontName) as CFURL, .process, nil)
}

private let navy = NSColor(calibratedRed: 0.055, green: 0.118, blue: 0.245, alpha: 1)
private let gold = NSColor(calibratedRed: 0.74, green: 0.52, blue: 0.10, alpha: 1)
private let gray = NSColor(calibratedRed: 0.34, green: 0.38, blue: 0.43, alpha: 1)

private struct Copy {
    let scene1Title: String
    let scene1Body: String
    let scene2Title: String
    let scene2Body: String
    let scene3Title: String
    let scene3Body: String
    let scene4Title: String
    let scene4Body: String
    let scene5Body: String
    let scene5Tagline: String
    let googlePlay: String
}

private let localizedCopy: [String: Copy] = [
    "de-DE": Copy(
        scene1Title: "Ein ruhiger Moment\nfür jeden Tag",
        scene1Body: "Ein täglicher Bibelvers und ein Moment zum Innehalten –\ngenau zur richtigen Zeit.",
        scene2Title: "Zieh eine Karte.\nNimm Hoffnung mit.",
        scene2Body: "Entdecke einen Vers für diesen Moment.\nSpeichere oder teile ihn.",
        scene3Title: "Innehalten. Nachdenken.\nFesthalten, was zählt.",
        scene3Body: "Bewahre deine Gedanken direkt bei dem Vers auf,\nder dich berührt hat.",
        scene4Title: "Ganz nach Wunsch",
        scene4Body: "Wähle deine Bibelübersetzung und stelle\neine sanfte tägliche Erinnerung ein.",
        scene5Body: "Ein ruhiger Ort für Gottes Wort,\nGedanken und Hoffnung.",
        scene5Tagline: "TÄGLICHE BIBELVERSE  •  STILLE MOMENTE",
        googlePlay: "Bei Google Play erhältlich"
    ),
    "fr-FR": Copy(
        scene1Title: "Un moment de calme\nchaque jour",
        scene1Body: "Un verset chaque jour et un temps de réflexion,\nau moment où vous en avez besoin.",
        scene2Title: "Tirez une carte.\nEmportez l’espérance.",
        scene2Body: "Découvrez un verset pour l’instant présent,\npuis gardez-le ou partagez-le.",
        scene3Title: "Faites une pause. Réfléchissez.\nNotez ce qui compte.",
        scene3Body: "Gardez une note liée au verset\nqui vous a inspiré.",
        scene4Title: "À votre manière",
        scene4Body: "Choisissez votre traduction de la Bible et réglez\nun rappel quotidien tout en douceur.",
        scene5Body: "Un espace paisible pour lire, réfléchir\net retrouver l’espérance.",
        scene5Tagline: "VERSET DU JOUR  •  RÉFLEXION PAISIBLE",
        googlePlay: "Disponible sur Google Play"
    ),
    "it-IT": Copy(
        scene1Title: "Un momento di calma\nogni giorno",
        scene1Body: "Un versetto al giorno e uno spazio per riflettere,\nproprio quando ne hai bisogno.",
        scene2Title: "Pesca una carta.\nPorta con te la speranza.",
        scene2Body: "Scopri un versetto per questo momento,\npoi salvalo o condividilo.",
        scene3Title: "Fermati. Rifletti.\nAnnota ciò che conta.",
        scene3Body: "Conserva una nota insieme al versetto\nche ti ha ispirato.",
        scene4Title: "A modo tuo",
        scene4Body: "Scegli la traduzione della Bibbia e imposta\nun delicato promemoria quotidiano.",
        scene5Body: "Un luogo sereno per leggere, riflettere\ne ritrovare la speranza.",
        scene5Tagline: "VERSETTO DEL GIORNO  •  RIFLESSIONE",
        googlePlay: "Disponibile su Google Play"
    ),
    "es-419": Copy(
        scene1Title: "Un momento de calma\ncada día",
        scene1Body: "Un versículo diario y un espacio para reflexionar,\njusto cuando lo necesitas.",
        scene2Title: "Saca una tarjeta.\nLleva esperanza contigo.",
        scene2Body: "Encuentra un versículo para este momento;\nluego guárdalo o compártelo.",
        scene3Title: "Haz una pausa. Reflexiona.\nAnota lo que importa.",
        scene3Body: "Guarda una nota junto al versículo\nque te inspiró.",
        scene4Title: "A tu manera",
        scene4Body: "Elige una traducción de la Biblia y configura\nun recordatorio diario a la hora que prefieras.",
        scene5Body: "Un lugar tranquilo para leer, reflexionar\ny volver a la esperanza.",
        scene5Tagline: "VERSÍCULO DIARIO  •  REFLEXIÓN TRANQUILA",
        googlePlay: "Disponible en Google Play"
    ),
    "fil-PH": Copy(
        scene1Title: "Isang tahimik na sandali\naraw-araw",
        scene1Body: "Araw-araw na talata at panahon para magnilay –\nsa oras na kailangan mo.",
        scene2Title: "Pumili ng card.\nDalhin ang pag-asa.",
        scene2Body: "Tumuklas ng talata para sa sandaling ito,\npagkatapos ay itabi o ibahagi ito.",
        scene3Title: "Huminto. Magnilay.\nIsulat ang mahalaga.",
        scene3Body: "Itabi ang iyong tala kasama ng talatang\nnagbigay sa iyo ng inspirasyon.",
        scene4Title: "Ayon sa gusto mo",
        scene4Body: "Piliin ang salin ng Biblia at magtakda\nng banayad na paalala araw-araw.",
        scene5Body: "Isang payapang lugar para magbasa, magnilay,\nat muling makahanap ng pag-asa.",
        scene5Tagline: "ARAW-ARAW NA TALATA  •  TAHIMIK NA PAGNINILAY",
        googlePlay: "Available sa Google Play"
    ),
    "ml-IN": Copy(
        scene1Title: "ഓരോ ദിവസവും\nഒരു ശാന്ത നിമിഷം",
        scene1Body: "ദിവസവും ഒരു ബൈബിൾ വാക്യം, ശാന്തമായി ധ്യാനിക്കാൻ ഒരിടം –\nആവശ്യമുള്ള സമയത്ത്.",
        scene2Title: "ഒരു കാർഡ് എടുക്കൂ.\nപ്രത്യാശ കൂടെ കൊണ്ടുപോകൂ.",
        scene2Body: "ഈ നിമിഷത്തിനായൊരു വാക്യം കണ്ടെത്തൂ;\nഅത് സൂക്ഷിക്കൂ അല്ലെങ്കിൽ പങ്കിടൂ.",
        scene3Title: "ഒന്നു നിൽക്കൂ. ധ്യാനിക്കൂ.\nപ്രധാനമായത് കുറിച്ചിടൂ.",
        scene3Body: "നിങ്ങളെ സ്പർശിച്ച വാക്യത്തിനൊപ്പം\nനിങ്ങളുടെ കുറിപ്പും സൂക്ഷിക്കൂ.",
        scene4Title: "നിങ്ങൾക്കിഷ്ടമുള്ള പോലെ",
        scene4Body: "ഇഷ്ടമുള്ള ബൈബിൾ പരിഭാഷ തിരഞ്ഞെടുത്ത്\nദിവസവും ഓർമ്മിപ്പിക്കേണ്ട സമയം ക്രമീകരിക്കൂ.",
        scene5Body: "വായിക്കാനും ധ്യാനിക്കാനും വീണ്ടും പ്രത്യാശയിലേക്ക്\nമടങ്ങാനും ഒരു ശാന്ത ഇടം.",
        scene5Tagline: "ദൈനംദിന തിരുവചനം  •  ശാന്തമായ ധ്യാനം",
        googlePlay: "Google Play-യിൽ ലഭ്യമാണ്"
    )
]

guard let copy = localizedCopy[locale] else {
    fputs("Unsupported locale: \(locale)\n", stderr)
    exit(3)
}

private func rectFromTop(x: CGFloat, y: CGFloat, width: CGFloat, height: CGFloat) -> NSRect {
    NSRect(x: x, y: CGFloat(canvasHeight) - y - height, width: width, height: height)
}

private func appFont(weight: String, size: CGFloat) -> NSFont {
    let family = locale == "ml-IN" ? "NotoSansMalayalam-\(weight)" : "Poppins-\(weight)"
    return NSFont(name: family, size: size) ?? .systemFont(ofSize: size, weight: weight == "Bold" ? .bold : .regular)
}

private func poppins(weight: String, size: CGFloat) -> NSFont {
    NSFont(name: "Poppins-\(weight)", size: size) ?? .systemFont(ofSize: size)
}

private func drawText(
    _ value: String,
    x: CGFloat,
    y: CGFloat,
    width: CGFloat,
    font: NSFont,
    color: NSColor,
    lineHeight: CGFloat,
    kern: CGFloat = 0
) {
    let paragraph = NSMutableParagraphStyle()
    paragraph.minimumLineHeight = lineHeight
    paragraph.maximumLineHeight = lineHeight
    let attributes: [NSAttributedString.Key: Any] = [
        .font: font,
        .foregroundColor: color,
        .paragraphStyle: paragraph,
        .kern: kern
    ]
    let text = NSAttributedString(string: value, attributes: attributes)
    let measured = text.boundingRect(
        with: NSSize(width: width, height: 800),
        options: [.usesLineFragmentOrigin, .usesFontLeading]
    )
    text.draw(in: rectFromTop(x: x, y: y, width: width, height: ceil(measured.height) + 10))
}

private func drawBackground() {
    NSColor(calibratedWhite: 0.985, alpha: 1).setFill()
    canvas.fill()
    let base = NSGradient(colors: [
        NSColor(calibratedRed: 0.99, green: 0.98, blue: 0.95, alpha: 1),
        NSColor(calibratedRed: 0.93, green: 0.96, blue: 0.99, alpha: 1)
    ])!
    base.draw(in: canvas, angle: -90)
    let glow = NSGradient(colorsAndLocations:
        (NSColor(calibratedRed: 1.0, green: 0.96, blue: 0.84, alpha: 0.38), 0),
        (NSColor(calibratedWhite: 1, alpha: 0), 1)
    )!
    glow.draw(in: NSRect(x: 1120, y: 530, width: 900, height: 700), relativeCenterPosition: NSPoint(x: 0.3, y: 0.3))
}

private func drawEyebrow(x: CGFloat, y: CGFloat) {
    drawText("HOPE CARDS", x: x, y: y, width: 340, font: poppins(weight: "SemiBold", size: 17), color: gold, lineHeight: 24, kern: 1.6)
    gold.setFill()
    NSBezierPath(roundedRect: rectFromTop(x: x, y: y + 48, width: 108, height: 4), xRadius: 2, yRadius: 2).fill()
}

private func titleSize(scene: Int) -> CGFloat {
    if locale == "ml-IN" { return scene == 3 ? 48 : 52 }
    switch (locale, scene) {
    case ("de-DE", 2), ("de-DE", 3): return 60
    case ("fr-FR", 2), ("fr-FR", 3): return 58
    case ("it-IT", 2), ("es-419", 2): return 60
    case ("fil-PH", 1), ("fil-PH", 3): return 60
    default: return 68
    }
}

private func render(scene: Int, title: String?, body: String?, patch: NSRect, tagline: String? = nil, googlePlay: String? = nil) {
    guard let source = NSImage(contentsOf: sourceDirectory.appendingPathComponent("scene-\(scene).png")) else {
        fputs("Missing source frame for scene \(scene)\n", stderr)
        exit(4)
    }
    guard let patchBitmap = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: canvasWidth,
        pixelsHigh: canvasHeight,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: 0,
        bitsPerPixel: 0
    ) else { exit(5) }

    let patchContext = NSGraphicsContext(bitmapImageRep: patchBitmap)!
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = patchContext
    NSColor.clear.setFill()
    canvas.fill(using: .copy)
    NSBezierPath(rect: patch).addClip()
    drawBackground()

    let isRight = scene == 2
    let x: CGFloat = isRight ? 900 : (scene == 4 ? 120 : 160)
    drawEyebrow(x: x, y: 256)

    if scene == 5 {
        drawText("Hope Cards", x: x, y: 370, width: 820, font: poppins(weight: "Bold", size: 76), color: navy, lineHeight: 82)
        drawText(body!, x: x, y: 505, width: 880, font: appFont(weight: "Regular", size: locale == "ml-IN" ? 24 : 29), color: gray, lineHeight: locale == "ml-IN" ? 40 : 42)
        drawText(tagline!, x: x, y: 806, width: 900, font: appFont(weight: "SemiBold", size: locale == "ml-IN" ? 16 : 18), color: gold, lineHeight: 28, kern: locale == "ml-IN" ? 0 : 0.7)
        drawText(googlePlay!, x: x, y: 922, width: 820, font: appFont(weight: "SemiBold", size: locale == "ml-IN" ? 21 : 24), color: navy, lineHeight: 34)
    } else {
        let size = titleSize(scene: scene)
        drawText(title!, x: x, y: 370, width: isRight ? 900 : (scene == 4 ? 1000 : 920), font: appFont(weight: "Bold", size: size), color: navy, lineHeight: size + 7)
        drawText(body!, x: x, y: scene == 4 ? 530 : 594, width: isRight ? 900 : 980, font: appFont(weight: "Regular", size: locale == "ml-IN" ? 23 : 27), color: gray, lineHeight: locale == "ml-IN" ? 40 : 39)
    }

    NSGraphicsContext.current?.compositingOperation = .destinationIn
    let fadeRect: NSRect
    let fadeColors: [NSColor]
    if isRight {
        fadeRect = rectFromTop(x: 760, y: 0, width: 160, height: 1080)
        fadeColors = [.clear, .white]
    } else {
        let fadeStart: CGFloat = scene == 4 ? 780 : 900
        fadeRect = rectFromTop(x: fadeStart, y: 0, width: patch.maxX - fadeStart, height: 1080)
        fadeColors = [.white, .clear]
    }
    NSGradient(colors: fadeColors)!.draw(in: fadeRect, angle: 0)
    patchContext.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()

    let patchImage = NSImage(size: canvas.size)
    patchImage.addRepresentation(patchBitmap)

    guard let bitmap = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: canvasWidth,
        pixelsHigh: canvasHeight,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: 0,
        bitsPerPixel: 0
    ) else { exit(6) }
    let context = NSGraphicsContext(bitmapImageRep: bitmap)!
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = context
    source.draw(in: canvas, from: .zero, operation: .copy, fraction: 1)
    patchImage.draw(in: canvas, from: .zero, operation: .sourceOver, fraction: 1)
    context.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()
    guard let data = bitmap.representation(using: .png, properties: [:]) else { exit(6) }
    try! data.write(to: outputDirectory.appendingPathComponent("scene-\(scene).png"))
}

render(scene: 1, title: copy.scene1Title, body: copy.scene1Body, patch: rectFromTop(x: 0, y: 0, width: 1080, height: 1080))
render(scene: 2, title: copy.scene2Title, body: copy.scene2Body, patch: rectFromTop(x: 760, y: 0, width: 1160, height: 1080))
render(scene: 3, title: copy.scene3Title, body: copy.scene3Body, patch: rectFromTop(x: 0, y: 0, width: 1100, height: 1080))
render(scene: 4, title: copy.scene4Title, body: copy.scene4Body, patch: rectFromTop(x: 0, y: 0, width: 900, height: 1080))
render(scene: 5, title: nil, body: copy.scene5Body, patch: rectFromTop(x: 0, y: 0, width: 1080, height: 1080), tagline: copy.scene5Tagline, googlePlay: copy.googlePlay)

print("Localized supplied promo frames for \(locale)")
