import { useState } from "react";
import { Pressable } from "react-native";
import {
  useAnimatedStyle,
  useSharedValue,
  withDelay,
  withSequence,
  withSpring,
} from "react-native-reanimated";

import { verses } from "../../data/verses";
import DeckStack from "./DeckStack";

export default function DrawCard() {
  const [currentVerse, setCurrentVerse] = useState(verses[0]);
  const [isAnimating, setIsAnimating] = useState(false);
  const [showFront, setShowFront] = useState(false);

  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  const scale = useSharedValue(1);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [
      { translateX: translateX.value },
      { translateY: translateY.value },
      { scale: scale.value },
    ],
  }));

  function drawCard() {
    if (isAnimating) return;

    setIsAnimating(true);
    setShowFront(false);

    let nextVerse = currentVerse;

    while (nextVerse.id === currentVerse.id) {
      nextVerse = verses[Math.floor(Math.random() * verses.length)];
    }

    console.log("Draw:", nextVerse.reference);

    setCurrentVerse(nextVerse);

    translateX.value = withSequence(
      withSpring(14, {
        damping: 14,
        stiffness: 140,
      }),
      withDelay(
        250,
        withSpring(0, {
          damping: 14,
          stiffness: 140,
        })
      )
    );

    translateY.value = withSequence(
      withSpring(-70, {
        damping: 14,
        stiffness: 140,
      }),
      withDelay(
        250,
        withSpring(0, {
          damping: 14,
          stiffness: 140,
        })
      )
    );

    scale.value = withSequence(
      withSpring(1.04, {
        damping: 14,
        stiffness: 140,
      }),
      withDelay(
        250,
        withSpring(1, {
          damping: 14,
          stiffness: 140,
        })
      )
    );

    setTimeout(() => {
      setShowFront(true);
      setIsAnimating(false);
    }, 450);
  }

  return (
    <Pressable onPress={drawCard}>
      <DeckStack
        animatedStyle={animatedStyle}
        showFront={showFront}
        verse={currentVerse}
      />
    </Pressable>
  );
}