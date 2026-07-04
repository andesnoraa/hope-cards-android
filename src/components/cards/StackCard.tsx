import { StyleSheet, View } from "react-native";

export default function StackCard() {
  return <View style={styles.card} />;
}

const styles = StyleSheet.create({
  card: {
    width: 345,
    height: 500,

    backgroundColor: "#1A2747",

    borderRadius: 34,

    borderWidth: 1.5,
    borderColor: "#C5A24C",

    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 24,
    shadowOffset: {
      width: 0,
      height: 12,
    },

    elevation: 10,
  },
});