"""Rebuild 400 questions from editable multilingual sources and bundled scripture.

No network, runtime generation, external quiz bank, or translation service is used.
Keep the initial 30 original questions. The TSV is the editable multilingual source.
"""
import hashlib
import json
from pathlib import Path
import random
import re
import unicodedata
from collections import defaultdict, deque

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / 'android/app/src/main/assets'
BANK = ASSETS / 'quiz/questions.json'
LANGUAGES = ['en', 'ml', 'de', 'fr', 'it', 'es', 'fil']
EDITIONS = ['en-bsb', 'ml-mal1910', 'de-lut1912', 'fr-lsg1910', 'it-riv1927', 'es-rv1909', 'tl-adb1905']
ENUMS = ['BSB', 'MAL1910', 'LUT1912', 'LSG1910', 'RIV1927', 'RV1909', 'ADB1905']
NEW_BATCHES = [('early-ot', 70), ('late-ot', 60), ('nt', 70)]
QUESTION_COUNT = 400
PROMPTS = dict(zip(LANGUAGES, [
    'Which reference matches this passage?', 'ഈ വചനഭാഗത്തിന് യോജിക്കുന്ന വേദഭാഗസൂചന ഏത്?',
    'Welche Bibelstelle entspricht diesem Text?', 'Quelle référence correspond à ce passage ?',
    'Quale riferimento corrisponde a questo brano?', '¿Qué referencia corresponde a este pasaje?',
    'Aling talata ang tumutugma sa siping ito?',
]))
# Luther 1912 uses different printed numbering for these passages.
REFERENCE_OVERRIDES = {
    'jacob-name': {'de': 'Genesis 32:29'},
    'jonah-days': {'de': 'Jonah 2:1'},
    'v3-egypt-frogs': {'de': 'Exodus 8:2'},
    'v3-aaron-staff-fruit': {'de': 'Numbers 17:23'},
    'v3-david-cave-proof': {'de': '1 Samuel 24:5'},
    'v3-wall-builders-swords': {'de': 'Nehemiah 4:12'},
    'v3-daniel-daily-prayer': {'de': 'Daniel 6:11'},
}


def tsv(name):
    return [line.split('|') for line in (HERE / name).read_text().splitlines() if line and not line.startswith('#')]


def seeded(value):
    return random.Random(int(hashlib.sha256(value.encode()).hexdigest(), 16))


def normalized(text):
    """Catch duplicates hidden by capitalization, punctuation, or Unicode variants."""
    text = unicodedata.normalize('NFKC', text).casefold()
    return ''.join(c for c in text if not c.isspace() and not unicodedata.category(c).startswith('P'))


def reference_span(reference):
    match = re.fullmatch(r'(.+) (\d+):(\d+)(?:[-–](\d+))?', reference)
    assert match, ('Invalid reference', reference)
    book, chapter, first, last = match.groups()
    first, last = int(first), int(last or first)
    assert 0 < first <= last and int(chapter) > 0, ('Invalid verse range', reference)
    return ('Psalms' if book == 'Psalm' else book, int(chapter), first, last)


def read_labels(name, inherited):
    labels = dict(inherited)
    for row in tsv(name):
        assert len(row) == len(LANGUAGES) and all(value.strip() for value in row), (name, row)
        assert all(value == value.strip() for value in row), (name, row)
        labels[row[0]] = dict(zip(LANGUAGES, row))
    return labels


def story_questions(name, labels, expected_count):
    questions = []
    for row in tsv(name):
        assert len(row) == 10 and all(value.strip() for value in row), (name, row)
        identifier, reference, answers, *prompts = row
        keys = answers.split(';')
        assert len(keys) == 4 and len(set(keys)) == 4, (name, identifier, keys)
        correct = keys[0]
        seeded(identifier).shuffle(keys)
        for key in keys:
            if key.isdigit(): labels[key] = dict.fromkeys(LANGUAGES, key)
            assert key in labels, (name, identifier, key)
        questions.append(dict(id=identifier, reference=reference, correctIndex=keys.index(correct), kind='story', locales={
            lang: dict(question=prompt, options=[labels[key][lang] for key in keys], explanation='')
            for lang, prompt in zip(LANGUAGES, prompts)
        }))
    assert len(questions) == expected_count, (name, len(questions), expected_count)
    return questions


def validate(questions):
    assert len(questions) == QUESTION_COUNT
    assert len({q['id'] for q in questions}) == QUESTION_COUNT, 'Duplicate question IDs'
    passages = [q for q in questions if q.get('kind') == 'verse-reference']
    assert len({q['reference'] for q in passages}) == len(passages), 'Duplicate passage references'
    passage_spans = [(q['id'], reference_span(q['reference'])) for q in passages]
    for q in questions:
        span = reference_span(q['reference'])
        if q.get('kind') != 'verse-reference':
            for passage_id, other in passage_spans:
                overlaps = span[:2] == other[:2] and max(span[2], other[2]) <= min(span[3], other[3])
                assert not overlaps, ('Story reuses a recognition passage', q['id'], passage_id)
    for lang in LANGUAGES:
        seen = {}
        for q in questions:
            assert set(q['locales']) == set(LANGUAGES), q['id']
            assert q['reference'].strip() and 0 <= q['correctIndex'] < 4, q['id']
            text = q['locales'][lang]
            prompt = normalized(text['question'])
            assert prompt and prompt not in seen, ('Duplicate prompt', lang, seen.get(prompt), q['id'])
            seen[prompt] = q['id']
            assert len(text['options']) == 4 and all(option.strip() for option in text['options']), (q['id'], lang)
            assert len({normalized(option) for option in text['options']}) == 4, ('Duplicate options', q['id'], lang)
            assert isinstance(text['explanation'], str), (q['id'], lang)


def main():
    root = json.loads(BANK.read_text())
    base = [q for q in root['questions'] if q.get('kind') not in {'story', 'verse-reference'}]
    assert len(base) == 30
    labels = {}
    for q in base:
        for i, english in enumerate(q['locales']['en']['options']):
            labels[english] = {lang: q['locales'][lang]['options'][i] for lang in LANGUAGES}
    labels = read_labels('answer-labels.tsv', labels)
    additions = story_questions('story-additions.tsv', labels, 100)
    scripture = {lang: {v['id']: v for v in json.loads((ASSETS / 'verses' / (edition + '.json')).read_text())}
                 for lang, edition in zip(LANGUAGES, EDITIONS)}
    kotlin = (ROOT / 'android/app/src/main/java/com/aaronsedna/hopecards/model/BibleReferenceFormatter.kt').read_text().split('private val bookTitles = ')[1]
    books = {edition: dict(re.findall(r'"([^"]+)" to "([^"]+)"', block)) for edition, block in
             re.findall(r'Translation\.(\w+) to mapOf\((.*?)\n        \)', kotlin, re.S)}
    def localized_ref(ref, lang):
        book, position = ref.rsplit(' ', 1)
        return ref if lang == 'en' else books[ENUMS[LANGUAGES.index(lang)]][book.replace('Psalm', 'Psalms') if book == 'Psalm' else book] + ' ' + position
    candidates = []
    for identifier, verse in scripture['en'].items():
        if len(verse['verse']) > 220 or len(verse['verse'].split()) < 8: continue
        if not all(identifier in scripture[lang] and scripture[lang][identifier]['reference'] == verse['reference'] and
                   len(scripture[lang][identifier]['verse']) <= 650 for lang in LANGUAGES): continue
        candidates.append(identifier)
    # Select passages across books instead of taking a block from one book or category.
    groups = defaultdict(deque)
    seeded('passage-pool-v2').shuffle(candidates)
    for identifier in candidates: groups[scripture['en'][identifier]['reference'].rsplit(' ', 1)[0]].append(identifier)
    selected = []
    while len(selected) < 70:
        for group in groups.values():
            if group and len(selected) < 70: selected.append(group.popleft())
        assert any(groups.values()) or len(selected) == 70, 'Not enough distinct passages'
    for identifier in selected:
        source = scripture['en'][identifier]
        distractors = [other for other in candidates if scripture['en'][other]['reference'] != source['reference'] and
                       all(scripture[lang][other]['verse'] != scripture[lang][identifier]['verse'] for lang in LANGUAGES)]
        options = [identifier] + seeded(identifier + '-distractors').sample(distractors, 3)
        seeded(identifier + '-options').shuffle(options)
        additions.append(dict(id='passage-' + identifier, reference=source['reference'], correctIndex=options.index(identifier),
            kind='verse-reference', sourceVerseId=identifier, locales={lang: dict(
                question=PROMPTS[lang] + '\n\n“' + scripture[lang][identifier]['verse'] + '”',
                options=[localized_ref(scripture[lang][option]['reference'], lang) for option in options], explanation='')
                for lang in LANGUAGES}))
    questions = base + additions
    # Append new content after the original 200, preserving their IDs and answer order.
    for batch, count in NEW_BATCHES:
        batch_labels = read_labels(f'labels-v3-{batch}.tsv', labels)
        questions.extend(story_questions(f'story-v3-{batch}.tsv', batch_labels, count))
    for q in questions:
        for lang, reference in REFERENCE_OVERRIDES.get(q['id'], {}).items():
            q['locales'][lang]['reference'] = reference
    validate(questions)
    root.update(contentVersion=3, questions=questions,
                authorship='330 original Hope Cards story questions with scripture references. 70 passage-recognition questions use the already bundled Bible texts. No third-party quiz bank imported.')
    BANK.write_text(json.dumps(root, ensure_ascii=False, indent=2) + '\n')
    print('400 questions in each of seven languages. 330 story questions and 70 passage-recognition questions.')


if __name__ == '__main__': main()
