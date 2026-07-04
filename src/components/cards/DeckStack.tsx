import { Pressable, StyleSheet, View } from "react-native";

import DeckCard from "./DeckCard";

type Props = {
  onPress: () => void;
};

export default function DeckStack({ onPress }: Props) {
  return (
    <Pressable onPress={onPress} style={styles.container}>
      <View style={[styles.layer, styles.layer4]}>
        <DeckCard />
      </View>

      <View style={[styles.layer, styles.layer3]}>
        <DeckCard />
      </View>

      <View style={[styles.layer, styles.layer2]}>
        <DeckCard />
      </View>

      <View style={styles.layer}>
        <DeckCard />
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    width: 360,
    height: 520,

    justifyContent: "center",
    alignItems: "center",
  },

  layer: {
    position: "absolute",
  },

  layer2: {
    transform: [
      { translateX: -10 },
      { translateY: -10 },
      { rotate: "-2deg" },
    ],
  },

  layer3: {
    transform: [
      { translateX: -20 },
      { translateY: -18 },
      { rotate: "-4deg" },
    ],
  },

  layer4: {
    transform: [
      { translateX: -30 },
      { translateY: -26 },
      { rotate: "-6deg" },
    ],
  },
});