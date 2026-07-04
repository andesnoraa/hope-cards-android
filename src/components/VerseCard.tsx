import { Pressable, StyleSheet, Text, View } from "react-native";
import type { Verse } from "../types/verse";

type VerseCardProps = {
  verse: Verse;
  onFavorite?: () => void;
  onShare?: () => void;
};

export default function VerseCard({
  verse,
  onFavorite,
  onShare,
}: VerseCardProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.category}>{verse.category}</Text>

      <View style={styles.divider} />

      <Text style={styles.verse}>{verse.fullVerse}</Text>

      <View style={styles.footer}>
        <Text style={styles.reference}>{verse.reference}</Text>

        <Text style={styles.translation}>{verse.translation}</Text>
      </View>

      <View style={styles.actions}>
        <Pressable
          style={styles.actionButton}
          onPress={onFavorite}
        >
          <Text style={styles.actionText}>❤️ Favorite</Text>
        </Pressable>

        <Pressable
          style={styles.actionButton}
          onPress={onShare}
        >
          <Text style={styles.actionText}>📤 Share</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: "#FFFFFF",

    borderRadius: 28,

    padding: 28,

    marginTop: 20,

    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 20,
    shadowOffset: {
      width: 0,
      height: 8,
    },

    elevation: 5,
  },

  category: {
    textAlign: "center",

    fontSize: 18,

    fontWeight: "600",

    color: "#C68A00",
  },

  divider: {
    width: 60,
    height: 2,

    alignSelf: "center",

    backgroundColor: "#D4AF37",

    marginVertical: 16,

    borderRadius: 5,
  },

  verse: {
    fontSize: 24,

    lineHeight: 38,

    textAlign: "center",

    color: "#1F2937",
  },

  footer: {
    marginTop: 32,

    alignItems: "flex-end",
  },

  reference: {
    fontSize: 20,

    fontWeight: "700",

    color: "#1F2937",
  },

  translation: {
    marginTop: 6,

    color: "#6B7280",

    fontSize: 15,
  },

  actions: {
    flexDirection: "row",

    justifyContent: "space-between",

    marginTop: 28,

    paddingTop: 20,

    borderTopWidth: 1,

    borderTopColor: "#ECECEC",
  },

  actionButton: {
    paddingVertical: 8,
  },

  actionText: {
    fontSize: 16,

    color: "#1E3A8A",

    fontWeight: "600",
  },
});