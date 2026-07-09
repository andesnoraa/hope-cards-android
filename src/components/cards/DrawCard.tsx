import { useEffect, useState } from "react";
import {
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withDelay,
  withSpring,
} from "react-native-reanimated";

import { verses } from "../../data/verses";
import DeckStack from "./DeckStack";

import {
  isFavorite,
  toggleFavorite,
} from "../../services/favorites";

import { shareVerse } from "../../services/share";

const CARD_SPRING = {
  damping: 28,
  stiffness: 170,
  mass: 1,
};

const BUTTON_SPRING = {
  damping: 14,
  stiffness: 260,
};

export default function DrawCard() {
  const [currentVerse, setCurrentVerse] = useState(verses[0]);
  const [showingVerse, setShowingVerse] = useState(false);
  const [favorite, setFavorite] = useState(false);

  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  const rotateY = useSharedValue(0);
  const scale = useSharedValue(1);

  const buttonScale = useSharedValue(1);

  useEffect(() => {
    async function loadFavorite() {
      const saved = await isFavorite(currentVerse.id);
      setFavorite(saved);
    }

    loadFavorite();
  }, [currentVerse]);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [
      { translateX: translateX.value },
      { translateY: translateY.value },
      { scale: scale.value },
    ],
  }));

  const buttonAnimatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: buttonScale.value }],
  }));

  function drawCard() {
    if (showingVerse) {
      rotateY.value = withSpring(0, CARD_SPRING);

      translateX.value = withSpring(0, CARD_SPRING);
      translateY.value = withSpring(0, CARD_SPRING);
      scale.value = withSpring(1, CARD_SPRING);

      setShowingVerse(false);
      return;
    }

    let nextVerse = currentVerse;

    while (nextVerse.id === currentVerse.id) {
      nextVerse =
        verses[Math.floor(Math.random() * verses.length)];
    }

    setCurrentVerse(nextVerse);

    translateX.value = withSpring(0, CARD_SPRING);
    translateY.value = withSpring(-20, CARD_SPRING);
    scale.value = withSpring(1.02, CARD_SPRING);

    rotateY.value = withDelay(
      120,
      withSpring(180, CARD_SPRING)
    );

    setShowingVerse(true);
  }

  async function handleToggleFavorite() {
    const saved = await toggleFavorite(currentVerse.id);
    setFavorite(saved);
  }

  async function handleShare() {
    await shareVerse(currentVerse);
  }

  return (
    <View style={styles.container}>
      <View style={styles.deckContainer}>
        <Pressable onPress={drawCard}>
          <DeckStack
            animatedStyle={animatedStyle}
            rotateY={rotateY}
            verse={currentVerse}
            favorite={favorite}
            onToggleFavorite={handleToggleFavorite}
            onShare={handleShare}
          />
        </Pressable>
      </View>

      <Pressable
        onPress={drawCard}
        onPressIn={() => {
          buttonScale.value = withSpring(
            0.96,
            BUTTON_SPRING
          );
        }}
        onPressOut={() => {
          buttonScale.value = withSpring(
            1,
            BUTTON_SPRING
          );
        }}
      >
        <Animated.View
          style={[
            styles.button,
            buttonAnimatedStyle,
          ]}
        >
          <Text style={styles.buttonText}>
            {showingVerse
              ? "Return to Deck"
              : "Draw a Card"}
          </Text>
        </Animated.View>
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