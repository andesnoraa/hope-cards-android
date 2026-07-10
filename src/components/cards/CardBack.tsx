import { StyleSheet, Text, View } from "react-native";

export default function CardBack() {
  return (
    <View style={styles.card}>
      <View style={styles.innerBorder} />

      <Text style={styles.cross}>✝</Text>

      <Text style={styles.title}>HOPE</Text>

      <Text style={styles.subtitle}>CARDS</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: 345,
    height: 500,

    backgroundColor: "#1A2747",

    borderRadius: 34,

    justifyContent: "center",
    alignItems: "center",

    borderWidth: 1.5,
    borderColor: "#C5A24C",

    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 20,
    shadowOffset: {
      width: 0,
      height: 10,
    },

    elevation: 8,
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

  cross: {
    fontSize: 62,

    color: "#D4AF37",

    marginBottom: 16,

    fontFamily: "CormorantGaramond-Bold",
  },

  title: {
    color: "#F8F6F2",

    fontFamily: "CormorantGaramond-Bold",

    fontSize: 68,

    letterSpacing: 2,

    textAlign: "center",
  },

  subtitle: {
    marginTop: 4,

    color: "#C5A24C",

    fontSize: 20,

    letterSpacing: 14,

    fontWeight: "600",

    textAlign: "center",
  },
});