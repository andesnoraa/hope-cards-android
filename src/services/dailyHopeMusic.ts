import type { AudioSource } from "expo-audio";

const STILL_WATERS = require("../../assets/audio/still-waters.mp3");

const CATEGORY_TRACKS: Partial<
  Record<string, AudioSource>
> = {};

export function getDailyHopeMusic(
  category: string
): AudioSource {
  return (
    CATEGORY_TRACKS[category] ??
    STILL_WATERS
  );
}
