import { useState } from "react";
import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import AppHeader from "../components/layout/AppHeader";

import PrimaryButton from "../components/buttons/PrimaryButton";
import AnimatedHopeCard from "../components/cards/AnimatedHopeCard";
import { verses } from "../data/verses";

export default function HomeScreen() {
  const [currentVerse, setCurrentVerse] = useState(verses[0]);

  function drawHopeCard() {
    let randomVerse = currentVerse;

    while (randomVerse.id === currentVerse.id) {
      randomVerse = verses[Math.floor(Math.random() * verses.length)];
    }

    setCurrentVerse(randomVerse);
  }

  return (
    <SafeAreaView style={styles.container}>
      <AppHeader />

      <PrimaryButton
        title="Draw Another Card"
        onPress={drawHopeCard}
      />

      <AnimatedHopeCard verse={currentVerse} />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F4F6F8",
    padding: 20,
  },

  title: {
    fontSize: 34,
    fontWeight: "700",
    textAlign: "center",
    marginVertical: 20,
    color: "#1F2937",
  },
});