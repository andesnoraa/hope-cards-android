import { StyleSheet, View } from "react-native";

import CardBack from "./CardBack";
import StackCard from "./StackCard";

export default function DeckStack() {
  return (
    <View style={styles.container}>
      <View style={[styles.card, styles.card4]}>
        <StackCard />
      </View>

      <View style={[styles.card, styles.card3]}>
        <StackCard />
      </View>

      <View style={[styles.card, styles.card2]}>
        <StackCard />
      </View>

      <View style={[styles.card, styles.card1]}>
        <CardBack />
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

  card4: {
    transform: [
      { translateX: -9 },
      { translateY: -10 },
      { rotate: "-3deg" },
    ],
    zIndex: 1,
  },

  card3: {
    transform: [
      { translateX: -6 },
      { translateY: -6 },
      { rotate: "-2deg" },
    ],
    zIndex: 2,
  },

  card2: {
    transform: [
      { translateX: -3 },
      { translateY: -3 },
      { rotate: "-1deg" },
    ],
    zIndex: 3,
  },

  card1: {
    transform: [
      { translateX: 0 },
      { translateY: 0 },
      { rotate: "0.5deg" },
    ],
    zIndex: 4,
  },
});