import { useState } from "react";
import { Pressable } from "react-native";
import {
  useAnimatedStyle,
  useSharedValue,
  withSequence,
  withSpring,
} from "react-native-reanimated";

import { verses } from "../../data/verses";
import DeckStack from "./DeckStack";

export default function DrawCard() {
  const [currentVerse, setCurrentVerse] = useState(verses[0]);

  const translateX = useSharedValue(0);
  const translateY = useSharedValue(0);
  const rotateZ = useSharedValue(0);
  const rotateY = useSharedValue(0);
  const scale = useSharedValue(1);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [
      { translateX: translateX.value },
      { translateY: translateY.value },
      { rotateZ: `${rotateZ.value}deg` },
      { scale: scale.value },
    ],
  }));

  function drawCard() {
    let nextVerse = currentVerse;

    while (nextVerse.id === currentVerse.id) {
      nextVerse =
        verses[Math.floor(Math.random() * verses.length)];
    }

    setCurrentVerse(nextVerse);

    translateX.value = withSpring(20);
    translateY.value = withSpring(-25);
    rotateZ.value = withSpring(8);
    scale.value = withSpring(1.03);

    rotateY.value = withSequence(
      withSpring(180),
      withSpring(0)
    );
  }

  return (
    <Pressable onPress={drawCard}>
      <DeckStack
        animatedStyle={animatedStyle}
        rotateY={rotateY}
        verse={currentVerse}
      />
    </Pressable>
  );
}