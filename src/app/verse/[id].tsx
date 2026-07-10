import { Stack, useLocalSearchParams } from "expo-router";
import { useEffect, useState } from "react";

import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import Animated, {
  FadeIn,
} from "react-native-reanimated";

import ActionButton from "../../components/common/ActionButton";

import {
  isFavorite,
  toggleFavorite,
} from "../../services/favorites";

import { shareVerse } from "../../services/share";

import {
  getVerseById,
} from "../../services/verseService";

export default function VerseScreen() {
  const { id } = useLocalSearchParams();

  const verse = getVerseById(id as string);

  const [favorite, setFavorite] =
    useState(false);

  useEffect(() => {
    async function loadFavorite() {
      if (!verse) return;

      const saved = await isFavorite(
        verse.id
      );

      setFavorite(saved);
    }

    loadFavorite();
  }, [verse]);

  async function handleToggleFavorite() {
    if (!verse) return;

    const saved = await toggleFavorite(
      verse.id
    );

    setFavorite(saved);
  }

  async function handleShare() {
    if (!verse) return;

    await shareVerse(verse);
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
        <Animated.View
          entering={FadeIn.duration(300)}
          style={styles.contentWrapper}
        >
          <Text style={styles.category}>
            {verse.category.toUpperCase()}
          </Text>

          <View style={styles.divider} />

          <Text style={styles.reference}>
            {verse.reference}
          </Text>

          <Text style={styles.verse}>
            {verse.verse}
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

          <View
            style={styles.translationSection}
          >
            <Text
              style={
                styles.translationLabel
              }
            >
              Translation
            </Text>

            <Text
              style={
                styles.translationText
              }
            >
              {verse.translation}
            </Text>
          </View>
        </Animated.View>
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

  contentWrapper: {
    width: "100%",
    alignItems: "center",
  },

  category: {
    fontSize: 16,
    fontWeight: "600",
    color: "#C5A24C",
    letterSpacing: 3,
    marginBottom: 10,
  },

  divider: {
    width: 80,
    height: 3,
    borderRadius: 2,
    backgroundColor: "#D4AF37",
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
    fontSize: 23,
    lineHeight: 44,
    color: "#273043",
    textAlign: "center",
    fontFamily:
      "SourceSerif4_400Regular",
  },

  actions: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    gap: 12,
    marginTop: 28,
    marginBottom: 18,
  },

  translationSection: {
    marginTop: 34,
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