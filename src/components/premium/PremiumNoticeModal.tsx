import Ionicons from "@expo/vector-icons/Ionicons";
import {
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { useAppTheme } from "../../theme/appTheme";

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

  return (
    <Modal
      transparent
      visible={visible}
      animationType="fade"
      onRequestClose={
        onSecondary ?? onPrimary
      }
    >
      <View style={styles.backdrop}>
        <View
          style={[
            styles.card,
            {
              backgroundColor:
                theme.surface,
            },
          ]}
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
            style={[
              styles.title,
              { color: theme.text },
            ]}
          >
            {title}
          </Text>

          <Text
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
              <Pressable
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
              </Pressable>
            ) : null}

            <Pressable
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
            </Pressable>
          </View>
        </View>
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
    borderRadius: 8,
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
