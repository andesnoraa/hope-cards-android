"""Build the reviewed 365-entry selection; never synthesize or paraphrase Scripture.

Usage: python3 scripts/daily-verses/build.py SOURCE_DIR BASELINE_DIR
Inputs are public-domain edition distributions documented in sources.json.
"""
import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path
from sources import EDITIONS, load_sources, normalize, reference_parts, reference

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / 'android/app/src/main/assets/verses'

def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')

def main(source_dir, baseline_dir):
    source_dir, baseline_dir = Path(source_dir), Path(baseline_dir)
    sources = load_sources(source_dir)
    old = {e: json.loads((baseline_dir / (e + '.json')).read_text()) for e in EDITIONS}
    replacements = json.loads((HERE / 'replacements.json').read_text())
    by_old = {r['oldId']: r for r in replacements}
    assert len(by_old) == len(replacements)
    assert set(by_old) <= {v['id'] for v in old['en-bsb']}

    # Segond numbers most Psalm superscriptions as verses. Record explicit
    # mappings for this selection rather than applying a runtime offset.
    french_psalm_offsets = {}
    for chapter in range(1, 151):
        count = lambda e: max(v for b, c, v in sources[e] if b == 'PSA' and c == chapter)
        french_psalm_offsets[chapter] = count('fr-lsg1910') - count('en-kjv')

    def edition_ref(edition, canonical):
        book, chapter, first, last = reference_parts(canonical)
        if edition == 'fr-lsg1910':
            if book == 'PSA':
                offset = french_psalm_offsets[chapter]
                assert offset in {0, 1, 2}, (canonical, offset)
                first, last = first + offset, last + offset
            elif (book, chapter, first, last) == ('2CO', 13, 14, 14):
                first = last = 13
        if edition == 'es-rv1909' and (book, chapter, first, last) == ('2CO', 13, 14, 14):
            first = last = 13
        # The 1910 Malayalam translation reorders the clauses of Philippians
        # 1:3-6. Its v4 contains the assurance corresponding to English v6.
        if edition == 'ml-mal1910' and (book, chapter, first, last) == ('PHP', 1, 6, 6):
            first = last = 4
        # Keep the English book key for the existing localized-title formatter.
        return reference(book, chapter, first, last)

    provenance, checks, mappings, review = {}, {}, {}, []
    active, archive = {}, {}
    for edition, code in EDITIONS.items():
        source_file = code + ('.json' if code in {'mal1910', 'tagalog'} else '_usfx.zip')
        url = f'https://api.getbible.net/v2/{code}.json' if source_file.endswith('.json') else f'https://ebible.org/Scriptures/{source_file}'
        provenance[edition] = {'edition': old[edition][0]['translation'], 'url': url, 'file': source_file,
            'sha256': hashlib.sha256((source_dir / source_file).read_bytes()).hexdigest(),
            'license': 'Public domain', 'retrieved': '2026-09-22'}
        active[edition], archive[edition], mappings[edition] = [], [], {}
        # Independently parse the publisher's VPL export to catch USFX extraction
        # errors. A superscription may precede the verse in VPL but not USFX.
        vpl = {}
        if source_file.endswith('.zip'):
            file = source_dir / (code + '_vpl.zip')
            for line in zipfile.ZipFile(file).read(code + '_vpl.txt').decode('utf-8-sig').splitlines():
                match = re.match(r'^(\w+) (\d+):(\d+) (.*)$', line)
                if match:
                    b, c, v, text = match.groups()
                    b = {'JOH':'JHN', 'MAR':'MRK', 'PHI':'PHP', 'JAM':'JAS', '1JO':'1JN', '2JO':'2JN', '3JO':'3JN', 'EZE':'EZK', 'JOE':'JOL', 'NAH':'NAM', 'SON':'SNG', 'SOL':'SNG'}.get(b, b)
                    vpl[(b, int(c), int(v))] = normalize(text)
            provenance[edition]['crossCheck'] = {'url': f'https://ebible.org/Scriptures/{file.name}', 'sha256': hashlib.sha256(file.read_bytes()).hexdigest()}

        checked = set()
        def sourced(row, canonical):
            ref = edition_ref(edition, canonical)
            book, chapter, first, last = reference_parts(ref)
            texts = []
            for verse in range(first, last + 1):
                key = (book, chapter, verse)
                text = sources[edition][key]
                assert text, (edition, key)
                if vpl:
                    assert vpl[key].replace('[', '').replace(']', '').endswith(text), (edition, key, text, vpl[key])
                checked.add(key)
                texts.append(text)
            # Preserve 'Psalm' vs 'Psalms' spelling in existing references to
            # avoid changing stable artwork reference labels needlessly.
            if canonical.startswith('Psalm '):
                ref = ref.replace('Psalms ', 'Psalm ', 1)
            joined = ' '.join(texts)
            if edition == 'de-lut1912':
                # eBible's Luther text is distributed under KJV verse keys.
                # Display Luther's own numbering (CrossWire Luther mapping;
                # Psalm superscription offsets agree with Segond here).
                if book == 'PSA':
                    offset = french_psalm_offsets[chapter]
                    assert offset in {0, 1, 2}
                    if chapter == 46 and first == 1:
                        heading = 'Ein Lied der Kinder Korah, von der Jugend, vorzusingen. '
                        assert joined.startswith(heading)
                        joined = joined.removeprefix(heading)
                    ref = reference(book, chapter, first + offset, last + offset)
                    if canonical.startswith('Psalm '):
                        ref = ref.replace('Psalms ', 'Psalm ', 1)
                elif (book, chapter, first) == ('2CO', 13, 14):
                    ref = '2 Corinthians 13:13'
                elif (book, chapter, first) == ('DEU', 13, 3):
                    ref = 'Deuteronomy 13:4'
            if edition == 'it-riv1927' and joined.startswith('(G13-13) '):
                joined = joined.removeprefix('(G13-13) ')
                ref = '2 Corinthians 13:13'
            if edition == 'es-rv1909' and ref == '2 Corinthians 13:13':
                colophon = ' La segunda Epístola á los Corintios fué enviada de Filipos de Macedonia con Tito y Lucas.'
                assert joined.endswith(colophon)
                joined = joined.removesuffix(colophon)
            result = dict(row, verse=joined, reference=ref)
            mappings[edition][row['id']] = {'canonicalReference': canonical, 'sourceReference': edition_ref(edition, canonical), 'displayReference': ref,
                'textSha256': hashlib.sha256(joined.encode()).hexdigest()}
            return result

        for index, row in enumerate(old[edition]):
            canonical = old['en-bsb'][index]['reference']
            assert row['id'] == old['en-bsb'][index]['id']
            replacement = by_old.get(row['id'])
            if replacement:
                archive[edition].append(sourced(row, canonical))
                ref = replacement['newReference']
                new_id = re.sub(r'[^a-z0-9]+', '-', ref.lower()).strip('-')
                row = dict(row, id=new_id, category=replacement['category'], tags=[replacement['category'].lower()])
                canonical = ref
            active[edition].append(sourced(row, canonical))
        assert len(active[edition]) == 365
        assert len({v['id'] for v in active[edition]}) == 365
        assert len({reference_parts(v['reference']) for v in active[edition]}) == 365
        # Prevent overlapping ranges in the daily rotation.
        occupied = set()
        for row in active[edition]:
            b, c, a, z = reference_parts(row['reference'])
            for v in range(a, z + 1):
                assert (b, c, v) not in occupied, (edition, row['reference'])
                occupied.add((b, c, v))
        checks[edition] = {'activeCards': len(active[edition]), 'archivedCards': len(archive[edition]),
            'distinctSourceVersesChecked': len(checked), 'maxCharacters': max(len(v['verse']) for v in active[edition]),
            'maxWords': max(len(v['verse'].split()) for v in active[edition])}

    for index, before in enumerate(old['en-bsb']):
        replacement = by_old.get(before['id'])
        changed = any(old[e][index] != active[e][index] for e in EDITIONS)
        if not changed:
            continue
        review.append({'kind': 'replacement' if replacement else 'source-correction', 'oldId': before['id'],
            'reason': replacement['reason'] if replacement else 'Match the named edition source exactly, including its verse numbering, wording, capitalization and punctuation; normalize layout whitespace only.',
            'editions': {e: {'before': old[e][index], 'after': active[e][index]} for e in EDITIONS}})
    for edition in EDITIONS:
        write_json(ASSETS / (edition + '.json'), active[edition])
        write_json(ASSETS / 'archive' / (edition + '.json'), archive[edition])
    write_json(HERE / 'sources.json', provenance)
    write_json(HERE / 'reference-map.json', mappings)
    write_json(HERE / 'verification.json', checks)
    write_json(HERE / 'review.json', review)
    print(json.dumps(checks, indent=2))
    print('Replaced', len(replacements), 'cards. Total review rows:', len(review))

if __name__ == '__main__':
    main(*sys.argv[1:])
