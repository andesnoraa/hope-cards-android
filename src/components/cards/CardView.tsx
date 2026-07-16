import { StyleSheet, View } from "react-native";

import type { Verse } from "../../types/verse";

import CardFront from "./CardFront";
import { CARD_HEIGHT, CARD_WIDTH } from "./cardDimensions";

type Props = {
  verse: Verse;
  favorite: boolean;
  onToggleFavorite: () => void;
  onShare: () => void;
};

export default function CardView({
  verse,
  favorite,
  onToggleFavorite,
  onShare,
}: Props) {
  return (
    <View style={styles.container}>
      <CardFront
        verse={verse}
        favorite={favorite}
        onToggleFavorite={onToggleFavorite}
        onShare={onShare}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: CARD_WIDTH,
    height: CARD_HEIGHT,
    justifyContent: "center",
    alignItems: "center",
  },
});
