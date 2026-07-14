import { StyleSheet, Text, View } from "react-native";
import { useAppTheme } from "../../theme/appTheme";

type Props = {
  title: string;
};

function PlaceholderScreen({ title }: Props) {
  const { theme } = useAppTheme();

  return (
    <View
      style={[
        styles.container,
        {
          backgroundColor:
            theme.background,
        },
      ]}
    >
      <Text
        style={[
          styles.title,
          { color: theme.text },
        ]}
      >
        {title}
      </Text>

      <Text
        style={[
          styles.subtitle,
          {
            color: theme.textSecondary,
          },
        ]}
      >
        Coming Soon
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },

  title: {
    fontSize: 28,
    fontWeight: "700",
  },

  subtitle: {
    marginTop: 12,
    fontSize: 18,
  },
});

export default function Screen() {
  return <PlaceholderScreen title="Premium" />;
}
