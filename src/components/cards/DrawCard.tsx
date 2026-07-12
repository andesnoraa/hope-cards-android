import { useFocusEffect } from "expo-router";
import {
  useCallback,
  useEffect,
  useState,
} from "react";
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

import DeckStack from "./DeckStack";

import {
  getRandomVerse,
} from "../../services/verseService";

import {
  isFavorite,
  subscribeToFavorites,
  toggleFavorite,
} from "../../services/favorites";

import {
  lightImpact,
  selection,
  success,
} from "../../services/haptics";

import { getSettings } from "../../services/settings";

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
  const [currentVerse, setCurrentVerse] =
    useState(() => getRandomVerse());

  const [showingVerse, setShowingVerse] =
    useState(false);

  const [favorite, setFavorite] =
    useState(false);

  const [showDrawButton, setShowDrawButton] =
    useState(true);

  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  const rotateY = useSharedValue(0);
  const scale = useSharedValue(1);

  const buttonScale = useSharedValue(1);

  useEffect(() => {
    async function loadFavorite() {
      const saved = await isFavorite(
        currentVerse.id
      );

      setFavorite(saved);
    }

    loadFavorite();
  }, [currentVerse]);

  useEffect(() => {
    const unsubscribe =
      subscribeToFavorites(async () => {
        const saved =
          await isFavorite(
            currentVerse.id
          );

        setFavorite(saved);
      });

    return unsubscribe;
  }, [currentVerse]);

  useFocusEffect(
    useCallback(() => {
      async function loadSettings() {
        const settings =
          await getSettings();

        setShowDrawButton(
          settings.showDrawButton
        );
      }

      loadSettings();
    }, [])
  );

  const animatedStyle =
    useAnimatedStyle(() => ({
      transform: [
        { translateX: translateX.value },
        { translateY: translateY.value },
        { scale: scale.value },
      ],
    }));

  const buttonAnimatedStyle =
    useAnimatedStyle(() => ({
      transform: [
        { translateY: -16 },
        { scale: buttonScale.value },
      ],
    }));

  async function drawCard() {
    if (showingVerse) {
      rotateY.value = withSpring(
        0,
        CARD_SPRING
      );

      translateX.value = withSpring(
        0,
        CARD_SPRING
      );

      translateY.value = withSpring(
        0,
        CARD_SPRING
      );

      scale.value = withSpring(
        1,
        CARD_SPRING
      );

      setShowingVerse(false);
      return;
    }

    const nextVerse = getRandomVerse(
      currentVerse.id
    );

    setCurrentVerse(nextVerse);

    translateX.value = withSpring(
      0,
      CARD_SPRING
    );

    translateY.value = withSpring(
      -20,
      CARD_SPRING
    );

    scale.value = withSpring(
      1.02,
      CARD_SPRING
    );

    rotateY.value = withDelay(
      120,
      withSpring(
        180,
        CARD_SPRING
      )
    );

    await lightImpact();

    setShowingVerse(true);
  }

  async function handleToggleFavorite() {
    const saved =
      await toggleFavorite(
        currentVerse.id
      );

    setFavorite(saved);

    if (saved) {
      await success();
    } else {
      await selection();
    }
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
            showingVerse={showingVerse}
            verse={currentVerse}
            favorite={favorite}
            onToggleFavorite={
              handleToggleFavorite
            }
            onShare={handleShare}
          />
        </Pressable>
      </View>

      {showDrawButton && (
        <Pressable
          onPress={drawCard}
          onPressIn={() => {
            buttonScale.value =
              withSpring(
                0.96,
                BUTTON_SPRING
              );
          }}
          onPressOut={() => {
            buttonScale.value =
              withSpring(
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
            <Text
              style={styles.buttonText}
            >
              {showingVerse
                ? "Return to Deck"
                : "Draw a Card"}
            </Text>
          </Animated.View>
        </Pressable>
      )}
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
    marginBottom: 64,
  },

  button: {
    width: 270,
    height: 56,

    backgroundColor: "#1A2747",

    borderRadius: 12,

    borderWidth: 2,
    borderColor: "#C5A24C",

    justifyContent: "center",
    alignItems: "center",

    shadowColor: "#000",
    shadowOpacity: 0.12,
    shadowRadius: 8,
    shadowOffset: {
      width: 0,
      height: 3,
    },

    elevation: 5,
  },

  buttonText: {
    color: "#FFFFFF",

    fontSize: 18,
    fontWeight: "600",

    letterSpacing: 0.5,
  },
});