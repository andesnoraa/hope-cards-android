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
          shadowColor: theme.shadow,
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
    shadowOpacity: 0.06,
    shadowRadius: 16,
    shadowOffset: {
      width: 0,
      height: 8,
    },

    elevation: 7,
  },
});
