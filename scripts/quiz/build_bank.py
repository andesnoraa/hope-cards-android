"""Rebuild the 100 original additions and 70 recognition questions from reviewed local sources.

No network, runtime generation, external quiz bank, or translation service is used.
Keep the initial 30 original questions. The TSV is the editable multilingual source.
"""
import hashlib
import json
from pathlib import Path
import random
import re
from collections import defaultdict, deque

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
ASSETS = ROOT / 'android/app/src/main/assets'
BANK = ASSETS / 'quiz/questions.json'
LANGUAGES = ['en', 'ml', 'de', 'fr', 'it', 'es', 'fil']
EDITIONS = ['en-bsb', 'ml-mal1910', 'de-lut1912', 'fr-lsg1910', 'it-riv1927', 'es-rv1909', 'tl-adb1905']
ENUMS = ['BSB', 'MAL1910', 'LUT1912', 'LSG1910', 'RIV1927', 'RV1909', 'ADB1905']
PROMPTS = dict(zip(LANGUAGES, [
    'Which reference matches this passage?', 'ഈ വചനഭാഗത്തിന് യോജിക്കുന്ന വേദഭാഗസൂചന ഏത്?',
    'Welche Bibelstelle entspricht diesem Text?', 'Quelle référence correspond à ce passage ?',
    'Quale riferimento corrisponde a questo brano?', '¿Qué referencia corresponde a este pasaje?',
    'Aling talata ang tumutugma sa siping ito?',
]))


def tsv(name):
    return [line.split('|') for line in (HERE / name).read_text().splitlines() if line and not line.startswith('#')]


def seeded(value):
    return random.Random(int(hashlib.sha256(value.encode()).hexdigest(), 16))


def main():
    root = json.loads(BANK.read_text())
    base = [q for q in root['questions'] if q.get('kind') not in {'story', 'verse-reference'}]
    assert len(base) == 30
    labels = {}
    for q in base:
        for i, english in enumerate(q['locales']['en']['options']):
            labels[english] = {lang: q['locales'][lang]['options'][i] for lang in LANGUAGES}
    for row in tsv('answer-labels.tsv'):
        assert len(row) == 7, row
        labels[row[0]] = dict(zip(LANGUAGES, row))
    additions = []
    for row in tsv('story-additions.tsv'):
        assert len(row) == 10, row
        identifier, reference, answers, *prompts = row
        keys = answers.split(';')
        correct = keys[0]
        seeded(identifier).shuffle(keys)
        for key in keys:
            if key.isdigit(): labels[key] = dict.fromkeys(LANGUAGES, key)
            assert key in labels, key
        additions.append(dict(id=identifier, reference=reference, correctIndex=keys.index(correct), kind='story', locales={
            lang: dict(question=prompt, options=[labels[key][lang] for key in keys], explanation='')
            for lang, prompt in zip(LANGUAGES, prompts)
        }))
    assert len(additions) == 100
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
    # Luther 1912 prints these with Hebrew chapter/verse numbering.
    for q in additions:
        if q['id'] == 'jacob-name': q['locales']['de']['reference'] = 'Genesis 32:29'
        if q['id'] == 'jonah-days': q['locales']['de']['reference'] = 'Jonah 2:1'
    questions = base + additions
    assert len(questions) == 200
    assert len({q['id'] for q in questions}) == 200
    for lang in LANGUAGES:
        assert len({q['locales'][lang]['question'] for q in questions}) == 200
        for q in questions:
            assert len(set(q['locales'][lang]['options'])) == 4, (q['id'], lang)
    root.update(contentVersion=2, questions=questions,
                authorship='130 original Hope Cards story questions with scripture references. 70 passage-recognition questions use the already bundled Bible texts. No third-party quiz bank imported.')
    BANK.write_text(json.dumps(root, ensure_ascii=False, indent=2) + '\n')
    print('200 questions in each of seven languages. 130 story questions and 70 passage-recognition questions.')


if __name__ == '__main__': main()
