#!/usr/bin/env swift

import AppKit
import CoreText
import Foundation

let width = 1920
let height = 1080
let canvas = NSRect(x: 0, y: 0, width: width, height: height)

guard CommandLine.arguments.count == 3 else {
    fputs("Usage: generate-easy-read-promo.swift <screenshot-directory> <output-directory>\n", stderr)
    exit(2)
}

let screenshotDirectory = URL(fileURLWithPath: CommandLine.arguments[1], isDirectory: true)
let outputDirectory = URL(fileURLWithPath: CommandLine.arguments[2], isDirectory: true)
try FileManager.default.createDirectory(at: outputDirectory, withIntermediateDirectories: true)

let projectRoot = URL(fileURLWithPath: FileManager.default.currentDirectoryPath, isDirectory: true)
let fontDirectory = projectRoot.appendingPathComponent("android/app/src/main/res/font", isDirectory: true)
for fontName in ["poppins_regular.ttf", "poppins_semibold.ttf", "poppins_bold.ttf"] {
    CTFontManagerRegisterFontsForURL(fontDirectory.appendingPathComponent(fontName) as CFURL, .process, nil)
}

let navy = NSColor(calibratedRed: 0.055, green: 0.118, blue: 0.245, alpha: 1)
let gold = NSColor(calibratedRed: 0.74, green: 0.52, blue: 0.10, alpha: 1)
let gray = NSColor(calibratedRed: 0.34, green: 0.38, blue: 0.43, alpha: 1)

func image(_ fileName: String) -> NSImage {
    let url = screenshotDirectory.appendingPathComponent(fileName)
    guard let value = NSImage(contentsOf: url) else {
        fputs("Unable to read screenshot: \(url.path)\n", stderr)
        exit(3)
    }
    return value
}

func font(_ family: String, size: CGFloat) -> NSFont {
    NSFont(name: family, size: size) ?? .systemFont(ofSize: size, weight: family.contains("Bold") ? .bold : .regular)
}

func rectFromTop(x: CGFloat, y: CGFloat, width: CGFloat, height: CGFloat) -> NSRect {
    NSRect(x: x, y: CGFloat(1080) - y - height, width: width, height: height)
}

func drawText(
    _ value: String,
    x: CGFloat,
    y: CGFloat,
    width: CGFloat,
    font: NSFont,
    color: NSColor,
    lineHeight: CGFloat? = nil,
    alignment: NSTextAlignment = .left,
    kern: CGFloat = 0
) {
    let paragraph = NSMutableParagraphStyle()
    paragraph.alignment = alignment
    if let lineHeight {
        paragraph.minimumLineHeight = lineHeight
        paragraph.maximumLineHeight = lineHeight
    }
    let attributes: [NSAttributedString.Key: Any] = [
        .font: font,
        .foregroundColor: color,
        .paragraphStyle: paragraph,
        .kern: kern
    ]
    let text = NSAttributedString(string: value, attributes: attributes)
    let measured = text.boundingRect(
        with: NSSize(width: width, height: 900),
        options: [.usesLineFragmentOrigin, .usesFontLeading]
    )
    text.draw(in: rectFromTop(x: x, y: y, width: width, height: ceil(measured.height) + 8))
}

func drawBackground() {
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

func drawEyebrow() {
    drawText("HOPE CARDS", x: 72, y: 78, width: 310, font: font("Poppins-SemiBold", size: 17), color: gold, kern: 1.6)
    gold.setFill()
    NSBezierPath(roundedRect: rectFromTop(x: 72, y: 111, width: 58, height: 4), xRadius: 2, yRadius: 2).fill()
}

func drawPhone(_ screenshot: NSImage, centerX: CGFloat, centerY: CGFloat = 545, screenHeight: CGFloat = 972) {
    let sourceSize = screenshot.size
    let screenWidth = screenHeight * sourceSize.width / sourceSize.height
    let outer = NSRect(
        x: centerX - screenWidth / 2 - 12,
        y: CGFloat(height) - centerY - screenHeight / 2 - 12,
        width: screenWidth + 24,
        height: screenHeight + 24
    )
    let inner = outer.insetBy(dx: 9, dy: 9)

    NSGraphicsContext.saveGraphicsState()
    let shadow = NSShadow()
    shadow.shadowColor = NSColor.black.withAlphaComponent(0.20)
    shadow.shadowBlurRadius = 22
    shadow.shadowOffset = NSSize(width: 0, height: -8)
    shadow.set()
    NSColor(calibratedWhite: 0.055, alpha: 1).setFill()
    NSBezierPath(roundedRect: outer, xRadius: 44, yRadius: 44).fill()
    NSGraphicsContext.restoreGraphicsState()

    NSGraphicsContext.saveGraphicsState()
    NSBezierPath(roundedRect: inner, xRadius: 35, yRadius: 35).addClip()
    screenshot.draw(in: inner, from: .zero, operation: .copy, fraction: 1)
    NSGraphicsContext.restoreGraphicsState()

    NSColor.white.withAlphaComponent(0.24).setStroke()
    let highlight = NSBezierPath(roundedRect: outer.insetBy(dx: 4, dy: 4), xRadius: 40, yRadius: 40)
    highlight.lineWidth = 2
    highlight.stroke()
}

func render(_ name: String, draw: () -> Void) {
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
    ) else { exit(4) }
    let context = NSGraphicsContext(bitmapImageRep: bitmap)!
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = context
    drawBackground()
    draw()
    context.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()
    let data = bitmap.representation(using: .png, properties: [:])!
    try! data.write(to: outputDirectory.appendingPathComponent(name))
}

let dailyHope = image("01-daily-hope.png")
let card = image("02-draw-a-card.png")
let journal = image("05-add-journal-amen.png")
let settings = image("03-settings.png")
let reminder = image("04-daily-reminder.png")

render("01-daily.png") {
    drawEyebrow()
    drawText("A quiet moment\nfor every day", x: 72, y: 164, width: 720, font: font("Poppins-Bold", size: 68), color: navy, lineHeight: 74)
    drawText("A daily Bible verse and a quiet place to reflect—\nright when you need it.", x: 72, y: 344, width: 820, font: font("Poppins-Regular", size: 27), color: gray, lineHeight: 39)
    drawPhone(dailyHope, centerX: 1505)
}

render("02-card.png") {
    drawPhone(card, centerX: 270)
    drawEyebrow()
    drawText("Draw a card.\nCarry hope.", x: 705, y: 300, width: 700, font: font("Poppins-Bold", size: 68), color: navy, lineHeight: 74)
    drawText("Discover a verse for the moment,\nthen save it or share it.", x: 705, y: 480, width: 760, font: font("Poppins-Regular", size: 27), color: gray, lineHeight: 39)
}

render("03-journal.png") {
    drawEyebrow()
    drawText("Pause. Reflect.\nAdd a note.", x: 72, y: 300, width: 790, font: font("Poppins-Bold", size: 68), color: navy, lineHeight: 74)
    drawText("Keep a personal note with the verse\nthat inspired it.", x: 72, y: 480, width: 800, font: font("Poppins-Regular", size: 27), color: gray, lineHeight: 39)
    drawPhone(journal, centerX: 1510)
}

render("04-settings.png") {
    drawEyebrow()
    drawText("Make it yours", x: 72, y: 310, width: 700, font: font("Poppins-Bold", size: 68), color: navy, lineHeight: 74)
    drawText("Choose your Bible translation and set\na gentle daily reminder.", x: 72, y: 420, width: 820, font: font("Poppins-Regular", size: 27), color: gray, lineHeight: 39)
    drawPhone(settings, centerX: 1380, screenHeight: 890)
    drawPhone(reminder, centerX: 1615, screenHeight: 890)
}

render("05-end.png") {
    drawEyebrow()
    drawText("Hope Cards", x: 72, y: 270, width: 760, font: font("Poppins-Bold", size: 76), color: navy, lineHeight: 82)
    drawText("A calm place for Scripture, reflection,\nand hope.", x: 72, y: 390, width: 760, font: font("Poppins-Regular", size: 29), color: gray, lineHeight: 42)
    drawText("DAILY SCRIPTURE  •  QUIET REFLECTION", x: 72, y: 585, width: 760, font: font("Poppins-SemiBold", size: 18), color: gold, kern: 0.7)
    drawText("Available on Google Play", x: 72, y: 630, width: 760, font: font("Poppins-SemiBold", size: 24), color: navy)
    drawPhone(card, centerX: 1510)
}

print("Rendered easy-read promo scenes in \(outputDirectory.path)")
