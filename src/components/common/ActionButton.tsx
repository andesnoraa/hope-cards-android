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
    color = "#273043",
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
                    size={20}
                    color={color}
                />

                <Text
                    style={[
                        styles.label,
                        {
                            color,
                        },
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
        paddingVertical: 8,
        paddingHorizontal: 12,
        borderRadius: 20,
    },

    pressed: {
        opacity: 0.6,
    },

    content: {
        flexDirection: "row",
        alignItems: "center",
    },

    label: {
        marginLeft: 6,
        fontSize: 15,
        fontWeight: "600",
    },
});