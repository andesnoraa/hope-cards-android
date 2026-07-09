import { Pressable, StyleSheet, Text } from "react-native";

import type { Verse } from "../../types/verse";

type Props = {
    verse: Verse;
    onPress: () => void;
};

export default function FavoriteCard({
    verse,
    onPress,
}: Props) {
    return (
        <Pressable
            style={styles.card}
            onPress={onPress}
        >
            <Text style={styles.category}>
                {verse.category}
            </Text>

            <Text
                style={styles.verse}
                numberOfLines={3}
            >
                {verse.fullVerse}
            </Text>

            <Text style={styles.reference}>
                {verse.reference}
            </Text>
        </Pressable>
    );
}

const styles = StyleSheet.create({
    card: {
        backgroundColor: "#F8F6F2",

        borderRadius: 22,

        borderWidth: 1.5,
        borderColor: "#C5A24C",

        padding: 20,

        marginBottom: 16,

        shadowColor: "#000",
        shadowOpacity: 0.08,
        shadowRadius: 8,
        shadowOffset: {
            width: 0,
            height: 4,
        },

        elevation: 3,
    },

    category: {
        color: "#C5A24C",

        fontSize: 14,

        fontWeight: "700",

        letterSpacing: 2,

        textTransform: "uppercase",

        marginBottom: 12,
    },

    verse: {
        color: "#273043",

        fontSize: 18,

        lineHeight: 28,

        marginBottom: 18,
    },

    reference: {
        color: "#273043",

        fontSize: 18,

        fontWeight: "700",
    },
});