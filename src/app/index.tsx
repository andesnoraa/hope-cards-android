import { StyleSheet, Text, View } from "react-native";

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Hope Cards</Text>

      <Text style={styles.subtitle}>
        Coming Soon...
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#FFFFFF",
    justifyContent: "center",
    alignItems: "center",
    padding: 24,
  },
  title: {
    fontSize: 34,
    fontWeight: "700",
    color: "#1F2937",
    marginBottom: 12,
  },
  subtitle: {
    fontSize: 18,
    color: "#6B7280",
  },
});