import { StyleSheet, View } from "react-native";
import type { SharedValue } from "react-native-reanimated";
import Animated, {
  useAnimatedStyle,
} from "react-native-reanimated";

import type { Verse } from "../../types/verse";

import CardBack from "./CardBack";
import CardView from "./CardView";
import StackCard from "./StackCard";
import {
  CARD_HEIGHT,
  CARD_STACK_HEIGHT,
  CARD_STACK_WIDTH,
  CARD_WIDTH,
} from "./cardDimensions";

type Props = {
  animatedStyle: any;
  rotateY: SharedValue<number>;
  showingVerse: boolean;
  verse: Verse;

  favorite: boolean;
  onToggleFavorite: () => void;
  onShare: () => void;
};

export default function DeckStack({
  animatedStyle,
  rotateY,
  showingVerse,
  verse,
  favorite,
  onToggleFavorite,
  onShare,
}: Props) {
  const backStyle = useAnimatedStyle(() => ({
    transform: [
      { perspective: 1200 },
      { rotateY: `${rotateY.value}deg` },
    ],
    backfaceVisibility: "hidden",
  }));

  const frontStyle = useAnimatedStyle(() => ({
    transform: [
      { perspective: 1200 },
      { rotateY: `${rotateY.value + 180}deg` },
    ],
    backfaceVisibility: "hidden",
  }));

  return (
    <View style={styles.container}>
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
        <Animated.View
          pointerEvents={
            showingVerse ? "none" : "auto"
          }
          style={[
            styles.face,
            backStyle,
          ]}
        >
          <CardBack />
        </Animated.View>

        <Animated.View
          pointerEvents={
            showingVerse ? "auto" : "none"
          }
          style={[
            styles.face,
            styles.frontFace,
            frontStyle,
          ]}
        >
          <CardView
            verse={verse}
            favorite={favorite}
            onToggleFavorite={onToggleFavorite}
            onShare={onShare}
          />
        </Animated.View>
      </Animated.View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: CARD_STACK_WIDTH,
    height: CARD_STACK_HEIGHT,
    justifyContent: "center",
    alignItems: "center",

    transform: [{ translateX: 4 }],
  },

  card: {
    position: "absolute",
  },

  card1: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,
    justifyContent: "center",
    alignItems: "center",
    zIndex: 4,
  },

  card2: {
    transform: [
      { translateX: -2 },
      { translateY: -2 },
      { rotate: "-0.6deg" },
    ],
    zIndex: 3,
  },

  card3: {
    transform: [
      { translateX: -4 },
      { translateY: -6 },
      { rotate: "-1deg" },
    ],
    zIndex: 2,
  },

  face: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,
    position: "absolute",
  },

  frontFace: {},
});
