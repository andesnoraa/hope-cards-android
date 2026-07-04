import { StyleSheet, Text, View } from "react-native";
import type { Verse } from "../types/verse";

type VerseCardProps = {
  verse: Verse;
};

export default function VerseCard({ verse }: VerseCardProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.category}>
        {verse.category}
      </Text>

      <Text style={styles.verse}>
        "{verse.fullVerse}"
      </Text>

      <Text style={styles.reference}>
        {verse.reference}
      </Text>

      <Text style={styles.translation}>
        {verse.translation}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: "#FFFFFF",
    borderRadius: 20,
    padding: 24,
    marginVertical: 16,

    shadowColor: "#000",
    shadowOpacity: 0.12,
    shadowRadius: 12,
    shadowOffset: {
      width: 0,
      height: 6,
    },

    elevation: 6,
  },

  category: {
    fontSize: 14,
    fontWeight: "700",
    color: "#3B82F6",
    marginBottom: 12,
    textTransform: "uppercase",
  },

  verse: {
    fontSize: 20,
    lineHeight: 32,
    color: "#111827",
  },

  reference: {
    marginTop: 24,
    fontSize: 18,
    fontWeight: "700",
    color: "#1F2937",
  },

  translation: {
    marginTop: 4,
    color: "#6B7280",
  },
});