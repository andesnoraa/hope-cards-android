import { StyleSheet, Text, View } from "react-native";

import { Colors } from "../../theme/colors";
import { Spacing } from "../../theme/spacing";
import { Typography } from "../../theme/typography";
import type { Verse } from "../../types/verse";

type Props = {
  verse: Verse;
};

export default function CardFront({ verse }: Props) {
  return (
    <View style={styles.card}>
      <Text style={styles.category}>{verse.category}</Text>

      <View style={styles.divider} />

      <Text style={styles.verse}>{verse.fullVerse}</Text>

      <View style={styles.footer}>
        <Text style={styles.reference}>{verse.reference}</Text>

        <Text style={styles.translation}>{verse.translation}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.surface,

    borderRadius: 30,

    paddingHorizontal: Spacing.xl,
    paddingVertical: 36,

    marginVertical: Spacing.lg,

    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 18,
    shadowOffset: {
      width: 0,
      height: 8,
    },

    elevation: 5,
  },

  category: {
    ...Typography.body,

    textAlign: "center",

    color: "#B8860B",

    fontWeight: "600",

    letterSpacing: 1,
  },

  divider: {
    alignSelf: "center",

    width: 70,

    height: 2,

    borderRadius: 2,

    backgroundColor: "#D4AF37",

    marginTop: Spacing.sm,
    marginBottom: Spacing.xl,
  },

  verse: {
    ...Typography.body,

    textAlign: "center",

    fontSize: 26,

    lineHeight: 40,

    color: Colors.text,
  },

  footer: {
    marginTop: 40,

    alignItems: "center",
  },

  reference: {
    ...Typography.reference,

    fontSize: 24,

    color: Colors.text,
  },

  translation: {
    marginTop: Spacing.sm,

    color: Colors.textSecondary,

    letterSpacing: 1,
  },
});