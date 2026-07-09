import AsyncStorage from "@react-native-async-storage/async-storage";

const FAVORITES_KEY = "hope_cards_favorites";

/**
 * Returns all favorite verse IDs.
 */
export async function getFavorites(): Promise<number[]> {
  try {
    const value = await AsyncStorage.getItem(FAVORITES_KEY);

    return value ? JSON.parse(value) : [];
  } catch (error) {
    console.error("Failed to load favorites:", error);

    return [];
  }
}

/**
 * Saves a favorite verse.
 */
export async function saveFavorite(
  verseId: number
): Promise<void> {
  try {
    const favorites = await getFavorites();

    if (!favorites.includes(verseId)) {
      favorites.push(verseId);

      await AsyncStorage.setItem(
        FAVORITES_KEY,
        JSON.stringify(favorites)
      );
    }
  } catch (error) {
    console.error("Failed to save favorite:", error);
  }
}

/**
 * Removes a favorite verse.
 */
export async function removeFavorite(
  verseId: number
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
  } catch (error) {
    console.error("Failed to remove favorite:", error);
  }
}

/**
 * Returns true if the verse is already a favorite.
 */
export async function isFavorite(
  verseId: number
): Promise<boolean> {
  const favorites = await getFavorites();

  return favorites.includes(verseId);
}

/**
 * Toggles a favorite on/off.
 */
export async function toggleFavorite(
  verseId: number
): Promise<boolean> {
  const favorite = await isFavorite(verseId);

  if (favorite) {
    await removeFavorite(verseId);
    return false;
  }

  await saveFavorite(verseId);

  return true;
}