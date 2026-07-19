import { StyleSheet, View } from "react-native";
import { useAppTheme } from "../../theme/appTheme";
import { CARD_HEIGHT, CARD_WIDTH } from "./cardDimensions";

export default function StackCard() {
  const { theme } = useAppTheme();

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor:
            theme.cardBack,
          borderColor:
            theme.cardBackAccent,
        },
      ]}
    />
  );
}

const styles = StyleSheet.create({
  card: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,

    borderRadius: 34,

    borderWidth: 1.5,
    boxShadow: "0 2px 6px rgba(0, 0, 0, 0.05)",
  },
});
