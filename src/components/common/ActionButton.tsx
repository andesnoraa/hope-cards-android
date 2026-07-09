import Ionicons from "@expo/vector-icons/Ionicons";
import { Pressable, StyleSheet, Text, View } from "react-native";

type Props = {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  onPress: () => void;
  color?: string;
};

export default function ActionButton({
  icon,
  label,
  onPress,
  color = "#C5A24C",
}: Props) {
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.button,
        pressed && styles.pressed,
      ]}
    >
      <View style={styles.content}>
        <Ionicons
          name={icon}
          size={22}
          color={color}
        />

        <Text
          style={[
            styles.label,
            { color },
          ]}
        >
          {label}
        </Text>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    width: 120,
    alignItems: "center",
    justifyContent: "center",
    paddingVertical: 8,
    borderRadius: 20,
  },

  pressed: {
    opacity: 0.65,
    transform: [{ scale: 0.96 }],
  },

  content: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
  },

  label: {
    marginLeft: 6,
    fontSize: 16,
    fontWeight: "600",
  },
});