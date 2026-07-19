import bbeVerses from "../data/verses/en-bbe.json";
import bsbVerses from "../data/verses/en-bsb.json";
import kjvVerses from "../data/verses/en-kjv.json";
import webVerses from "../data/verses/en-web.json";

import type { Verse } from "../types/verse";

type TranslationDefinition = {
  attribution: string;
  language: string;
  label: string;
  license: string;
  name: string;
  verses: Verse[];
};

// To add a translation, import its verse JSON above and add
// one entry here. Settings, validation, and verse lookup derive
// their supported translations from this registry.
const translationRegistry = {
  bsb: {
    attribution: "Berean Bible Translation Committee",
    language: "en",
    label: "BSB",
    license: "Public domain",
    name: "Berean Standard Bible",
    verses: bsbVerses as Verse[],
  },
  bbe: {
    attribution: "Professor S. H. Hooke",
    language: "en",
    label: "BBE",
    license: "Public domain",
    name: "Bible in Basic English",
    verses: bbeVerses as Verse[],
  },
  kjv: {
    attribution: "King James Version translators",
    language: "en",
    label: "KJV",
    license: "Public domain",
    name: "King James Version",
    verses: kjvVerses as Verse[],
  },
  web: {
    attribution: "World English Bible project",
    language: "en",
    label: "WEB",
    license: "Public domain",
    name: "World English Bible",
    verses: webVerses as Verse[],
  },
} satisfies Record<string, TranslationDefinition>;

export type TranslationId =
  keyof typeof translationRegistry;

export const DEFAULT_TRANSLATION: TranslationId = "bsb";

export const TRANSLATION_OPTIONS = (
  Object.keys(translationRegistry) as TranslationId[]
).map((id) => ({
  id,
  attribution: translationRegistry[id].attribution,
  language: translationRegistry[id].language,
  label: translationRegistry[id].label,
  license: translationRegistry[id].license,
  name: translationRegistry[id].name,
}));

export function isTranslationId(
  value: unknown
): value is TranslationId {
  return (
    typeof value === "string" &&
    Object.hasOwn(translationRegistry, value)
  );
}

/**
 * Returns every verse.
 */
export function getVerses(
  translation: TranslationId = DEFAULT_TRANSLATION
): Verse[] {
  return translationRegistry[translation].verses;
}

/**
 * Returns a verse by its ID.
 */
export function getVerseById(
  id: string,
  translation: TranslationId = DEFAULT_TRANSLATION
): Verse | undefined {
  return getVerses(translation).find(
    (verse) => verse.id === id
  );
}

/**
 * Returns a random verse.
 * If excludeId is provided, a different verse is returned.
 */
export function getRandomVerse(
  excludeId?: string,
  translation: TranslationId = DEFAULT_TRANSLATION
): Verse {
  const verseList = getVerses(translation);
  if (verseList.length === 0) {
    throw new Error("No verses available.");
  }

  if (verseList.length === 1) {
    return verseList[0];
  }

  let verse: Verse;

  do {
    verse =
      verseList[
      Math.floor(
        Math.random() *
        verseList.length
      )
      ];
  } while (
    excludeId &&
    verse.id === excludeId
  );

  return verse;
}

/**
 * Returns all verses in a category.
 */
export function getVersesByCategory(
  category: string,
  translation: TranslationId = DEFAULT_TRANSLATION
): Verse[] {
  return getVerses(translation).filter(
    (verse) =>
      verse.category.toLowerCase() ===
      category.toLowerCase()
  );
}

/**
 * Searches verses by text, reference,
 * category or tags.
 */
export function searchVerses(
  query: string,
  translation: TranslationId = DEFAULT_TRANSLATION
): Verse[] {
  const verseList = getVerses(translation);
  const q = query.trim().toLowerCase();

  if (!q) {
    return verseList;
  }

  return verseList.filter((verse) => {
    return (
      verse.verse
        .toLowerCase()
        .includes(q) ||
      verse.reference
        .toLowerCase()
        .includes(q) ||
      verse.category
        .toLowerCase()
        .includes(q) ||
      verse.tags.some((tag) =>
        tag.toLowerCase().includes(q)
      )
    );
  });
}

/**
 * Returns the translation currently loaded.
 */
export function getTranslation(
  id: TranslationId = DEFAULT_TRANSLATION
) {
  const translation = translationRegistry[id];

  return {
    id,
    language: translation.language,
    name: `${translation.name} (${translation.label})`,
  };
}
