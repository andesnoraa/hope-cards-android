import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import Animated, {
  FadeIn,
  FadeInDown,
} from "react-native-reanimated";

import ActionButton from "../../components/common/ActionButton";

import {
  getDailyHope,
} from "../../services/dailyHope";

import {
  isFavorite,
  subscribeToFavorites,
  toggleFavorite,
} from "../../services/favorites";

import { shareVerse } from "../../services/share";

import {
  lightImpact,
  selection,
  success,
} from "../../services/haptics";

import type { Verse } from "../../types/verse";
import { useAppTheme } from "../../theme/appTheme";

export default function DailyHopeScreen() {
  const { theme } = useAppTheme();

  const [verse, setVerse] =
    useState<Verse | null>(null);

  const [favorite, setFavorite] =
    useState(false);

  const today = useMemo(() => {
    const parts =
      new Intl.DateTimeFormat("en-GB", {
        weekday: "long",
        day: "numeric",
        month: "long",
      }).formatToParts(new Date());

    const weekday =
      parts.find(
        (p) => p.type === "weekday"
      )?.value ?? "";

    const day =
      parts.find(
        (p) => p.type === "day"
      )?.value ?? "";

    const month =
      parts.find(
        (p) => p.type === "month"
      )?.value ?? "";

    return `${weekday} • ${day} ${month}`;
  }, []);

  useEffect(() => {
    async function load() {
      const todayVerse =
        await getDailyHope();

      setVerse(todayVerse);

      const saved =
        await isFavorite(todayVerse.id);

      setFavorite(saved);

      setTimeout(() => {
        lightImpact();
      }, 1300);
    }

    load();
  }, []);

  useEffect(() => {
    if (!verse) {
      return;
    }

    const unsubscribe =
      subscribeToFavorites(async () => {
        const saved =
          await isFavorite(
            verse.id
          );

        setFavorite(saved);
      });

    return unsubscribe;
  }, [verse]);

  async function handleFavorite() {
    if (!verse) return;

    const saved =
      await toggleFavorite(
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
    return null;
  }

  return (
    <ScrollView
      style={[
        styles.container,
        {
          backgroundColor:
            theme.background,
        },
      ]}
      contentContainerStyle={
        styles.content
      }
      showsVerticalScrollIndicator={false}
    >
      <Animated.View
        entering={FadeIn.duration(600)}
        style={styles.header}
      >
        <Text
          style={[
            styles.title,
            { color: theme.text },
          ]}
        >
          Today's Hope
        </Text>

        <Animated.Text
          entering={FadeIn.delay(300).duration(
            600
          )}
          style={[
            styles.date,
            {
              color:
                theme.textSecondary,
            },
          ]}
        >
          {today}
        </Animated.Text>
      </Animated.View>

      <Animated.View
        entering={FadeInDown.delay(900).duration(
          1000
        )}
        style={styles.verseContainer}
      >
        <Text
          style={[
            styles.reference,
            { color: theme.text },
          ]}
        >
          {verse.reference}
        </Text>

        <View
          style={[
            styles.separator,
            {
              backgroundColor:
                theme.accent,
            },
          ]}
        />

        <Text
          style={[
            styles.verse,
            { color: theme.cardText },
          ]}
        >
          {verse.verse}
        </Text>
      </Animated.View>

      <Animated.View
        entering={FadeIn.delay(1900).duration(
          600
        )}
        style={styles.actions}
      >
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
          onPress={handleFavorite}
        />

        <ActionButton
          icon="share-outline"
          label="Share"
          color={theme.accent}
          onPress={handleShare}
        />
      </Animated.View>

      <Animated.Text
        entering={FadeIn.delay(2100).duration(
          600
        )}
        style={[
          styles.translation,
          {
            color: theme.textTertiary,
          },
        ]}
      >
        {verse.translation}
      </Animated.Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  content: {
    alignItems: "center",
    paddingHorizontal: 28,
    paddingTop: 32,
    paddingBottom: 60,
  },

  header: {
    alignItems: "center",
    marginBottom: 24,
  },

  title: {
    fontSize: 36,
    fontWeight: "700",
  },

  date: {
    marginTop: 8,
    fontSize: 16,
    letterSpacing: 0.4,
  },

  verseContainer: {
    width: "82%",
    alignItems: "center",
    marginTop: 12,
  },

  reference: {
    fontSize: 28,
    fontWeight: "700",
    textAlign: "center",
    marginBottom: 14,
  },

  separator: {
    width: 56,
    height: 2,
    borderRadius: 2,
    opacity: 0.75,
    marginBottom: 36,
  },

  verse: {
    fontSize: 22,
    lineHeight: 52,
    textAlign: "center",
    fontFamily:
      "CormorantGaramond-Regular",
  },

  actions: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    gap: 12,
    marginTop: 60,
  },

  translation: {
    marginTop: 48,
    fontSize: 14,
    textAlign: "center",
    letterSpacing: 0.5,
  },
});
