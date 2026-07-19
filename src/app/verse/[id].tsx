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
import { getSettings } from "../../services/settings";
import type { Verse } from "../../types/verse";

import {
  selection,
  success,
} from "../../services/haptics";
import { useAppTheme } from "../../theme/appTheme";

function getVerseStyle(text: string) {
  const length = text.length;

  if (length <= 90) {
    return {
      fontSize: 24,
      lineHeight: 40,
    };
  }

  if (length <= 150) {
    return {
      fontSize: 22,
      lineHeight: 37,
    };
  }

  if (length <= 220) {
    return {
      fontSize: 20,
      lineHeight: 34,
    };
  }

  return {
    fontSize: 18,
    lineHeight: 31,
  };
}

export default function VerseScreen() {
  const { theme } = useAppTheme();
  const { id } = useLocalSearchParams();

  const [verse, setVerse] =
    useState<Verse | undefined>();

  const [favorite, setFavorite] =
    useState(false);

  useEffect(() => {
    async function loadVerse() {
      const settings = await getSettings();

      setVerse(
        getVerseById(
          id as string,
          settings.preferredTranslation
        )
      );
    }

    loadVerse();
  }, [id]);

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

    if (saved) {
      await success();
    } else {
      await selection();
    }
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

        <View
          style={[
            styles.container,
            {
              backgroundColor:
                theme.background,
            },
          ]}
        >
          <Text
            style={[
              styles.title,
              { color: theme.text },
            ]}
          >
            Verse not found
          </Text>
        </View>
      </>
    );
  }

  const verseStyle = getVerseStyle(verse.verse);

  return (
    <>
      <Stack.Screen
        options={{
          title: "Verse",
        }}
      />

      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={[
          styles.container,
          {
            backgroundColor:
              theme.background,
          },
        ]}
        contentContainerStyle={styles.content}
      >
        <Animated.View
          entering={FadeIn.duration(300)}
          style={styles.contentWrapper}
        >
          <Text
            style={[
              styles.category,
              { color: theme.accent },
            ]}
          >
            {verse.category.toUpperCase()}
          </Text>

          <View
            style={[
              styles.divider,
              {
                backgroundColor:
                  theme.accent,
              },
            ]}
          />

          <Text
            style={[
              styles.reference,
              { color: theme.text },
            ]}
          >
            {verse.reference}
          </Text>

          <Text
            selectable
            style={[
              styles.verse,
              verseStyle,
              { color: theme.cardText },
            ]}
          >
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
                  ? theme.danger
                  : theme.accent
              }
              onPress={handleToggleFavorite}
            />

            <ActionButton
              icon="share-outline"
              label="Share"
              color={theme.accent}
              onPress={handleShare}
            />
          </View>

          <View
            style={styles.translationSection}
          >
            <Text
              style={[
                styles.translationLabel,
                {
                  color:
                    theme.textTertiary,
                },
              ]}
            >
              Translation
            </Text>

            <Text
              style={[
                styles.translationText,
                { color: theme.text },
              ]}
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
  },

  content: {
    flexGrow: 1,
    justifyContent: "center",
    paddingHorizontal: 28,
    paddingTop: 32,
    paddingBottom: 220,
    alignItems: "center",
  },

  contentWrapper: {
    width: "100%",
    alignItems: "center",
  },

  category: {
    fontSize: 16,
    fontWeight: "600",
    letterSpacing: 3,
    marginBottom: 10,
  },

  divider: {
    width: 80,
    height: 3,
    borderRadius: 2,
    marginBottom: 26,
  },

  reference: {
    fontSize: 30,
    fontWeight: "700",
    textAlign: "center",
  },

  verse: {
    marginTop: 34,
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
    letterSpacing: 1.5,
    textTransform: "uppercase",
    marginBottom: 8,
  },

  translationText: {
    fontSize: 18,
    fontWeight: "600",
    textAlign: "center",
  },

  title: {
    fontSize: 28,
    fontWeight: "700",
    textAlign: "center",
  },
});
