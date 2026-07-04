import { useState } from "react";
import { Pressable } from "react-native";
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withSpring,
} from "react-native-reanimated";

import { verses } from "../../data/verses";
import DeckStack from "./DeckStack";

export default function DrawCard() {
  const [currentVerse, setCurrentVerse] = useState(verses[0]);

  const translateY = useSharedValue(0);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateY: translateY.value }],
  }));

  function drawCard() {
    let nextVerse = currentVerse;

    while (nextVerse.id === currentVerse.id) {
      nextVerse = verses[Math.floor(Math.random() * verses.length)];
    }

    setCurrentVerse(nextVerse);

    translateY.value = withSpring(-30, {
      damping: 12,
      stiffness: 140,
    });

    console.log("Draw:", nextVerse.reference);
  }

  return (
    <Pressable onPress={drawCard}>
      <Animated.View style={animatedStyle}>
        <DeckStack />
      </Animated.View>
    </Pressable>
  );
}