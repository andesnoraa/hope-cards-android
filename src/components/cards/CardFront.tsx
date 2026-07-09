import { StyleSheet, Text, View } from "react-native";

import type { Verse } from "../../types/verse";

import ActionButton from "../common/ActionButton";

type Props = {
  verse: Verse;
  favorite: boolean;
  onToggleFavorite: () => void;
  onShare: () => void;
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
  favorite,
  onToggleFavorite,
  onShare,
}: Props) {
  const verseStyle = getVerseStyle(verse.fullVerse);

  return (
    <View style={styles.card}>
      <View style={styles.innerBorder} />

      <View style={styles.content}>
        <View style={styles.header}>
          <Text style={styles.category}>
            {verse.category}
          </Text>

          <View style={styles.divider} />
        </View>

        <View style={styles.verseContainer}>
          <Text style={[styles.verse, verseStyle]}>
            {verse.fullVerse}
          </Text>
        </View>

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
            onPress={onToggleFavorite}
          />

          <ActionButton
            icon="share-outline"
            label="Share"
            color="#C5A24C"
            onPress={onShare}
          />
        </View>

        <View style={styles.footer}>
          <Text style={styles.reference}>
            {verse.reference}
          </Text>

          <Text style={styles.translation}>
            {verse.translation}
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: 345,
    height: 500,

    backgroundColor: "#F8F6F2",

    borderRadius: 34,

    borderWidth: 1.5,
    borderColor: "#C5A24C",

    shadowColor: "#000",
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

    borderColor: "rgba(197,162,76,0.45)",
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
    color: "#C5A24C",

    fontSize: 16,

    fontWeight: "600",

    letterSpacing: 2,

    textTransform: "uppercase",
  },

  divider: {
    width: 70,
    height: 3,

    borderRadius: 2,

    backgroundColor: "#D4AF37",

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

    color: "#273043",

    textAlign: "center",

    fontFamily: "CormorantGaramond-Regular",

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

    color: "#273043",
  },

  translation: {
    marginTop: 6,

    fontSize: 16,

    color: "#7A8292",

    letterSpacing: 1.5,
  },
});