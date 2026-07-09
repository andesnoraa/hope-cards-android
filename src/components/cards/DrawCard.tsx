import { useState } from "react";
import {
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import {
  useAnimatedStyle,
  useSharedValue,
  withSpring,
} from "react-native-reanimated";

import { verses } from "../../data/verses";
import DeckStack from "./DeckStack";

export default function DrawCard() {
  const [currentVerse, setCurrentVerse] = useState(verses[0]);
  const [showingVerse, setShowingVerse] = useState(false);

  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  const rotateY = useSharedValue(0);
  const scale = useSharedValue(1);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [
      { translateX: translateX.value },
      { translateY: translateY.value },
      { scale: scale.value },
    ],
  }));

  function drawCard() {
    // Return card to deck
    if (showingVerse) {
      rotateY.value = withSpring(0);

      translateX.value = withSpring(0);
      translateY.value = withSpring(0);
      scale.value = withSpring(1);

      setShowingVerse(false);

      return;
    }

    // Pick a new verse
    let nextVerse = currentVerse;

    while (nextVerse.id === currentVerse.id) {
      nextVerse =
        verses[Math.floor(Math.random() * verses.length)];
    }

    setCurrentVerse(nextVerse);

    // Lift animation
    translateX.value = withSpring(20);
    translateY.value = withSpring(-25);
    scale.value = withSpring(1.03);

    // Flip to front
    rotateY.value = withSpring(180);

    setShowingVerse(true);
  }

  return (
    <View style={styles.container}>
      <View style={styles.deckContainer}>
        <Pressable onPress={drawCard}>
          <DeckStack
            animatedStyle={animatedStyle}
            rotateY={rotateY}
            verse={currentVerse}
          />
        </Pressable>
      </View>

      <Pressable
        style={styles.button}
        onPress={drawCard}
      >
        <Text style={styles.buttonText}>
          {showingVerse
            ? "Return to Deck"
            : "Draw a Card"}
        </Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
  },

  deckContainer: {
    marginBottom: 80,
  },

  button: {
    width: 260,
    height: 56,

    backgroundColor: "#1A2747",

    borderRadius: 28,

    borderWidth: 2,
    borderColor: "#C5A24C",

    justifyContent: "center",
    alignItems: "center",

    shadowColor: "#000",
    shadowOpacity: 0.15,
    shadowRadius: 10,
    shadowOffset: {
      width: 0,
      height: 4,
    },

    elevation: 6,
  },

  buttonText: {
    color: "#FFFFFF",

    fontSize: 18,
    fontWeight: "600",

    letterSpacing: 0.5,
  },
});