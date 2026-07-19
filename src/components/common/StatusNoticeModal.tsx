import Ionicons from "@expo/vector-icons/Ionicons";
import {
  Modal,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import Animated, {
  FadeInUp,
  ReduceMotion,
  useReducedMotion,
} from "react-native-reanimated";

import { useAppTheme } from "../../theme/appTheme";
import SubtlePressable from "./SubtlePressable";
import useModalTitleFocus from "./useModalTitleFocus";

type Props = {
  visible: boolean;
  title: string;
  message: string;
  icon: keyof typeof Ionicons.glyphMap;
  onDismiss: () => void;
};

export default function StatusNoticeModal({
  visible,
  title,
  message,
  icon,
  onDismiss,
}: Props) {
  const { theme } = useAppTheme();
  const reduceMotion = useReducedMotion();
  const titleRef = useModalTitleFocus(visible);

  return (
    <Modal
      transparent
      visible={visible}
      animationType={
        reduceMotion ? "none" : "fade"
      }
      statusBarTranslucent
      onRequestClose={onDismiss}
    >
      <View
        accessibilityViewIsModal
        style={styles.backdrop}
      >
        <Animated.View
          entering={FadeInUp.duration(180).reduceMotion(
            ReduceMotion.System
          )}
          style={[
            styles.card,
            {
              backgroundColor: theme.surface,
              borderColor: theme.accentLine,
            },
          ]}
        >
          <ScrollView
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator={false}
          >
            <View
              style={[
                styles.iconHalo,
                {
                  backgroundColor: theme.accentSoft,
                },
              ]}
            >
              <View
                style={[
                  styles.iconCircle,
                  {
                    backgroundColor: theme.accent,
                  },
                ]}
              >
                <Ionicons
                  name={icon}
                  size={28}
                  color={theme.white}
                />
              </View>
            </View>

            <Text
              ref={titleRef}
              accessible
              accessibilityRole="header"
              style={[
                styles.title,
                { color: theme.text },
              ]}
            >
              {title}
            </Text>

            <Text
              selectable
              style={[
                styles.message,
                { color: theme.textSecondary },
              ]}
            >
              {message}
            </Text>

            <SubtlePressable
              accessibilityRole="button"
              accessibilityLabel="Close confirmation"
              onPress={onDismiss}
              style={[
                styles.button,
                {
                  backgroundColor:
                    theme.buttonBackground,
                  borderColor: theme.buttonBorder,
                },
              ]}
            >
              <Text
                style={[
                  styles.buttonText,
                  { color: theme.buttonText },
                ]}
              >
                Done
              </Text>
            </SubtlePressable>
          </ScrollView>
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    padding: 26,
    backgroundColor: "rgba(18, 18, 18, 0.48)",
  },
  card: {
    width: "100%",
    maxWidth: 390,
    maxHeight: "86%",
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: 24,
    borderCurve: "continuous",
    boxShadow: "0 12px 28px rgba(0, 0, 0, 0.16)",
  },
  content: {
    alignItems: "center",
    paddingHorizontal: 24,
    paddingTop: 28,
    paddingBottom: 24,
  },
  iconHalo: {
    width: 78,
    height: 78,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 39,
  },
  iconCircle: {
    width: 56,
    height: 56,
    alignItems: "center",
    justifyContent: "center",
    borderRadius: 28,
  },
  title: {
    marginTop: 20,
    fontSize: 24,
    lineHeight: 30,
    fontWeight: "700",
    textAlign: "center",
  },
  message: {
    marginTop: 10,
    fontSize: 16,
    lineHeight: 24,
    textAlign: "center",
  },
  button: {
    width: "100%",
    minHeight: 52,
    alignItems: "center",
    justifyContent: "center",
    marginTop: 24,
    borderWidth: 1.5,
    borderRadius: 18,
    borderCurve: "continuous",
  },
  buttonText: {
    fontSize: 16,
    fontWeight: "700",
    letterSpacing: 0.3,
  },
});
