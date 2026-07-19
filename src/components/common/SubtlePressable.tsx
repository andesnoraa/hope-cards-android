import type {
  GestureResponderEvent,
  PressableProps,
  StyleProp,
  ViewStyle,
} from "react-native";
import { Pressable } from "react-native";
import Animated, {
  ReduceMotion,
  useAnimatedStyle,
  useSharedValue,
  withTiming,
} from "react-native-reanimated";

type Props = Omit<PressableProps, "style"> & {
  style?: StyleProp<ViewStyle>;
  scaleTo?: number;
};

export default function SubtlePressable({
  style,
  scaleTo = 0.97,
  onPressIn,
  onPressOut,
  ...props
}: Props) {
  const scale = useSharedValue(1);

  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ scale: scale.value }],
  }));

  function handlePressIn(
    event: GestureResponderEvent
  ) {
    scale.value = withTiming(scaleTo, {
      duration: 80,
      reduceMotion: ReduceMotion.System,
    });
    onPressIn?.(event);
  }

  function handlePressOut(
    event: GestureResponderEvent
  ) {
    scale.value = withTiming(1, {
      duration: 120,
      reduceMotion: ReduceMotion.System,
    });
    onPressOut?.(event);
  }

  return (
    <Animated.View style={animatedStyle}>
      <Pressable
        {...props}
        style={style}
        onPressIn={handlePressIn}
        onPressOut={handlePressOut}
      />
    </Animated.View>
  );
}
