import AsyncStorage from "@react-native-async-storage/async-storage";

import {
  getVerses,
} from "./verseService";

import type { Verse } from "../types/verse";

const FAVORITES_KEY = "hope_cards_favorites";

type FavoritesListener = () => void;

const listeners = new Set<FavoritesListener>();

function notifyFavoritesChanged() {
  listeners.forEach((listener) => listener());
}

export function subscribeToFavorites(
  listener: FavoritesListener
) {
  listeners.add(listener);

  return () => {
    listeners.delete(listener);
  };
}

/**
 * Returns all favorite verse IDs.
 */
export async function getFavorites(): Promise<string[]> {
  try {
    const value = await AsyncStorage.getItem(
      FAVORITES_KEY
    );

    return value ? JSON.parse(value) : [];
  } catch (error) {
    console.error(
      "Failed to load favorites:",
      error
    );

    return [];
  }
}

/**
 * Saves a favorite verse.
 */
export async function saveFavorite(
  verseId: string
): Promise<void> {
  try {
    const favorites = await getFavorites();

    if (!favorites.includes(verseId)) {
      favorites.push(verseId);

      await AsyncStorage.setItem(
        FAVORITES_KEY,
        JSON.stringify(favorites)
      );

      notifyFavoritesChanged();
    }
  } catch (error) {
    console.error(
      "Failed to save favorite:",
      error
    );
  }
}

/**
 * Removes a favorite verse.
 */
export async function removeFavorite(
  verseId: string
): Promise<void> {
  try {
    const favorites = await getFavorites();

    const updated = favorites.filter(
      (id) => id !== verseId
    );

    await AsyncStorage.setItem(
      FAVORITES_KEY,
      JSON.stringify(updated)
    );

    notifyFavoritesChanged();
  } catch (error) {
    console.error(
      "Failed to remove favorite:",
      error
    );
  }
}

/**
 * Returns true if the verse is already a favorite.
 */
export async function isFavorite(
  verseId: string
): Promise<boolean> {
  const favorites = await getFavorites();

  return favorites.includes(verseId);
}

/**
 * Toggles a favorite on/off.
 */
export async function toggleFavorite(
  verseId: string
): Promise<boolean> {
  const favorite = await isFavorite(verseId);

  if (favorite) {
    await removeFavorite(verseId);
    return false;
  }

  await saveFavorite(verseId);

  return true;
}

/**
 * Returns all saved verses.
 */
export async function getFavoriteVerses(): Promise<
  Verse[]
> {
  const favoriteIds = await getFavorites();

  const verses = getVerses();

  return verses.filter((verse) =>
    favoriteIds.includes(verse.id)
  );
}