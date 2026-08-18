import { Share } from "react-native";
import * as Sharing from "expo-sharing";

import type { Verse } from "../types/verse";

export async function shareVerse(
  verse: Verse
) {
  try {
    await Share.share({
      title: verse.reference,

      message: `${verse.verse}

— ${verse.reference} • ${verse.translation}

Shared from Hope Cards ❤️

https://play.google.com/store/apps/details?id=com.aaronsedna.hopecards`,
    });
  } catch (error) {
    console.error(
      "Failed to share verse:",
      error
    );
  }
}

export async function shareVerseImage(
  imageUri: string,
  verse: Verse
) {
  const isAvailable =
    await Sharing.isAvailableAsync();

  if (!isAvailable) {
    await shareVerse(verse);
    return;
  }

  await Sharing.shareAsync(imageUri, {
    mimeType: "image/png",
    dialogTitle: `Share ${verse.reference}`,
    UTI: "public.png",
  });
}
