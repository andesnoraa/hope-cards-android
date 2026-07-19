import { StyleSheet, Text, View } from "react-native";
import { useAppTheme } from "../../theme/appTheme";
import { CARD_HEIGHT, CARD_WIDTH } from "./cardDimensions";

export default function CardBack() {
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
    >
      <View
        style={[
          styles.innerBorder,
          {
            borderColor:
              theme.cardBackAccent,
          },
        ]}
      />

      <View style={styles.logoGroup}>
        <Text
          style={[
            styles.cross,
            {
              color:
                theme.cardBackAccent,
            },
          ]}
        >
          ✝
        </Text>

        <Text
          style={[
            styles.title,
            {
              color:
                theme.cardBackText,
            },
          ]}
        >
          HOPE
        </Text>

        <Text
          style={[
            styles.subtitle,
            {
              color:
                theme.cardBackAccent,
            },
          ]}
        >
          CARDS
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,

    borderRadius: 34,

    justifyContent: "center",
    alignItems: "center",

    borderWidth: 1.5,
    boxShadow: "0 6px 14px rgba(0, 0, 0, 0.10)",
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

  logoGroup: {
    alignItems: "center",
    transform: [{ translateY: -18 }],
  },

  cross: {
    fontSize: 52,

    marginBottom: 12,

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
