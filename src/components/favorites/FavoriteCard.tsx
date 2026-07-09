import {
    Pressable,
    StyleSheet,
    Text,
    View,
} from "react-native";

import Ionicons from "@expo/vector-icons/Ionicons";

import type { Verse } from "../../types/verse";

type Props = {
  verse: Verse;
  onPress: () => void;
};

export default function FavoriteCard({
  verse,
  onPress,
}: Props) {
  return (
    <Pressable
      style={styles.container}
      onPress={onPress}
    >
      <Text style={styles.category}>
        {verse.category.toUpperCase()}
      </Text>

      <Text
        style={styles.verse}
        numberOfLines={2}
      >
        {verse.fullVerse}
      </Text>

      <View style={styles.footer}>
        <Text style={styles.reference}>
          {verse.reference}
        </Text>

        <Ionicons
          name="chevron-forward"
          size={18}
          color="#9AA0AA"
        />
      </View>

      <View style={styles.divider} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingVertical: 22,
  },

  category: {
    color: "#C5A24C",

    fontSize: 13,

    fontWeight: "700",

    letterSpacing: 2.5,

    marginBottom: 10,
  },

  verse: {
    color: "#273043",

    fontSize: 19,

    lineHeight: 30,

    marginBottom: 18,
  },

  footer: {
    flexDirection: "row",

    justifyContent: "space-between",

    alignItems: "center",

    marginBottom: 18,
  },

  reference: {
    color: "#1A2747",

    fontSize: 18,

    fontWeight: "700",
  },

  divider: {
    height: StyleSheet.hairlineWidth,

    backgroundColor: "#DDD8CC",
  },
});