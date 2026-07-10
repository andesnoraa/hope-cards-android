import { StyleSheet } from "react-native";
import Animated from "react-native-reanimated";

import type { Verse } from "../../types/verse";

import CardBack from "./CardBack";
import CardFront from "./CardFront";

type Props = {
    animatedStyle: any;
    showFront: boolean;
    verse: Verse;
    favorite: boolean;
    onToggleFavorite: () => void;
    onShare: () => void;
};

export default function FlipCard({
    animatedStyle,
    showFront,
    verse,
    favorite,
    onToggleFavorite,
    onShare,
}: Props) {
    return (
        <Animated.View
            style={[styles.container, animatedStyle]}
        >
            <Animated.View
                pointerEvents="none"
                style={[
                    styles.face,
                    {
                        opacity: showFront ? 0 : 1,
                    },
                ]}
            >
                <CardBack />
            </Animated.View>

            <Animated.View
                pointerEvents="none"
                style={[
                    styles.face,
                    {
                        opacity: showFront ? 1 : 0,
                    },
                ]}
            >
                <CardFront
                    verse={verse}
                    favorite={favorite}
                    onToggleFavorite={onToggleFavorite}
                    onShare={onShare}
                />
            </Animated.View>
        </Animated.View>
    );
}

const styles = StyleSheet.create({
    container: {
        position: "absolute",
        top: 0,
        left: 0,
        width: 355,
        height: 510,
        justifyContent: "center",
        alignItems: "center",
        zIndex: 10,
    },

    face: {
        width: 345,
        height: 500,
        position: "absolute",
    },
});