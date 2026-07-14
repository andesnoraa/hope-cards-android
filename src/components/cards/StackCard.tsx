import { StyleSheet, View } from "react-native";
import { useAppTheme } from "../../theme/appTheme";

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
    width: 345,
    height: 500,

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
