#!/usr/bin/env swift

import AppKit
import CoreText
import Foundation

private let fileManager = FileManager.default
private let root = URL(fileURLWithPath: fileManager.currentDirectoryPath, isDirectory: true)
private let storeRoot = root.appendingPathComponent("assets/store", isDirectory: true)
private let listingRoot = storeRoot.appendingPathComponent("listings", isDirectory: true)
private let sourceRoot = storeRoot.appendingPathComponent("source/phone", isDirectory: true)
private let tabletSourceRoot = storeRoot.appendingPathComponent("source/tablet", isDirectory: true)

private let navy = NSColor(calibratedRed: 22 / 255, green: 42 / 255, blue: 77 / 255, alpha: 1)
private let ink = NSColor(calibratedRed: 28 / 255, green: 51 / 255, blue: 62 / 255, alpha: 1)
private let gold = NSColor(calibratedRed: 202 / 255, green: 153 / 255, blue: 43 / 255, alpha: 1)
private let softGold = NSColor(calibratedRed: 232 / 255, green: 207 / 255, blue: 147 / 255, alpha: 1)
private let ivory = NSColor(calibratedRed: 250 / 255, green: 248 / 255, blue: 243 / 255, alpha: 1)
private let warmIvory = NSColor(calibratedRed: 242 / 255, green: 234 / 255, blue: 219 / 255, alpha: 1)
private let mist = NSColor(calibratedRed: 230 / 255, green: 239 / 255, blue: 237 / 255, alpha: 1)

private struct ScreenshotSpec {
    let source: String
    let output: String
    let cropTop: CGFloat
    let cropHeight: CGFloat
}

private let screenshotSpecs = [
    ScreenshotSpec(source: "01-card-back.png", output: "01-draw-a-card.png", cropTop: 260, cropHeight: 1_650),
    ScreenshotSpec(source: "02-card-front.png", output: "02-read-scripture.png", cropTop: 260, cropHeight: 1_650),
    ScreenshotSpec(source: "03-daily-hope.png", output: "03-daily-hope.png", cropTop: 100, cropHeight: 1_765),
    ScreenshotSpec(source: "04-favorites.png", output: "04-save-favorites.png", cropTop: 100, cropHeight: 1_765),
    ScreenshotSpec(source: "05-journal.png", output: "05-journal-notes.png", cropTop: 100, cropHeight: 1_765),
    ScreenshotSpec(source: "06-themes.png", output: "06-choose-a-theme.png", cropTop: 140, cropHeight: 1_765),
]

private func registerBundledFonts() {
    let fontDirectory = root.appendingPathComponent("android/app/src/main/res/font", isDirectory: true)
    ["poppins_regular.ttf", "poppins_semibold.ttf", "poppins_bold.ttf", "source_serif_regular.ttf"].forEach { name in
        CTFontManagerRegisterFontsForURL(fontDirectory.appendingPathComponent(name) as CFURL, .process, nil)
    }
}

private func font(named name: String, size: CGFloat, fallbackWeight: NSFont.Weight = .regular) -> NSFont {
    NSFont(name: name, size: size) ?? NSFont.systemFont(ofSize: size, weight: fallbackWeight)
}

private func displayFont(locale: String, size: CGFloat, bold: Bool) -> NSFont {
    if locale == "ml-IN" {
        return font(named: bold ? "MalayalamSangamMN-Bold" : "MalayalamSangamMN", size: size, fallbackWeight: bold ? .bold : .regular)
    }
    return font(named: bold ? "Poppins-Bold" : "Poppins-Regular", size: size, fallbackWeight: bold ? .bold : .regular)
}

private func topRect(x: CGFloat, y: CGFloat, width: CGFloat, height: CGFloat, canvasHeight: CGFloat) -> NSRect {
    NSRect(x: x, y: canvasHeight - y - height, width: width, height: height)
}

private func paragraph(alignment: NSTextAlignment, lineSpacing: CGFloat = 0) -> NSMutableParagraphStyle {
    let style = NSMutableParagraphStyle()
    style.alignment = alignment
    style.lineBreakMode = .byWordWrapping
    style.lineSpacing = lineSpacing
    return style
}

private func textAttributes(font: NSFont, color: NSColor, alignment: NSTextAlignment, lineSpacing: CGFloat = 0, tracking: CGFloat = 0) -> [NSAttributedString.Key: Any] {
    [
        .font: font,
        .foregroundColor: color,
        .paragraphStyle: paragraph(alignment: alignment, lineSpacing: lineSpacing),
        .kern: tracking,
    ]
}

private func fittedFont(
    text: String,
    locale: String,
    bold: Bool,
    maxSize: CGFloat,
    minSize: CGFloat,
    width: CGFloat,
    height: CGFloat,
    alignment: NSTextAlignment,
    lineSpacing: CGFloat = 0
) -> NSFont {
    var size = maxSize
    while size > minSize {
        let candidate = displayFont(locale: locale, size: size, bold: bold)
        let attributed = NSAttributedString(
            string: text,
            attributes: textAttributes(font: candidate, color: navy, alignment: alignment, lineSpacing: lineSpacing)
        )
        let bounds = attributed.boundingRect(
            with: NSSize(width: width, height: .greatestFiniteMagnitude),
            options: [.usesLineFragmentOrigin, .usesFontLeading]
        )
        if bounds.width <= width && bounds.height <= height { return candidate }
        size -= 2
    }
    return displayFont(locale: locale, size: minSize, bold: bold)
}

private func drawText(
    _ text: String,
    locale: String,
    in rect: NSRect,
    font: NSFont,
    color: NSColor,
    alignment: NSTextAlignment = .left,
    lineSpacing: CGFloat = 0,
    tracking: CGFloat = 0
) {
    NSAttributedString(
        string: text,
        attributes: textAttributes(font: font, color: color, alignment: alignment, lineSpacing: lineSpacing, tracking: tracking)
    ).draw(with: rect, options: [.usesLineFragmentOrigin, .usesFontLeading])
}

private func makeBitmap(width: Int, height: Int, draw: (_ canvasHeight: CGFloat) throws -> Void) throws -> NSBitmapImageRep {
    guard let bitmap = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: width,
        pixelsHigh: height,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: 0,
        bitsPerPixel: 0
    ), let context = NSGraphicsContext(bitmapImageRep: bitmap) else {
        throw NSError(domain: "HopeCardsStoreAssets", code: 1, userInfo: [NSLocalizedDescriptionKey: "Unable to create bitmap context"])
    }

    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = context
    context.imageInterpolation = .high
    try draw(CGFloat(height))
    context.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()
    return bitmap
}

private func writePNG(_ bitmap: NSBitmapImageRep, to destination: URL) throws {
    try fileManager.createDirectory(at: destination.deletingLastPathComponent(), withIntermediateDirectories: true)
    guard let data = bitmap.representation(using: .png, properties: [.compressionFactor: 0.92]) else {
        throw NSError(domain: "HopeCardsStoreAssets", code: 2, userInfo: [NSLocalizedDescriptionKey: "Unable to encode PNG"])
    }
    try data.write(to: destination, options: .atomic)
}

private func drawBackground(width: CGFloat, height: CGFloat) {
    NSGradient(starting: ivory, ending: warmIvory)?.draw(in: NSRect(x: 0, y: 0, width: width, height: height), angle: -90)

    softGold.withAlphaComponent(0.11).setFill()
    NSBezierPath(ovalIn: NSRect(x: width - 365, y: height - 260, width: 500, height: 500)).fill()
    mist.withAlphaComponent(0.40).setFill()
    NSBezierPath(ovalIn: NSRect(x: -250, y: -185, width: 640, height: 640)).fill()
}

private func generateScreenshot(locale: String, caption: String, index: Int, spec: ScreenshotSpec, destination: URL) throws {
    let width = 1080
    let height = 1920
    let bitmap = try makeBitmap(width: width, height: height) { canvasHeight in
        drawBackground(width: CGFloat(width), height: canvasHeight)

        drawText(
            "HOPE CARDS",
            locale: locale,
            in: topRect(x: 82, y: 60, width: 600, height: 45, canvasHeight: canvasHeight),
            font: font(named: "Poppins-SemiBold", size: 29, fallbackWeight: .semibold),
            color: gold,
            tracking: 5.5
        )

        drawText(
            String(format: "%02d  —  %02d", index + 1, screenshotSpecs.count),
            locale: locale,
            in: topRect(x: 800, y: 62, width: 198, height: 44, canvasHeight: canvasHeight),
            font: font(named: "Poppins-SemiBold", size: 23, fallbackWeight: .semibold),
            color: navy.withAlphaComponent(0.62),
            alignment: .right,
            tracking: 1.5
        )

        gold.setFill()
        NSBezierPath(roundedRect: topRect(x: 82, y: 118, width: 84, height: 7, canvasHeight: canvasHeight), xRadius: 3.5, yRadius: 3.5).fill()

        let captionRect = topRect(x: 82, y: 145, width: 916, height: 165, canvasHeight: canvasHeight)
        let captionFont = fittedFont(
            text: caption,
            locale: locale,
            bold: true,
            maxSize: 60,
            minSize: 42,
            width: captionRect.width,
            height: captionRect.height,
            alignment: .left,
            lineSpacing: 3
        )
        drawText(caption, locale: locale, in: captionRect, font: captionFont, color: navy, lineSpacing: 3)

        let outer = topRect(x: 54, y: 330, width: 972, height: 1_550, canvasHeight: canvasHeight)
        let shadow = NSShadow()
        shadow.shadowColor = NSColor.black.withAlphaComponent(0.18)
        shadow.shadowBlurRadius = 28
        shadow.shadowOffset = NSSize(width: 0, height: -9)

        NSGraphicsContext.saveGraphicsState()
        shadow.set()
        ivory.setFill()
        NSBezierPath(roundedRect: outer, xRadius: 50, yRadius: 50).fill()
        NSGraphicsContext.restoreGraphicsState()

        gold.withAlphaComponent(0.32).setStroke()
        let frameBorder = NSBezierPath(roundedRect: outer.insetBy(dx: 2, dy: 2), xRadius: 48, yRadius: 48)
        frameBorder.lineWidth = 2
        frameBorder.stroke()

        let inner = outer.insetBy(dx: 16, dy: 16)
        guard let image = NSImage(contentsOf: sourceRoot.appendingPathComponent(spec.source)) else {
            throw NSError(domain: "HopeCardsStoreAssets", code: 3, userInfo: [NSLocalizedDescriptionKey: "Missing source screenshot: \(spec.source)"])
        }

        let sourceWidth = image.size.width
        let sourceHeight = image.size.height
        let desiredAspect = inner.width / inner.height
        var cropHeight = min(spec.cropHeight, sourceHeight - spec.cropTop)
        var cropWidth = cropHeight * desiredAspect
        if cropWidth > sourceWidth {
            cropWidth = sourceWidth
            cropHeight = cropWidth / desiredAspect
        }
        let sourceX = (sourceWidth - cropWidth) / 2
        let sourceY = sourceHeight - spec.cropTop - cropHeight
        let sourceRect = NSRect(x: sourceX, y: max(0, sourceY), width: cropWidth, height: cropHeight)

        NSGraphicsContext.saveGraphicsState()
        NSBezierPath(roundedRect: inner, xRadius: 34, yRadius: 34).addClip()
        image.draw(in: inner, from: sourceRect, operation: .copy, fraction: 1, respectFlipped: false, hints: [.interpolation: NSImageInterpolation.high])
        NSGraphicsContext.restoreGraphicsState()

        NSColor.white.withAlphaComponent(0.55).setStroke()
        let border = NSBezierPath(roundedRect: inner, xRadius: 34, yRadius: 34)
        border.lineWidth = 2
        border.stroke()
    }
    try writePNG(bitmap, to: destination)
}

private func generateFeatureGraphic(locale: String, lines: [String], destination: URL) throws {
    let width = 1024
    let height = 500
    let subtitle = lines.first ?? ""
    let featureLine = lines.dropFirst().first ?? ""
    let bitmap = try makeBitmap(width: width, height: height) { canvasHeight in
        NSGradient(
            colors: [
                NSColor(calibratedRed: 10 / 255, green: 29 / 255, blue: 55 / 255, alpha: 1),
                NSColor(calibratedRed: 29 / 255, green: 58 / 255, blue: 96 / 255, alpha: 1),
            ]
        )?.draw(in: NSRect(x: 0, y: 0, width: CGFloat(width), height: canvasHeight), angle: -18)

        softGold.withAlphaComponent(0.075).setFill()
        NSBezierPath(ovalIn: NSRect(x: -150, y: -220, width: 610, height: 610)).fill()
        NSColor.white.withAlphaComponent(0.045).setFill()
        NSBezierPath(ovalIn: NSRect(x: 740, y: 250, width: 420, height: 420)).fill()

        gold.setFill()
        NSBezierPath(roundedRect: topRect(x: 68, y: 70, width: 62, height: 9, canvasHeight: canvasHeight), xRadius: 4.5, yRadius: 4.5).fill()

        drawText(
            "Hope Cards",
            locale: locale,
            in: topRect(x: 68, y: 112, width: 540, height: 90, canvasHeight: canvasHeight),
            font: font(named: "Poppins-Bold", size: 64, fallbackWeight: .bold),
            color: ivory
        )

        let subtitleRect = topRect(x: 70, y: 215, width: 525, height: 114, canvasHeight: canvasHeight)
        let subtitleFont = fittedFont(
            text: subtitle,
            locale: locale,
            bold: false,
            maxSize: 31,
            minSize: 23,
            width: subtitleRect.width,
            height: subtitleRect.height,
            alignment: .left,
            lineSpacing: 5
        )
        drawText(subtitle, locale: locale, in: subtitleRect, font: subtitleFont, color: NSColor.white.withAlphaComponent(0.70), lineSpacing: 5)

        let featureRect = topRect(x: 70, y: 366, width: 530, height: 62, canvasHeight: canvasHeight)
        let featureFont = fittedFont(
            text: featureLine,
            locale: locale,
            bold: true,
            maxSize: 25,
            minSize: 18,
            width: featureRect.width,
            height: featureRect.height,
            alignment: .left
        )
        drawText(featureLine, locale: locale, in: featureRect, font: featureFont, color: gold)

        let cards: [(NSRect, NSColor, CGFloat)] = [
            (topRect(x: 630, y: 74, width: 262, height: 326, canvasHeight: canvasHeight), NSColor.white, 0.10),
            (topRect(x: 658, y: 92, width: 262, height: 326, canvasHeight: canvasHeight), ivory, 0.13),
            (topRect(x: 688, y: 111, width: 262, height: 326, canvasHeight: canvasHeight), navy, 0.18),
        ]

        for (cardRect, fill, shadowAlpha) in cards {
            NSGraphicsContext.saveGraphicsState()
            let shadow = NSShadow()
            shadow.shadowColor = NSColor.black.withAlphaComponent(shadowAlpha)
            shadow.shadowBlurRadius = 18
            shadow.shadowOffset = NSSize(width: 0, height: -5)
            shadow.set()
            fill.setFill()
            let card = NSBezierPath(roundedRect: cardRect, xRadius: 37, yRadius: 37)
            card.fill()
            NSGraphicsContext.restoreGraphicsState()

            gold.withAlphaComponent(fill.isEqual(navy) ? 0.9 : 0.48).setStroke()
            let inset = cardRect.insetBy(dx: 13, dy: 13)
            let line = NSBezierPath(roundedRect: inset, xRadius: 29, yRadius: 29)
            line.lineWidth = 2
            line.stroke()
        }

        drawText(
            "✦",
            locale: locale,
            in: topRect(x: 739, y: 178, width: 160, height: 74, canvasHeight: canvasHeight),
            font: NSFont.systemFont(ofSize: 51, weight: .light),
            color: gold,
            alignment: .center
        )
        drawText(
            "HOPE",
            locale: locale,
            in: topRect(x: 710, y: 265, width: 218, height: 70, canvasHeight: canvasHeight),
            font: font(named: "SourceSerif4-Regular", size: 51),
            color: ivory,
            alignment: .center,
            tracking: 2.5
        )
        drawText(
            "C A R D S",
            locale: locale,
            in: topRect(x: 730, y: 338, width: 178, height: 42, canvasHeight: canvasHeight),
            font: font(named: "Poppins-SemiBold", size: 17, fallbackWeight: .semibold),
            color: gold,
            alignment: .center,
            tracking: 3
        )
    }
    try writePNG(bitmap, to: destination)
}

private func generatePlayIcon(destination: URL) throws {
    let width = 512
    let height = 512
    let bitmap = try makeBitmap(width: width, height: height) { canvasHeight in
        NSGradient(
            colors: [
                NSColor(calibratedRed: 20 / 255, green: 43 / 255, blue: 76 / 255, alpha: 1),
                NSColor(calibratedRed: 9 / 255, green: 27 / 255, blue: 51 / 255, alpha: 1),
            ]
        )?.draw(in: NSRect(x: 0, y: 0, width: CGFloat(width), height: canvasHeight), angle: -58)

        softGold.withAlphaComponent(0.09).setFill()
        NSBezierPath(ovalIn: NSRect(x: 210, y: 210, width: 390, height: 390)).fill()

        let cardRects = [
            topRect(x: 82, y: 76, width: 330, height: 350, canvasHeight: canvasHeight),
            topRect(x: 101, y: 94, width: 330, height: 350, canvasHeight: canvasHeight),
            topRect(x: 120, y: 112, width: 330, height: 350, canvasHeight: canvasHeight),
        ]

        for (index, cardRect) in cardRects.enumerated() {
            NSGraphicsContext.saveGraphicsState()
            let shadow = NSShadow()
            shadow.shadowColor = NSColor.black.withAlphaComponent(0.28)
            shadow.shadowBlurRadius = 18
            shadow.shadowOffset = NSSize(width: 0, height: -7)
            shadow.set()
            (index == cardRects.count - 1 ? ivory : NSColor.white.withAlphaComponent(0.96)).setFill()
            NSBezierPath(roundedRect: cardRect, xRadius: 45, yRadius: 45).fill()
            NSGraphicsContext.restoreGraphicsState()

            gold.withAlphaComponent(index == cardRects.count - 1 ? 0.92 : 0.44).setStroke()
            let outline = NSBezierPath(roundedRect: cardRect.insetBy(dx: 12, dy: 12), xRadius: 35, yRadius: 35)
            outline.lineWidth = index == cardRects.count - 1 ? 4 : 2
            outline.stroke()
        }

        drawText(
            "✦",
            locale: "en-US",
            in: topRect(x: 206, y: 173, width: 158, height: 88, canvasHeight: canvasHeight),
            font: NSFont.systemFont(ofSize: 62, weight: .light),
            color: gold,
            alignment: .center
        )
        drawText(
            "H",
            locale: "en-US",
            in: topRect(x: 202, y: 257, width: 168, height: 128, canvasHeight: canvasHeight),
            font: font(named: "SourceSerif4-Regular", size: 108),
            color: navy,
            alignment: .center
        )
        gold.setFill()
        NSBezierPath(roundedRect: topRect(x: 235, y: 385, width: 104, height: 7, canvasHeight: canvasHeight), xRadius: 3.5, yRadius: 3.5).fill()
    }
    try writePNG(bitmap, to: destination)
}

private func generateTabletScreenshot(locale: String, caption: String, index: Int, source: String, destination: URL) throws {
    let width = 1_600
    let height = 2_560
    let bitmap = try makeBitmap(width: width, height: height) { canvasHeight in
        drawBackground(width: CGFloat(width), height: canvasHeight)

        drawText(
            "HOPE CARDS",
            locale: locale,
            in: topRect(x: 108, y: 76, width: 900, height: 58, canvasHeight: canvasHeight),
            font: font(named: "Poppins-SemiBold", size: 38, fallbackWeight: .semibold),
            color: gold,
            tracking: 7
        )
        drawText(
            String(format: "%02d  /  02", index + 1),
            locale: locale,
            in: topRect(x: 1_230, y: 79, width: 260, height: 55, canvasHeight: canvasHeight),
            font: font(named: "Poppins-SemiBold", size: 30, fallbackWeight: .semibold),
            color: navy.withAlphaComponent(0.62),
            alignment: .right,
            tracking: 2
        )

        gold.setFill()
        NSBezierPath(roundedRect: topRect(x: 108, y: 148, width: 112, height: 9, canvasHeight: canvasHeight), xRadius: 4.5, yRadius: 4.5).fill()

        let captionRect = topRect(x: 108, y: 190, width: 1_382, height: 190, canvasHeight: canvasHeight)
        let captionFont = fittedFont(
            text: caption,
            locale: locale,
            bold: true,
            maxSize: 82,
            minSize: 58,
            width: captionRect.width,
            height: captionRect.height,
            alignment: .left,
            lineSpacing: 4
        )
        drawText(caption, locale: locale, in: captionRect, font: captionFont, color: navy, lineSpacing: 4)

        let outer = topRect(x: 80, y: 410, width: 1_440, height: 2_075, canvasHeight: canvasHeight)
        NSGraphicsContext.saveGraphicsState()
        let shadow = NSShadow()
        shadow.shadowColor = NSColor.black.withAlphaComponent(0.18)
        shadow.shadowBlurRadius = 36
        shadow.shadowOffset = NSSize(width: 0, height: -12)
        shadow.set()
        NSColor.white.setFill()
        NSBezierPath(roundedRect: outer, xRadius: 58, yRadius: 58).fill()
        NSGraphicsContext.restoreGraphicsState()

        let inner = outer.insetBy(dx: 18, dy: 18)
        guard let image = NSImage(contentsOf: tabletSourceRoot.appendingPathComponent(source)) else {
            throw NSError(domain: "HopeCardsStoreAssets", code: 5, userInfo: [NSLocalizedDescriptionKey: "Missing tablet screenshot: \(source)"])
        }
        let desiredAspect = inner.width / inner.height
        var cropHeight = min(2_280, image.size.height - 180)
        var cropWidth = cropHeight * desiredAspect
        if cropWidth > image.size.width {
            cropWidth = image.size.width
            cropHeight = cropWidth / desiredAspect
        }
        let sourceRect = NSRect(
            x: (image.size.width - cropWidth) / 2,
            y: image.size.height - 180 - cropHeight,
            width: cropWidth,
            height: cropHeight
        )

        NSGraphicsContext.saveGraphicsState()
        NSBezierPath(roundedRect: inner, xRadius: 42, yRadius: 42).addClip()
        image.draw(in: inner, from: sourceRect, operation: .copy, fraction: 1, respectFlipped: false, hints: [.interpolation: NSImageInterpolation.high])
        NSGraphicsContext.restoreGraphicsState()
    }
    try writePNG(bitmap, to: destination)
}

private func nonEmptyLines(at url: URL) throws -> [String] {
    try String(contentsOf: url, encoding: .utf8)
        .split(whereSeparator: \.isNewline)
        .map(String.init)
        .filter { !$0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty }
}

registerBundledFonts()

let locales = try fileManager.contentsOfDirectory(at: listingRoot, includingPropertiesForKeys: [.isDirectoryKey])
    .filter { (try? $0.resourceValues(forKeys: [.isDirectoryKey]).isDirectory) == true }
    .sorted { $0.lastPathComponent < $1.lastPathComponent }

for localeDirectory in locales {
    let locale = localeDirectory.lastPathComponent
    let captions = try nonEmptyLines(at: localeDirectory.appendingPathComponent("screenshot-captions.txt"))
    guard captions.count == screenshotSpecs.count else {
        throw NSError(domain: "HopeCardsStoreAssets", code: 4, userInfo: [NSLocalizedDescriptionKey: "\(locale) must contain exactly \(screenshotSpecs.count) screenshot captions"])
    }

    let screenshotsDirectory = storeRoot.appendingPathComponent("screenshots/phone/\(locale)", isDirectory: true)
    for (index, spec) in screenshotSpecs.enumerated() {
        try generateScreenshot(
            locale: locale,
            caption: captions[index],
            index: index,
            spec: spec,
            destination: screenshotsDirectory.appendingPathComponent(spec.output)
        )
    }

    let tabletDirectory = storeRoot.appendingPathComponent("screenshots/tablet/\(locale)", isDirectory: true)
    try generateTabletScreenshot(
        locale: locale,
        caption: captions[0],
        index: 0,
        source: "01-card-back.png",
        destination: tabletDirectory.appendingPathComponent("01-draw-a-card-tablet.png")
    )
    try generateTabletScreenshot(
        locale: locale,
        caption: captions[1],
        index: 1,
        source: "02-card-front.png",
        destination: tabletDirectory.appendingPathComponent("02-read-scripture-tablet.png")
    )

    let featureCopy = try nonEmptyLines(at: localeDirectory.appendingPathComponent("feature-copy.txt"))
    try generateFeatureGraphic(
        locale: locale,
        lines: featureCopy,
        destination: storeRoot.appendingPathComponent("feature-graphics/\(locale)/feature-graphic.png")
    )
}

let englishFeatureCopy = try nonEmptyLines(at: listingRoot.appendingPathComponent("en-US/feature-copy.txt"))
try generateFeatureGraphic(locale: "en-US", lines: englishFeatureCopy, destination: storeRoot.appendingPathComponent("feature-graphic.png"))
try generatePlayIcon(destination: storeRoot.appendingPathComponent("play-icon-512.png"))

print("Generated localized Play Store assets for \(locales.count) locales.")
