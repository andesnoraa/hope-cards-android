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
        <Text style={styles.emptyIcon}>
          ❤️
        </Text>

        <Text style={styles.emptyTitle}>
          No Favorites Yet
        </Text>

        <Text style={styles.emptySubtitle}>
          Save verses you love and they'll
          appear here.
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
    padding: 20,
    backgroundColor: "#F8F6F2",
    flexGrow: 1,
  },

  emptyContainer: {
    flex: 1,

    backgroundColor: "#F8F6F2",

    justifyContent: "center",
    alignItems: "center",

    paddingHorizontal: 40,
  },

  emptyIcon: {
    fontSize: 48,
    marginBottom: 20,
  },

  emptyTitle: {
    fontSize: 28,

    fontWeight: "700",

    color: "#1A2747",

    marginBottom: 12,
  },

  emptySubtitle: {
    textAlign: "center",

    fontSize: 18,

    color: "#777",

    lineHeight: 28,
  },
});