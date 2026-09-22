"""Extract edition text verbatim (only collapse layout whitespace), without notes."""
import json
import re
import zipfile
from pathlib import Path
from xml.etree import ElementTree as ET

BOOKS = 'Genesis|Exodus|Leviticus|Numbers|Deuteronomy|Joshua|Judges|Ruth|1 Samuel|2 Samuel|1 Kings|2 Kings|1 Chronicles|2 Chronicles|Ezra|Nehemiah|Esther|Job|Psalms|Proverbs|Ecclesiastes|Song of Solomon|Isaiah|Jeremiah|Lamentations|Ezekiel|Daniel|Hosea|Joel|Amos|Obadiah|Jonah|Micah|Nahum|Habakkuk|Zephaniah|Haggai|Zechariah|Malachi|Matthew|Mark|Luke|John|Acts|Romans|1 Corinthians|2 Corinthians|Galatians|Ephesians|Philippians|Colossians|1 Thessalonians|2 Thessalonians|1 Timothy|2 Timothy|Titus|Philemon|Hebrews|James|1 Peter|2 Peter|1 John|2 John|3 John|Jude|Revelation'.split('|')
CODES = 'GEN EXO LEV NUM DEU JOS JDG RUT 1SA 2SA 1KI 2KI 1CH 2CH EZR NEH EST JOB PSA PRO ECC SNG ISA JER LAM EZK DAN HOS JOL AMO OBA JON MIC NAM HAB ZEP HAG ZEC MAL MAT MRK LUK JHN ACT ROM 1CO 2CO GAL EPH PHP COL 1TH 2TH 1TI 2TI TIT PHM HEB JAS 1PE 2PE 1JN 2JN 3JN JUD REV'.split()
EDITIONS = {'en-bsb':'engbsb', 'en-bbe':'engBBE', 'en-kjv':'eng-kjv2006', 'en-web':'engwebu', 'de-lut1912':'deu1912', 'fr-lsg1910':'fraLSG', 'it-riv1927':'ita1927', 'es-rv1909':'spaRV1909', 'ml-mal1910':'mal1910', 'tl-adb1905':'tagalog'}

def normalize(text):
    return re.sub(r'\s+', ' ', text.replace('¶', '')).strip()

def reference_parts(reference):
    match = re.fullmatch(r'(.+) (\d+):(\d+)(?:[-–](\d+))?', reference)
    book, chapter, first, last = match.groups()
    return CODES[BOOKS.index('Psalms' if book == 'Psalm' else book)], int(chapter), int(first), int(last or first)

def reference(code, chapter, first, last=None):
    return f'{BOOKS[CODES.index(code)]} {chapter}:{first}' + (f'-{last}' if last and last != first else '')

def read_usfx(path, code):
    root = ET.fromstring(zipfile.ZipFile(path).read(code + '_usfx.xml'))
    result = {}
    for book in root.findall('book'):
        book_id = book.get('id')
        if book_id not in CODES:
            continue
        chapter, active, parts = None, None, []
        def finish():
            nonlocal active, parts
            if active:
                key = (book_id, chapter, active)
                assert key not in result, key
                result[key] = normalize(''.join(parts))
            active, parts = None, []
        def visit(node):
            nonlocal chapter, active
            if node.tag == 'c':
                finish()
                chapter = int(node.get('id'))
            elif node.tag == 'v':
                finish()
                active = int(node.get('id'))
            elif node.tag == 've':
                finish()
            elif node.tag in {'f', 'x', 's', 'd', 'h', 'toc', 'id', 'rem', 'fig'} or (node.tag == 'p' and node.get('sfm', 'p').startswith(('i', 'mt', 'ms', 'mr', 'r'))):
                return
            else:
                if active and node.text:
                    parts.append(node.text)
                for child in node:
                    visit(child)
                    if active and child.tail:
                        parts.append(child.tail)
        visit(book)
        finish()
    return result

def read_json(path):
    result = {}
    for book in json.loads(path.read_text())['books']:
        for chapter in book['chapters']:
            for verse in chapter['verses']:
                result[(CODES[book['nr'] - 1], chapter['chapter'], verse['verse'])] = normalize(verse['text'])
    return result

def load_sources(directory):
    directory = Path(directory)
    return {edition: read_json(directory / (code + '.json')) if code in {'mal1910', 'tagalog'} else read_usfx(directory / (code + '_usfx.zip'), code) for edition, code in EDITIONS.items()}

if __name__ == '__main__':
    import sys
    sources = load_sources(sys.argv[1])
    for edition, verses in sources.items():
        print(edition, len(verses), verses[('PSA', 46, 1)])
