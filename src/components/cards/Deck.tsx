import { Pressable, StyleSheet, Text, View } from "react-native";

import DeckStack from "./DeckStack";

type Props = {
  onPress: () => void;
};

export default function Deck({ onPress }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Hope Cards</Text>

      <Text style={styles.subtitle}>
        Tap the deck to draw today's hope.
      </Text>

      <DeckStack onPress={onPress} />

      <Pressable onPress={onPress}>
        <Text style={styles.tap}>
          Tap the deck to draw
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,

    justifyContent: "center",
    alignItems: "center",

    width: "100%",
  },

  title: {
    fontSize: 58,

    fontWeight: "800",

    color: "#1F3566",

    marginBottom: 10,
  },

  subtitle: {
    fontSize: 20,

    color: "#6B7280",

    marginBottom: 36,
  },

  tap: {
    marginTop: 18,

    fontSize: 18,

    color: "#6B7280",
  },
});