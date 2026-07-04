import { StyleSheet, Text, View } from "react-native";

export default function DeckCard() {
  return (
    <View style={styles.card}>
      <View style={styles.innerBorder} />

      <Text style={styles.logo}>🍃</Text>

      <Text style={styles.title}>Hope</Text>

      <Text style={styles.subtitle}>CARDS</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: 320,
    height: 470,

    backgroundColor: "#203A7A",

    borderRadius: 36,

    justifyContent: "center",
    alignItems: "center",

    borderWidth: 2,
    borderColor: "#C9A227",

    shadowColor: "#000",
    shadowOpacity: 0.28,
    shadowRadius: 28,
    shadowOffset: {
      width: 0,
      height: 18,
    },

    elevation: 18,
  },

  innerBorder: {
    position: "absolute",

    top: 14,
    left: 14,
    right: 14,
    bottom: 14,

    borderRadius: 28,

    borderWidth: 1.5,

    borderColor: "rgba(201,162,39,0.55)",
  },

  logo: {
    fontSize: 54,
    marginBottom: 18,
  },

  title: {
    color: "#FFFFFF",

    fontSize: 56,

    fontWeight: "800",

    letterSpacing: -1,
  },

  subtitle: {
    marginTop: 12,

    color: "#D4AF37",

    fontSize: 24,

    letterSpacing: 10,

    fontWeight: "600",
  },
});