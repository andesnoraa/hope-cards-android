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

import {
  selection,
  success,
} from "../../services/haptics";
import {
  getPremiumStatus,
} from "../../services/premium";
import {
  getSettings,
} from "../../services/settings";
import { useAppTheme } from "../../theme/appTheme";

export default function VerseScreen() {
  const { theme } = useAppTheme();
  const { id } = useLocalSearchParams();

  const verse = getVerseById(id as string);

  const [favorite, setFavorite] =
    useState(false);
  const [
    showSavedVersePattern,
    setShowSavedVersePattern,
  ] = useState(false);

  useEffect(() => {
    async function loadFavorite() {
      if (!verse) return;

      const saved = await isFavorite(
        verse.id
      );
      const settings =
        await getSettings();
      const premiumStatus =
        await getPremiumStatus();

      setFavorite(saved);
      setShowSavedVersePattern(
        saved &&
          premiumStatus.isPremium &&
          settings.savedVersePatternEnabled
      );
    }

    loadFavorite();
  }, [verse]);

  async function handleToggleFavorite() {
    if (!verse) return;

    const saved = await toggleFavorite(
      verse.id
    );

    setFavorite(saved);

    const settings =
      await getSettings();
    const premiumStatus =
      await getPremiumStatus();

    setShowSavedVersePattern(
      saved &&
        premiumStatus.isPremium &&
        settings.savedVersePatternEnabled
    );

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

  return (
    <>
      <Stack.Screen
        options={{
          title: "Verse",
        }}
      />

      <ScrollView
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
          style={[
            styles.contentWrapper,
            showSavedVersePattern &&
              styles.patternWrapper,
          ]}
        >
          {showSavedVersePattern ? (
            <View
              pointerEvents="none"
              style={styles.pattern}
            >
              {[0, 1, 2, 3, 4].map(
                (index) => (
                  <View
                    key={index}
                    style={[
                      styles.patternLine,
                      {
                        backgroundColor:
                          theme.accent,
                        top:
                          22 +
                          index * 52,
                        right:
                          -24 +
                          index * 24,
                      },
                    ]}
                  />
                )
              )}

              {[0, 1, 2, 3].map(
                (index) => (
                  <View
                    key={`dot-${index}`}
                    style={[
                      styles.patternDot,
                      {
                        borderColor:
                          theme.accent,
                        top:
                          34 +
                          index * 66,
                        left:
                          18 +
                          index * 44,
                      },
                    ]}
                  />
                )
              )}
            </View>
          ) : null}

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
            style={[
              styles.verse,
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
    paddingHorizontal: 28,
    paddingTop: 20,
    paddingBottom: 60,
    alignItems: "center",
  },

  contentWrapper: {
    width: "100%",
    alignItems: "center",
  },

  patternWrapper: {
    position: "relative",
    overflow: "hidden",
    borderRadius: 28,
    paddingHorizontal: 18,
    paddingVertical: 22,
  },

  pattern: {
    position: "absolute",
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    opacity: 0.08,
  },

  patternLine: {
    position: "absolute",
    width: 140,
    height: 2,
    borderRadius: 2,
    transform: [
      {
        rotate: "-28deg",
      },
    ],
  },

  patternDot: {
    position: "absolute",
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 2,
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
    fontSize: 23,
    lineHeight: 44,
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
