import { StyleSheet, View } from "react-native";

import type { Verse } from "../../types/verse";

import CardFront from "./CardFront";

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
    width: 345,
    height: 500,
    justifyContent: "center",
    alignItems: "center",
  },
});