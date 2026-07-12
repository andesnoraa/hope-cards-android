import verses from "../data/verses/en-bbe.json";

import type { Verse } from "../types/verse";

const verseList = verses as Verse[];

/**
 * Returns every verse.
 */
export function getVerses(): Verse[] {
  return verseList;
}

/**
 * Returns a verse by its ID.
 */
export function getVerseById(
  id: string
): Verse | undefined {
  return verseList.find(
    (verse) => verse.id === id
  );
}

/**
 * Returns a random verse.
 * If excludeId is provided, a different verse is returned.
 */
export function getRandomVerse(
  excludeId?: string
): Verse {
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
  category: string
): Verse[] {
  return verseList.filter(
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
  query: string
): Verse[] {
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
export function getTranslation() {
  return {
    id: "bbe",
    language: "en",
    name: "Bible in Basic English (BBE)",
  };
}