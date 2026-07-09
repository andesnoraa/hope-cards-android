import { Stack, useLocalSearchParams } from "expo-router";
import { useEffect, useState } from "react";

import {
    ScrollView,
    Share,
    StyleSheet,
    Text,
    View,
} from "react-native";

import { verses } from "../../data/verses";

import ActionButton from "../../components/common/ActionButton";

import {
    isFavorite,
    toggleFavorite,
} from "../../services/favorites";

export default function VerseScreen() {
  const { id } = useLocalSearchParams();

  const verse = verses.find(
    (v) => v.id === Number(id)
  );

  const [favorite, setFavorite] = useState(false);

  useEffect(() => {
    async function loadFavorite() {
      if (!verse) return;

      const saved = await isFavorite(verse.id);

      setFavorite(saved);
    }

    loadFavorite();
  }, [verse]);

  async function handleToggleFavorite() {
    if (!verse) return;

    const saved = await toggleFavorite(verse.id);

    setFavorite(saved);
  }

  async function handleShare() {
    if (!verse) return;

    await Share.share({
      message:
        `${verse.fullVerse}\n\n` +
        `— ${verse.reference} (${verse.translation})\n\n` +
        `Shared from Hope Cards ❤️\n\n` +
        `https://play.google.com/store/apps/details?id=com.hopecards.app`,
    });
  }

  if (!verse) {
    return (
      <>
        <Stack.Screen
          options={{
            title: "Verse",
          }}
        />

        <View style={styles.container}>
          <Text style={styles.title}>
            Verse not found
          </Text>
        </View>
      </>
    );
  }

  return (
    <>
      <Stack.Screen
        options={{
          title: "Verse",
        }}
      />

      <ScrollView
        style={styles.container}
        contentContainerStyle={styles.content}
      >
        <Text style={styles.category}>
          {verse.category.toUpperCase()}
        </Text>

        <View style={styles.divider} />

        <Text style={styles.reference}>
          {verse.reference}
        </Text>

        <Text style={styles.verse}>
          {verse.fullVerse}
        </Text>

        <View style={styles.actions}>
          <ActionButton
            icon={
              favorite
                ? "heart"
                : "heart-outline"
            }
            label={
              favorite
                ? "Saved"
                : "Save"
            }
            color={
              favorite
                ? "#C0392B"
                : "#C5A24C"
            }
            onPress={handleToggleFavorite}
          />

          <ActionButton
            icon="share-outline"
            label="Share"
            color="#C5A24C"
            onPress={handleShare}
          />
        </View>

        <View style={styles.translationSection}>
          <Text style={styles.translationLabel}>
            Translation
          </Text>

          <Text style={styles.translationText}>
            {verse.translation}
          </Text>
        </View>
      </ScrollView>
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8F6F2",
  },

  content: {
    paddingHorizontal: 28,
    paddingTop: 20,
    paddingBottom: 60,
    alignItems: "center",
  },

  category: {
    fontSize: 16,
    fontWeight: "600",
    color: "#C5A24C",
    letterSpacing: 3,
  },

  divider: {
    width: 70,
    height: 3,
    borderRadius: 2,
    backgroundColor: "#D4AF37",
    marginTop: 12,
    marginBottom: 26,
  },

  reference: {
    fontSize: 30,
    fontWeight: "700",
    color: "#1A2747",
    textAlign: "center",
  },

  verse: {
    marginTop: 34,

    fontSize: 24,
    lineHeight: 42,

    color: "#273043",

    textAlign: "center",

    fontFamily: "SourceSerif4_400Regular",
  },

  actions: {
    flexDirection: "row",

    justifyContent: "center",

    alignItems: "center",

    gap: 12,

    marginTop: 34,

    marginBottom: 12,
  },

  translationSection: {
    marginTop: 42,
    alignItems: "center",
  },

  translationLabel: {
    fontSize: 13,

    color: "#8A8A8A",

    letterSpacing: 1.5,

    textTransform: "uppercase",

    marginBottom: 8,
  },

  translationText: {
    fontSize: 18,

    fontWeight: "600",

    color: "#1A2747",

    textAlign: "center",
  },

  title: {
    fontSize: 28,

    fontWeight: "700",

    color: "#1A2747",

    textAlign: "center",
  },
});