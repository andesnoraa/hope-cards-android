import { StyleSheet, View } from "react-native";
import type { SharedValue } from "react-native-reanimated";
import Animated, {
  useAnimatedStyle,
} from "react-native-reanimated";

import type { Verse } from "../../types/verse";

import CardBack from "./CardBack";
import CardView from "./CardView";
import StackCard from "./StackCard";

type Props = {
  animatedStyle: any;
  rotateY: SharedValue<number>;
  verse: Verse;

  favorite: boolean;
  onToggleFavorite: () => void;
  onShare: () => void;
};

export default function DeckStack({
  animatedStyle,
  rotateY,
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
        <Animated.View
          style={[
            styles.face,
            backStyle,
          ]}
        >
          <CardBack />
        </Animated.View>

        <Animated.View
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
    width: 355,
    height: 510,
    justifyContent: "center",
    alignItems: "center",

    transform: [{ translateX: 4 }],
  },

  card: {
    position: "absolute",
  },

  card1: {
    width: 345,
    height: 500,
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
      { translateY: -5 },
      { rotate: "-1.2deg" },
    ],
    zIndex: 2,
  },

  card4: {
    transform: [
      { translateX: -5 },
      { translateY: -8 },
      { rotate: "-1.8deg" },
    ],
    zIndex: 1,
  },
  face: {
    width: 345,
    height: 500,
    position: "absolute",
  },

  frontFace: {},
});