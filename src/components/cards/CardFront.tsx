import { StyleSheet, Text, View } from "react-native";

import type { Verse } from "../../types/verse";
import { useAppTheme } from "../../theme/appTheme";

import ActionButton from "../common/ActionButton";
import { CARD_HEIGHT, CARD_WIDTH } from "./cardDimensions";

type Props = {
  verse: Verse;

  favorite?: boolean;

  onToggleFavorite?: () => void;

  onShare?: () => void;

  showActions?: boolean;
};

function getVerseStyle(text: string) {
  const length = text.length;

  if (length <= 70) {
    return {
      fontSize: 24,
      lineHeight: 34,
    };
  }

  if (length <= 120) {
    return {
      fontSize: 22,
      lineHeight: 32,
    };
  }

  if (length <= 180) {
    return {
      fontSize: 20,
      lineHeight: 30,
    };
  }

  return {
    fontSize: 18,
    lineHeight: 28,
  };
}

export default function CardFront({
  verse,
  favorite = false,
  onToggleFavorite,
  onShare,
  showActions = true,
}: Props) {
  const { theme } = useAppTheme();

  const verseStyle = getVerseStyle(verse.verse);

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor:
            theme.cardFront,
          borderColor: theme.accent,
          shadowColor: theme.shadow,
        },
      ]}
    >
      <View
        style={[
          styles.innerBorder,
          {
            borderColor:
              theme.accentLine,
          },
        ]}
      />

      <View style={styles.content}>
        <View style={styles.header}>
          <Text
            style={[
              styles.category,
              { color: theme.accent },
            ]}
          >
            {verse.category}
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
        </View>

        <View style={styles.verseContainer}>
          <Text
            style={[
              styles.verse,
              verseStyle,
              { color: theme.cardText },
            ]}
          >
            {verse.verse}
          </Text>
        </View>

        {showActions && (
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
              onPress={onToggleFavorite!}
            />

            <ActionButton
              icon="share-outline"
              label="Share"
              color={theme.accent}
              onPress={onShare!}
            />
          </View>
        )}

        <View style={styles.footer}>
          <Text
            style={[
              styles.reference,
              {
                color: theme.cardText,
              },
            ]}
          >
            {verse.reference}
          </Text>

          <Text
            style={[
              styles.translation,
              {
                color: theme.cardMuted,
              },
            ]}
          >
            {verse.translation}
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,

    borderRadius: 34,

    borderWidth: 1.5,
    shadowOpacity: 0.12,
    shadowRadius: 42,
    shadowOffset: {
      width: 0,
      height: 24,
    },

    elevation: 18,
  },

  innerBorder: {
    position: "absolute",

    top: 12,
    left: 12,
    right: 12,
    bottom: 12,

    borderRadius: 26,

    borderWidth: 1,

  },

  content: {
    flex: 1,

    paddingHorizontal: 28,
    paddingTop: 30,
    paddingBottom: 24,
  },

  header: {
    alignItems: "center",
  },

  category: {
    fontSize: 16,

    fontWeight: "600",

    letterSpacing: 2,

    textTransform: "uppercase",
  },

  divider: {
    width: 70,
    height: 3,

    borderRadius: 2,

    marginTop: 10,
  },

  verseContainer: {
    flex: 1,

    justifyContent: "center",
    alignItems: "center",

    paddingVertical: 18,
  },

  verse: {
    flex: 1,

    textAlign: "center",

    fontFamily:
      "CormorantGaramond-Regular",

    fontWeight: "400",

    letterSpacing: 0.2,

    paddingHorizontal: 8,

    textAlignVertical: "center",
  },

  actions: {
    flexDirection: "row",

    justifyContent: "center",

    alignItems: "center",

    gap: 12,

    marginBottom: 18,
  },

  footer: {
    alignItems: "center",

    paddingBottom: 6,
  },

  reference: {
    fontSize: 22,

    fontWeight: "700",

  },

  translation: {
    marginTop: 6,

    fontSize: 16,


    letterSpacing: 1.5,
  },
});
