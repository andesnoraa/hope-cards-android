import { Stack, useLocalSearchParams } from "expo-router";
import { StyleSheet, Text, View } from "react-native";

import { verses } from "../../data/verses";

export default function VerseScreen() {
    const { id } = useLocalSearchParams();

    const verse = verses.find(
        (v) => v.id === Number(id)
    );

    if (!verse) {
        return (
            <>
                <Stack.Screen
                    options={{
                        title: "Verse",
                    }}
                />

                <View style={styles.container}>
                    <Text style={styles.title}>
                        Verse not found
                    </Text>
                </View>
            </>
        );
    }

    return (
        <>
            <Stack.Screen
                options={{
                    title: "Verse",
                }}
            />

            <View style={styles.container}>
                <Text style={styles.title}>
                    {verse.reference}
                </Text>

                <Text style={styles.subtitle}>
                    {verse.fullVerse}
                </Text>
            </View>
        </>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: "#F8F6F2",
        justifyContent: "center",
        alignItems: "center",
        padding: 24,
    },

    title: {
        fontSize: 30,
        fontWeight: "700",
        color: "#1A2747",
        textAlign: "center",
        marginBottom: 20,
    },

    subtitle: {
        fontSize: 20,
        color: "#444",
        textAlign: "center",
        lineHeight: 32,
    },
});