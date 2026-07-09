import { Ionicons } from "@expo/vector-icons";
import { router, useFocusEffect } from "expo-router";
import { useCallback, useState } from "react";
import {
  FlatList,
  StyleSheet,
  Text,
  View,
} from "react-native";

import FavoriteCard from "../../components/favorites/FavoriteCard";

import {
  getFavoriteVerses,
} from "../../services/favorites";

import type { Verse } from "../../types/verse";

export default function FavoritesScreen() {
  const [favorites, setFavorites] = useState<Verse[]>([]);

  useFocusEffect(
    useCallback(() => {
      async function loadFavorites() {
        const saved =
          await getFavoriteVerses();

        setFavorites(saved);
      }

      loadFavorites();
    }, [])
  );

  if (favorites.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <Ionicons
          name="heart"
          size={72}
          color="#D83A2E"
          style={styles.emptyIcon}
        />

        <Text style={styles.emptyTitle}>
          No Favorites Yet
        </Text>

        <Text style={styles.emptySubtitle}>
          Save verses you love and they'll appear here.
        </Text>
      </View>
    );
  }

  return (
    <FlatList
      data={favorites}
      keyExtractor={(item) =>
        item.id.toString()
      }
      contentContainerStyle={styles.list}
      showsVerticalScrollIndicator={false}
      renderItem={({ item }) => (
        <FavoriteCard
          verse={item}
          onPress={() =>
            router.push(`/verse/${item.id}`)
          }
        />
      )}
    />
  );
}

const styles = StyleSheet.create({
  list: {
    flexGrow: 1,

    padding: 20,

    backgroundColor: "#F8F6F2",
  },

  emptyContainer: {
    flex: 1,

    backgroundColor: "#F8F6F2",

    justifyContent: "center",

    alignItems: "center",

    paddingHorizontal: 50,

    // Move the empty state slightly upward
    paddingBottom: 140,
  },

  emptyIcon: {
    marginBottom: 24,
  },

  emptyTitle: {
    fontSize: 34,

    fontWeight: "700",

    color: "#1A2747",

    textAlign: "center",

    marginBottom: 18,
  },

  emptySubtitle: {
    textAlign: "center",

    fontSize: 20,

    color: "#777",

    lineHeight: 34,

    maxWidth: 320,
  },
});