"""Additional checks against Malayalam's transcription and Tagalog chapter exports.

Downloads are build-time only; no network code is added to the Android app.
"""
import concurrent.futures
import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
from sources import CODES, normalize, read_json, reference_parts

HERE = Path(__file__).resolve().parent

def main(directory):
    directory = Path(directory)
    mappings = json.loads((HERE / 'reference-map.json').read_text())
    def fetch(url, filename):
        path = directory / filename
        if not path.exists():
            subprocess.run(['curl', '-L', '--fail', '--silent', '--show-error', '--retry', '2', url, '-o', str(path)], check=True)
        return path
    commit = '5f0f30ea9726c419d4b5ee53aaab93ff3480a98d'
    url = f'https://github.com/tfbf/Bible-Malayalam-Sathyavedapusthakam-1910/archive/{commit}.zip'
    path = fetch(url, 'mal1910-primary.zip')
    primary = {}
    with zipfile.ZipFile(path) as archive:
        for name in archive.namelist():
            if '/usfm/' not in name or not name.endswith('.usfm'):
                continue
            text = archive.read(name).decode('utf-8-sig')
            book = re.search(r'\\id (\w+)', text)[1]
            chapter, verse, parts = None, None, []
            def finish():
                if verse:
                    text = ' '.join(parts)
                    text = re.sub(r'\\f .*?\\f\*', '', text)
                    text = re.sub(r'\\x .*?\\x\*', '', text)
                    text = re.sub(r'\\\+?\w+\*? ?', '', text)
                    primary[(book, chapter, verse)] = normalize(text)
            for line in text.splitlines():
                match = re.match(r'\\c (\d+)', line)
                if match:
                    finish(); chapter = int(match[1]); verse = None; parts = []; continue
                match = re.match(r'\\v (\d+) (.*)', line)
                if match:
                    finish(); verse = int(match[1]); parts = [match[2]]; continue
                if re.match(r'\\(s\d?|d|ms\d?|cl|r)\b', line):
                    finish(); verse = None; parts = []; continue
                if verse:
                    parts.append(line)
            finish()
    raw = read_json(directory / 'mal1910.json')
    checked = set()
    whitespace_differences = []
    for item in mappings['ml-mal1910'].values():
        b, c, first, last = reference_parts(item['sourceReference'])
        for v in range(first, last + 1):
            key = b, c, v
            # Two Psalms differ only by a layout space before Selah.
            assert ''.join(primary[key].split()) == ''.join(raw[key].split()), key
            if primary[key] != raw[key] and key not in checked:
                whitespace_differences.append(list(key))
            checked.add(key)
    report = {'mal1910': {'checkedSourceVerses': len(checked), 'source': url,
        'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'layoutSpaceDifferences': whitespace_differences}}

    mapping_url = 'https://raw.githubusercontent.com/crosswire/jsword/master/src/main/resources/org/crosswire/jsword/versification/Luther.properties'
    mapping_file = fetch(mapping_url, 'Luther.properties')
    mapping_text = mapping_file.read_text()
    psalm_numbers = {}
    for line in mapping_text.splitlines():
        match = re.fullmatch(r'Ps\.(\d+)\.(\d+)-Ps\.\1\.(\d+)=Ps\.\1\.(\d+)-Ps\.\1\.(\d+)', line)
        if match:
            chapter, start, end, english_start, english_end = map(int, match.groups())
            assert end - start == english_end - english_start
            for v in range(english_start, english_end + 1):
                psalm_numbers[(chapter, v)] = start + v - english_start
    german_count = 0
    for item in mappings['de-lut1912'].values():
        b, c, first, last = reference_parts(item['sourceReference'])
        _, dc, df, dl = reference_parts(item['displayReference'])
        if b == 'PSA':
            assert dc == c
            assert (df, dl) == (psalm_numbers.get((c, first), first), psalm_numbers.get((c, last), last)), item
            german_count += 1
        elif (b, c, first) == ('2CO', 13, 14):
            assert '2Cor.13.13=2Cor.13.14' in mapping_text and df == 13
        elif (b, c, first) == ('DEU', 13, 3):
            assert 'Deut.13.1-Deut.13.19=Deut.12.32-Deut.13.18' in mapping_text and df == 4
    report['lutherNumbering'] = {'psalmCardsChecked': german_count, 'source': mapping_url,
        'sha256': hashlib.sha256(mapping_file.read_bytes()).hexdigest(),
        'note': 'Text comes from the eBible public-domain Luther 1912 edition. Display numbers follow Luther; source keys are retained separately.'}

    reviews = json.loads((HERE / 'review.json').read_text())
    replacement_refs = [reference_parts(r['editions']['tl-adb1905']['after']['reference']) for r in reviews if r['kind'] == 'replacement']
    chapters = sorted({(b, c) for b, c, _, _ in replacement_refs})
    def chapter_export(key):
        b, c = key
        book_number = CODES.index(b) + 1
        url = f'https://api.getbible.net/v2/tagalog/{book_number}/{c}.json'
        file = fetch(url, f'tagalog-{book_number}-{c}.json')
        data = json.loads(file.read_text())
        assert data['book_nr'] == book_number and data['chapter'] == c
        return key, data, {'url': url, 'sha256': hashlib.sha256(file.read_bytes()).hexdigest()}
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:
        downloaded = list(pool.map(chapter_export, chapters))
    chapter_data = {key: {v['verse']: normalize(v['text']) for v in data['verses']} for key, data, _ in downloaded}
    raw = read_json(directory / 'tagalog.json')
    count = 0
    for b, c, first, last in replacement_refs:
        for v in range(first, last + 1):
            assert chapter_data[(b, c)][v] == raw[(b, c, v)], (b, c, v)
            count += 1
    report['tagalog'] = {'replacementCardsChecked': len(replacement_refs), 'sourceVersesChecked': count,
        'method': 'Bulk edition compared with separately downloaded chapter exports from the same distributor.',
        'chapterExports': [metadata for _, _, metadata in downloaded]}
    (HERE / 'cross-check-results.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
    print('Malayalam primary transcription:', len(checked), 'verses checked; Tagalog:', count, 'replacement verse units checked.')

if __name__ == '__main__':
    main(sys.argv[1])
