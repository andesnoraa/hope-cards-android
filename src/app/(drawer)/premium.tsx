import { StyleSheet, Text, View } from "react-native";

type Props = {
  title: string;
};

function PlaceholderScreen({ title }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>

      <Text style={styles.subtitle}>
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
    backgroundColor: "#F8F6F2",
  },

  title: {
    fontSize: 28,
    fontWeight: "700",
    color: "#1A2747",
  },

  subtitle: {
    marginTop: 12,
    fontSize: 18,
    color: "#666",
  },
});

export default function Screen() {
  return <PlaceholderScreen title="Premium" />;
}