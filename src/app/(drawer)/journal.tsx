import Ionicons from "@expo/vector-icons/Ionicons";
import { router, useFocusEffect } from "expo-router";
import { useCallback, useState } from "react";
import {
  Alert,
  FlatList,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

import {
  getJournalEntries,
  saveJournalEntry,
  type JournalEntry,
} from "../../services/journal";
import {
  getPremiumStatus,
} from "../../services/premium";
import { useAppTheme } from "../../theme/appTheme";

function formatJournalDate(date: string) {
  const parsed = new Date(`${date}T12:00:00`);

  return new Intl.DateTimeFormat("en-GB", {
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(parsed);
}

export default function JournalScreen() {
  const { theme } = useAppTheme();
  const [isPremium, setIsPremium] =
    useState<boolean | null>(null);
  const [entries, setEntries] =
    useState<JournalEntry[]>([]);
  const [editingEntry, setEditingEntry] =
    useState<JournalEntry | null>(null);
  const [draftNote, setDraftNote] = useState("");

  const reloadEntries = useCallback(() => {
    setEntries(
      getJournalEntries().sort((a, b) =>
        b.updatedAt.localeCompare(a.updatedAt)
      )
    );
  }, []);

  function beginEditing(entry: JournalEntry) {
    setEditingEntry(entry);
    setDraftNote(entry.note);
  }

  function saveEdit() {
    if (!editingEntry) return;

    saveJournalEntry({
      ...editingEntry,
      note: draftNote,
      updatedAt: new Date().toISOString(),
    });
    setEditingEntry(null);
    reloadEntries();
  }

  function confirmDelete(entry: JournalEntry) {
    Alert.alert(
      "Delete Reflection?",
      "This reflection will be removed from this device.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: () => {
            saveJournalEntry({ ...entry, note: "" });
            reloadEntries();
          },
        },
      ]
    );
  }

  useFocusEffect(
    useCallback(() => {
      let mounted = true;

      async function load() {
        const status = await getPremiumStatus();

        if (!mounted) return;

        setIsPremium(status.isPremium);
        setEntries(
          status.isPremium
            ? getJournalEntries().sort((a, b) =>
                b.updatedAt.localeCompare(a.updatedAt)
              )
            : []
        );
      }

      load();

      return () => {
        mounted = false;
      };
    }, [])
  );

  if (isPremium === null) {
    return null;
  }

  if (!isPremium) {
    return (
      <View
        style={[
          styles.centered,
          { backgroundColor: theme.background },
        ]}
      >
        <View
          style={[
            styles.lockedIcon,
            { backgroundColor: theme.accentSoft },
          ]}
        >
          <Ionicons
            name="journal-outline"
            size={34}
            color={theme.accent}
          />
        </View>
        <Text
          style={[
            styles.emptyTitle,
            { color: theme.text },
          ]}
        >
          Your private reflection space
        </Text>
        <Text
          style={[
            styles.emptyCopy,
            { color: theme.textSecondary },
          ]}
        >
          Premium includes guided prompts and a private
          journal connected to each Daily Hope verse.
        </Text>
        <Pressable
          accessibilityRole="button"
          style={[
            styles.premiumButton,
            {
              backgroundColor: theme.buttonBackground,
              borderColor: theme.buttonBorder,
            },
          ]}
          onPress={() => router.push("/premium")}
        >
          <Text
            style={[
              styles.premiumButtonText,
              { color: theme.buttonText },
            ]}
          >
            Explore Premium
          </Text>
        </Pressable>
      </View>
    );
  }

  return (
    <>
    <FlatList
      contentInsetAdjustmentBehavior="automatic"
      data={entries}
      keyExtractor={(entry) => entry.id}
      style={{ backgroundColor: theme.background }}
      contentContainerStyle={[
        styles.list,
        entries.length === 0 && styles.emptyList,
      ]}
      showsVerticalScrollIndicator={false}
      ListEmptyComponent={
        <View style={styles.centered}>
          <View
            style={[
              styles.lockedIcon,
              { backgroundColor: theme.accentSoft },
            ]}
          >
            <Ionicons
              name="journal-outline"
              size={34}
              color={theme.accent}
            />
          </View>
          <Text
            style={[
              styles.emptyTitle,
              { color: theme.text },
            ]}
          >
            Your journal is ready
          </Text>
          <Text
            style={[
              styles.emptyCopy,
              { color: theme.textSecondary },
            ]}
          >
            Open Daily Hope and save your first guided
            reflection. It will appear here.
          </Text>
        </View>
      }
      renderItem={({ item }) => (
        <View
          style={[
            styles.entryCard,
            {
              backgroundColor: theme.surface,
              borderColor: theme.accentLine,
            },
          ]}
        >
          <View style={styles.entryHeader}>
            <Text
              style={[
                styles.entryReference,
                { color: theme.text },
              ]}
            >
              {item.reference}
            </Text>
            <Text
              style={[
                styles.entryDate,
                { color: theme.textTertiary },
              ]}
            >
              {formatJournalDate(item.date)}
            </Text>
          </View>
          <Text
            selectable
            style={[
              styles.entryPrompt,
              { color: theme.accent },
            ]}
          >
            {item.prompt}
          </Text>
          <Text
            selectable
            style={[
              styles.entryNote,
              { color: theme.cardText },
            ]}
          >
            {item.note}
          </Text>
          <View style={styles.entryActions}>
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={`Edit reflection for ${item.reference}`}
              onPress={() => beginEditing(item)}
              style={styles.entryAction}
            >
              <Ionicons name="create-outline" size={18} color={theme.accent} />
              <Text style={[styles.entryActionText, { color: theme.accent }]}>Edit</Text>
            </Pressable>
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={`Delete reflection for ${item.reference}`}
              onPress={() => confirmDelete(item)}
              style={styles.entryAction}
            >
              <Ionicons name="trash-outline" size={18} color={theme.textTertiary} />
              <Text style={[styles.entryActionText, { color: theme.textTertiary }]}>Delete</Text>
            </Pressable>
          </View>
        </View>
      )}
    />
    <Modal
      transparent
      animationType="fade"
      visible={editingEntry !== null}
      onRequestClose={() => setEditingEntry(null)}
    >
      <View style={styles.modalBackdrop}>
        <View style={[styles.editor, { backgroundColor: theme.surface, borderColor: theme.accentLine }]}>
          <Text style={[styles.editorTitle, { color: theme.text }]}>Edit Reflection</Text>
          <Text style={[styles.entryPrompt, { color: theme.accent }]}>{editingEntry?.prompt}</Text>
          <TextInput
            autoFocus
            multiline
            maxLength={20000}
            value={draftNote}
            onChangeText={setDraftNote}
            accessibilityLabel="Reflection text"
            style={[styles.editorInput, { color: theme.cardText, borderColor: theme.accentLine }]}
          />
          <View style={styles.editorActions}>
            <Pressable onPress={() => setEditingEntry(null)} style={styles.editorButton}>
              <Text style={{ color: theme.textSecondary }}>Cancel</Text>
            </Pressable>
            <Pressable onPress={saveEdit} style={[styles.editorButton, { backgroundColor: theme.buttonBackground }]}>
              <Text style={{ color: theme.buttonText, fontWeight: "700" }}>Save</Text>
            </Pressable>
          </View>
        </View>
      </View>
    </Modal>
    </>
  );
}

const styles = StyleSheet.create({
  list: {
    flexGrow: 1,
    padding: 20,
    gap: 16,
  },
  emptyList: {
    justifyContent: "center",
  },
  centered: {
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 34,
    paddingBottom: 100,
  },
  lockedIcon: {
    width: 76,
    height: 76,
    borderRadius: 38,
    alignItems: "center",
    justifyContent: "center",
    marginBottom: 22,
  },
  emptyTitle: {
    fontSize: 28,
    lineHeight: 36,
    fontWeight: "700",
    textAlign: "center",
  },
  emptyCopy: {
    maxWidth: 360,
    marginTop: 12,
    fontSize: 16,
    lineHeight: 25,
    textAlign: "center",
  },
  premiumButton: {
    width: "100%",
    maxWidth: 360,
    minHeight: 56,
    marginTop: 28,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
    borderRadius: 16,
    borderCurve: "continuous",
  },
  premiumButtonText: {
    fontSize: 16,
    fontWeight: "700",
  },
  entryCard: {
    padding: 20,
    gap: 14,
    borderWidth: 1,
    borderRadius: 20,
    borderCurve: "continuous",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.06)",
  },
  entryHeader: {
    flexDirection: "row",
    alignItems: "baseline",
    justifyContent: "space-between",
    gap: 12,
  },
  entryReference: {
    flex: 1,
    fontSize: 18,
    lineHeight: 24,
    fontWeight: "700",
  },
  entryDate: {
    fontSize: 12,
    lineHeight: 17,
  },
  entryPrompt: {
    fontSize: 14,
    lineHeight: 21,
    fontWeight: "600",
  },
  entryNote: {
    fontFamily: "SourceSerif4_400Regular",
    fontSize: 18,
    lineHeight: 28,
  },
  entryActions: { flexDirection: "row", gap: 24, paddingTop: 2 },
  entryAction: { flexDirection: "row", alignItems: "center", gap: 6, minHeight: 44 },
  entryActionText: { fontSize: 14, fontWeight: "700" },
  modalBackdrop: { flex: 1, justifyContent: "center", padding: 24, backgroundColor: "rgba(0, 0, 0, 0.45)" },
  editor: { padding: 22, gap: 16, borderWidth: 1, borderRadius: 22, borderCurve: "continuous" },
  editorTitle: { fontSize: 24, lineHeight: 30, fontWeight: "700" },
  editorInput: { minHeight: 160, maxHeight: 320, padding: 14, borderWidth: 1, borderRadius: 14, textAlignVertical: "top", fontFamily: "SourceSerif4_400Regular", fontSize: 18, lineHeight: 27 },
  editorActions: { flexDirection: "row", justifyContent: "flex-end", gap: 10 },
  editorButton: { minWidth: 92, minHeight: 46, alignItems: "center", justifyContent: "center", paddingHorizontal: 18, borderRadius: 14, borderCurve: "continuous" },
});
