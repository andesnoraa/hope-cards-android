import { Pressable, StyleSheet, Text, View } from "react-native";

import DeckStack from "./DeckStack";

type Props = {
  onPress: () => void;
};

export default function Deck({ onPress }: Props) {
  return (
    <View style={styles.container}>
      <Pressable onPress={onPress}>
        <DeckStack />
      </Pressable>

      <Text style={styles.tapHint}>
        Tap the deck
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
    width: "100%",
  },

  tapHint: {
    marginTop: 28,
    fontSize: 18,
    color: "#667085",
  },
});