import { StyleSheet, View } from "react-native";
import Animated from "react-native-reanimated";

import type { Verse } from "../../types/verse";

import CardBack from "./CardBack";
import CardFront from "./CardFront";
import StackCard from "./StackCard";

type Props = {
  animatedStyle: any;
  showFront: boolean;
  verse: Verse;
};

export default function DeckStack({
  animatedStyle,
  showFront,
  verse,
}: Props) {
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

      <Animated.View
        style={[
          styles.card,
          styles.card1,
          animatedStyle,
        ]}
      >
        {showFront ? (
          <CardFront verse={verse} />
        ) : (
          <CardBack />
        )}
      </Animated.View>
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
    transform: [{ rotate: "0.5deg" }],
    zIndex: 4,
  },
});