import { useEffect } from "react";
import Animated, {
    useAnimatedStyle,
    useSharedValue,
    withSequence,
    withTiming,
} from "react-native-reanimated";

import type { Verse } from "../../types/verse";
import HopeCard from "./HopeCard";

type Props = {
  verse: Verse;
};

export default function AnimatedHopeCard({ verse }: Props) {
  const scale = useSharedValue(1);

  useEffect(() => {
    scale.value = withSequence(
      withTiming(0.96, { duration: 120 }),
      withTiming(1, { duration: 220 })
    );
  }, [verse]);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: scale.value }],
  }));

  return (
    <Animated.View style={animatedStyle}>
      <HopeCard verse={verse} />
    </Animated.View>
  );
}