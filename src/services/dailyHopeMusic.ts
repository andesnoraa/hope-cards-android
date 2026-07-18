import type { AudioSource } from "expo-audio";

const CATEGORY_TRACKS: Record<string, AudioSource> = {
  Comfort: require("../../assets/audio/categories/comfort.m4a"),
  Courage: require("../../assets/audio/categories/courage.m4a"),
  Faith: require("../../assets/audio/categories/faith.m4a"),
  Freedom: require("../../assets/audio/categories/freedom.m4a"),
  Grace: require("../../assets/audio/categories/grace.m4a"),
  Hope: require("../../assets/audio/categories/hope.m4a"),
  Joy: require("../../assets/audio/categories/joy.m4a"),
  Life: require("../../assets/audio/categories/life.m4a"),
  Love: require("../../assets/audio/categories/love.m4a"),
  Peace: require("../../assets/audio/categories/peace.m4a"),
  Prayer: require("../../assets/audio/categories/prayer.m4a"),
  Strength: require("../../assets/audio/categories/strength.m4a"),
  Trust: require("../../assets/audio/categories/trust.m4a"),
  Wisdom: require("../../assets/audio/categories/wisdom.m4a"),
};

export function getDailyHopeMusic(
  category: string
): AudioSource {
  return CATEGORY_TRACKS[category] ?? CATEGORY_TRACKS.Hope;
}
