import { StyleSheet, Text, View } from "react-native";
import { useAppTheme } from "../../theme/appTheme";

export default function CardBack() {
  const { theme } = useAppTheme();

  return (
    <View
      style={[
        styles.card,
        {
          backgroundColor:
            theme.cardBack,
          borderColor: theme.accent,
          shadowColor: theme.shadow,
        },
      ]}
    >
      <View
        style={[
          styles.innerBorder,
          {
            borderColor:
              theme.accentLine,
          },
        ]}
      />

      <Text
        style={[
          styles.cross,
          { color: theme.accent },
        ]}
      >
        ✝
      </Text>

      <Text
        style={[
          styles.title,
          {
            color:
              theme.background,
          },
        ]}
      >
        HOPE
      </Text>

      <Text
        style={[
          styles.subtitle,
          { color: theme.accent },
        ]}
      >
        CARDS
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: 345,
    height: 500,

    borderRadius: 34,

    justifyContent: "center",
    alignItems: "center",

    borderWidth: 1.5,
    shadowOpacity: 0.08,
    shadowRadius: 20,
    shadowOffset: {
      width: 0,
      height: 10,
    },

    elevation: 8,
  },

  innerBorder: {
    position: "absolute",

    top: 12,
    left: 12,
    right: 12,
    bottom: 12,

    borderRadius: 26,

    borderWidth: 1,

  },

  cross: {
    fontSize: 62,

    marginBottom: 16,

    fontFamily: "CormorantGaramond-Bold",
  },

  title: {
    fontFamily: "CormorantGaramond-Bold",

    fontSize: 68,

    letterSpacing: 2,

    textAlign: "center",
  },

  subtitle: {
    marginTop: 4,

    fontSize: 20,

    letterSpacing: 14,

    fontWeight: "600",

    textAlign: "center",
  },
});
