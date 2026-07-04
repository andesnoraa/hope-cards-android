import { StyleSheet, Text, View } from "react-native";

import { Colors } from "../../theme/colors";
import { Spacing } from "../../theme/spacing";

export default function CardBack() {
  return (
    <View style={styles.card}>
      <Text style={styles.cross}>✝</Text>

      <Text style={styles.title}>
        Hope Cards
      </Text>

      <Text style={styles.subtitle}>
        Daily encouragement{"\n"}from God's Word
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: Colors.primary,
    borderRadius: 24,
    padding: 40,
    alignItems: "center",
    justifyContent: "center",
    minHeight: 380,

    shadowColor: "#000",
    shadowOpacity: 0.15,
    shadowRadius: 14,
    shadowOffset: {
      width: 0,
      height: 8,
    },

    elevation: 8,
  },

  cross: {
    fontSize: 52,
    color: "#FFFFFF",
    marginBottom: Spacing.lg,
  },

  title: {
    color: "#FFFFFF",
    fontSize: 30,
    fontWeight: "700",
  },

  subtitle: {
    marginTop: Spacing.md,
    color: "#FFFFFF",
    textAlign: "center",
    fontSize: 18,
    lineHeight: 28,
  },
});