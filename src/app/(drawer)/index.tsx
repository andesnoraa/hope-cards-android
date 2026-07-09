import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import DrawCard from "../../components/cards/DrawCard";

export default function HomeScreen() {
  return (
    <SafeAreaView style={styles.container}>
      <DrawCard />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F7F5F1",
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 24,
  },
});