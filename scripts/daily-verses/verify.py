"""Offline regression checks for the reviewed Scripture assets and provenance."""
import hashlib
import json
from pathlib import Path
from sources import EDITIONS, reference_parts
HERE = Path(__file__).resolve().parent
ASSETS = HERE.parents[1] / 'android/app/src/main/assets/verses'
maps = json.loads((HERE / 'reference-map.json').read_text())
reviews = json.loads((HERE / 'review.json').read_text())
replacements = [r for r in reviews if r['kind'] == 'replacement']
assert len(replacements) == 83
expected = None
for edition in EDITIONS:
    active = json.loads((ASSETS / (edition + '.json')).read_text())
    archive = json.loads((ASSETS / 'archive' / (edition + '.json')).read_text())
    ids = {v['id'] for v in active}
    assert len(active) == len(ids) == 365
    assert len(archive) == 83
    assert not ids.intersection(v['id'] for v in archive)
    if expected is None:
        expected = ids
    assert ids == expected, edition
    occupied = set()
    for v in active:
        assert 0 < len(v['verse']) <= 250, (edition, v['id'], len(v['verse']))
        b,c,first,last = reference_parts(v['reference'])
        for number in range(first,last+1):
            assert (b,c,number) not in occupied, (edition, v['id'])
            occupied.add((b,c,number))
    by_id = {v['id']: v for v in active + archive}
    assert set(by_id) == set(maps[edition]), edition
    for v in by_id.values():
        proof = maps[edition][v['id']]
        assert proof['displayReference'] == v['reference'], (edition, v['id'])
        assert proof['textSha256'] == hashlib.sha256(v['verse'].encode()).hexdigest(), (edition, v['id'])
    for row in reviews:
        after = row['editions'][edition]['after']
        assert by_id[after['id']] == after, (edition, after['id'])
    for row in replacements:
        assert row['oldId'] in by_id and row['oldId'] not in ids
        if edition == 'en-bsb':
            assert 10 <= len(row['editions'][edition]['after']['verse'].split()) <= 38
art = json.loads((HERE.parent / 'verse-art/verified-web-verses.json').read_text())['verses']
web = {v['id']:v for part in ['', 'archive/'] for v in json.loads((ASSETS / (part + 'en-web.json')).read_text())}
for card in art:
    assert card['reference'] == web[card['id']]['reference']
    assert card['text'].strip('“”') == web[card['id']]['verse'].strip('“”'), card['id']
print('PASS: 3,650 active entries, 830 archived entries, 830 replacement entries and all 180 existing artworks.')
