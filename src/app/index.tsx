import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import Deck from "../components/cards/Deck";

export default function HomeScreen() {
  function drawCard() {
    console.log("Draw card");
  }

  return (
    <SafeAreaView style={styles.container}>
      <Deck onPress={drawCard} />
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