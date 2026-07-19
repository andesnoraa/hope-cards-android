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
import SubtlePressable from "../common/SubtlePressable";
import useModalTitleFocus from "../common/useModalTitleFocus";

type Props = {
  visible: boolean;
  title: string;
  message: string;
  icon?: keyof typeof Ionicons.glyphMap;
  primaryLabel: string;
  onPrimary: () => void;
  secondaryLabel?: string;
  onSecondary?: () => void;
};

export default function PremiumNoticeModal({
  visible,
  title,
  message,
  icon = "diamond-outline",
  primaryLabel,
  onPrimary,
  secondaryLabel,
  onSecondary,
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
      onRequestClose={
        onSecondary ?? onPrimary
      }
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
              backgroundColor:
                theme.surface,
            },
          ]}
        >
          <ScrollView
            contentContainerStyle={styles.content}
            showsVerticalScrollIndicator={false}
          >
            <View
              style={[
                styles.iconWrap,
                {
                  backgroundColor:
                    theme.accentSoft,
                },
              ]}
            >
              <Ionicons
                name={icon}
                size={26}
                color={theme.accent}
              />
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
                {
                  color:
                    theme.textSecondary,
                },
              ]}
            >
              {message}
            </Text>

            <View style={styles.actions}>
              {secondaryLabel &&
              onSecondary ? (
                <SubtlePressable
                  accessibilityRole="button"
                  style={[
                    styles.secondaryButton,
                    {
                      backgroundColor:
                        theme.background,
                    },
                  ]}
                  onPress={onSecondary}
                >
                  <Text
                    style={[
                      styles.secondaryText,
                      { color: theme.text },
                    ]}
                  >
                    {secondaryLabel}
                  </Text>
                </SubtlePressable>
              ) : null}

              <SubtlePressable
                accessibilityRole="button"
                style={[
                  styles.primaryButton,
                  {
                    backgroundColor:
                      theme.buttonBackground,
                    borderColor:
                      theme.buttonBorder,
                  },
                ]}
                onPress={onPrimary}
              >
                <Text
                  style={[
                    styles.primaryText,
                    {
                      color:
                        theme.buttonText,
                    },
                  ]}
                >
                  {primaryLabel}
                </Text>
              </SubtlePressable>
            </View>
          </ScrollView>
        </Animated.View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    justifyContent: "center",
    padding: 26,
    backgroundColor:
      "rgba(18, 18, 18, 0.48)",
  },

  card: {
    maxHeight: "86%",
    borderRadius: 8,
  },

  content: {
    padding: 24,
  },

  iconWrap: {
    width: 54,
    height: 54,
    borderRadius: 27,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 18,
  },

  title: {
    fontSize: 24,
    fontWeight: "700",
  },

  message: {
    marginTop: 10,
    fontSize: 16,
    lineHeight: 24,
  },

  actions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 10,
    marginTop: 24,
  },

  secondaryButton: {
    minHeight: 44,
    borderRadius: 8,
    paddingHorizontal: 16,
    alignItems: "center",
    justifyContent: "center",
  },

  secondaryText: {
    fontSize: 14,
    fontWeight: "700",
  },

  primaryButton: {
    minHeight: 44,
    borderRadius: 8,
    borderWidth: 1.5,
    paddingHorizontal: 18,
    alignItems: "center",
    justifyContent: "center",
  },

  primaryText: {
    fontSize: 14,
    fontWeight: "700",
  },
});
