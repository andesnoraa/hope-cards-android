import { Share } from "react-native";

import type { Verse } from "../types/verse";

export async function shareVerse(
  verse: Verse
) {
  try {
    await Share.share({
      title: verse.reference,

      message: `${verse.verse}

— ${verse.reference} (${verse.translation})

Shared from Hope Cards ❤️

https://play.google.com/store/apps/details?id=com.hopecards.app`,
    });
  } catch (error) {
    console.error(
      "Failed to share verse:",
      error
    );
  }
}