import { StyleSheet, Text, View } from "react-native";

import { Colors } from "../../theme/colors";
import { Spacing } from "../../theme/spacing";
import { Typography } from "../../theme/typography";

export default function AppHeader() {
  return (
    <View style={styles.container}>
      <Text style={styles.cross}>✝</Text>

      <Text style={styles.title}>Hope Cards</Text>

      <Text style={styles.subtitle}>
        Daily encouragement{"\n"}from God's Word
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    marginTop: Spacing.lg,
    marginBottom: Spacing.xl,
  },

  cross: {
    fontSize: 34,
    color: Colors.primary,
    marginBottom: Spacing.sm,
  },

  title: {
    ...Typography.title,
    color: Colors.text,
  },

  subtitle: {
    marginTop: Spacing.sm,
    textAlign: "center",
    color: Colors.textSecondary,
    fontSize: 16,
    lineHeight: 24,
  },
});