import { StyleSheet, View } from "react-native";

import DeckCard from "./DeckCard";

export default function DeckStack() {
  return (
    <View style={styles.container}>
      <View style={[styles.card, styles.card4]}>
        <DeckCard />
      </View>

      <View style={[styles.card, styles.card3]}>
        <DeckCard />
      </View>

      <View style={[styles.card, styles.card2]}>
        <DeckCard />
      </View>

      <View style={[styles.card, styles.card1]}>
        <DeckCard />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: 355,
    height: 510,

    alignItems: "center",
    justifyContent: "center",
  },

  card: {
    position: "absolute",
  },

  // Bottom card
  card4: {
    transform: [
      { translateX: -9 },
      { translateY: -10 },
      { rotate: "-3deg" },
    ],
    zIndex: 1,
  },

  // Third card
  card3: {
    transform: [
      { translateX: -6 },
      { translateY: -6 },
      { rotate: "-2deg" },
    ],
    zIndex: 2,
  },

  // Second card
  card2: {
    transform: [
      { translateX: -3 },
      { translateY: -3 },
      { rotate: "-1deg" },
    ],
    zIndex: 3,
  },

  // Top card
  card1: {
    transform: [
      { translateX: 0 },
      { translateY: 0 },
      { rotate: "0.5deg" },
    ],
    zIndex: 4,
  },
});