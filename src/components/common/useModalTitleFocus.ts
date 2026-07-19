import {
  AccessibilityInfo,
  findNodeHandle,
  Text,
} from "react-native";
import {
  useEffect,
  useRef,
} from "react";

export default function useModalTitleFocus(
  visible: boolean
) {
  const titleRef = useRef<Text>(null);

  useEffect(() => {
    if (!visible) {
      return;
    }

    const timeout = setTimeout(() => {
      const node = findNodeHandle(
        titleRef.current
      );

      if (node !== null) {
        AccessibilityInfo.setAccessibilityFocus(
          node
        );
      }
    }, 220);

    return () => {
      clearTimeout(timeout);
    };
  }, [visible]);

  return titleRef;
}
