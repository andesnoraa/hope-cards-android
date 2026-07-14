import {
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";

import Ionicons from "@expo/vector-icons/Ionicons";

import type { Verse } from "../../types/verse";
import { useAppTheme } from "../../theme/appTheme";

type Props = {
  verse: Verse;
  onPress: () => void;
};

export default function FavoriteCard({
  verse,
  onPress,
}: Props) {
  const { theme } = useAppTheme();

  return (
    <Pressable
      style={styles.container}
      onPress={onPress}
    >
      <Text
        style={[
          styles.category,
          { color: theme.accent },
        ]}
      >
        {verse.category.toUpperCase()}
      </Text>

      <Text
        style={[
          styles.verse,
          { color: theme.cardText },
        ]}
        numberOfLines={2}
      >
        {verse.verse}
      </Text>

      <View style={styles.footer}>
        <Text
          style={[
            styles.reference,
            { color: theme.text },
          ]}
        >
          {verse.reference}
        </Text>

        <Ionicons
          name="chevron-forward"
          size={18}
          color={theme.textTertiary}
        />
      </View>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingVertical: 22,
  },

  category: {
    fontSize: 13,
    fontWeight: "700",
    letterSpacing: 2.5,
    marginBottom: 10,
  },

  verse: {
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
    fontSize: 18,
    fontWeight: "700",
  },

  divider: {
    height: StyleSheet.hairlineWidth,
  },
});
