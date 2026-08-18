import "expo-sqlite/localStorage/install";

const JOURNAL_KEY = "hope_cards_journal_entries";

export interface JournalEntry {
  id: string;
  date: string;
  verseId: string;
  reference: string;
  prompt: string;
  note: string;
  updatedAt: string;
}

const REFLECTION_PROMPTS: Record<string, string> = {
  comfort:
    "Where do you need to receive comfort today?",
  courage:
    "What would one small act of courage look like today?",
  faith:
    "What are you being invited to trust, even without seeing the whole path?",
  freedom:
    "What burden can you begin to release today?",
  grace:
    "Where can you receive or extend grace today?",
  hope:
    "What possibility does this verse invite you to hold onto?",
  joy:
    "What quiet gift can you notice and give thanks for today?",
  life:
    "What is helping you feel fully alive right now?",
  love:
    "Who might need to experience love through you today?",
  peace:
    "What can you place in God’s hands to make room for peace?",
  prayer:
    "What honest prayer rises from this verse?",
  strength:
    "Where do you need strength for the next faithful step?",
  trust:
    "What are you holding tightly that you could entrust to God?",
  wisdom:
    "What choice could you approach with greater wisdom today?",
};

export function getReflectionPrompt(
  category: string
): string {
  return (
    REFLECTION_PROMPTS[category.toLowerCase()] ??
    "What is this verse inviting you to notice, trust, or practice today?"
  );
}

export function getJournalEntryId(
  date: string,
  verseId: string
) {
  return `${date}:${verseId}`;
}

export function getJournalEntries(): JournalEntry[] {
  try {
    const value = localStorage.getItem(JOURNAL_KEY);
    const entries: unknown = value
      ? JSON.parse(value)
      : [];

    return Array.isArray(entries)
      ? entries.filter(isJournalEntry)
      : [];
  } catch (error) {
    console.error("Failed to load journal entries:", error);
    return [];
  }
}

export function getJournalEntry(
  id: string
): JournalEntry | null {
  return (
    getJournalEntries().find(
      (entry) => entry.id === id
    ) ?? null
  );
}

export function saveJournalEntry(
  entry: JournalEntry
) {
  const entries = getJournalEntries();
  const next = entries.filter(
    (item) => item.id !== entry.id
  );

  if (entry.note.trim()) {
    next.push({
      ...entry,
      note: entry.note.trim(),
    });
  }

  localStorage.setItem(
    JOURNAL_KEY,
    JSON.stringify(next)
  );
}

export function replaceJournalEntries(
  entries: JournalEntry[]
) {
  localStorage.setItem(
    JOURNAL_KEY,
    JSON.stringify(entries.filter(isJournalEntry))
  );
}

export function isJournalEntry(
  value: unknown
): value is JournalEntry {
  if (typeof value !== "object" || value === null) {
    return false;
  }

  const entry = value as Partial<JournalEntry>;

  return (
    typeof entry.id === "string" &&
    typeof entry.date === "string" &&
    typeof entry.verseId === "string" &&
    typeof entry.reference === "string" &&
    typeof entry.prompt === "string" &&
    typeof entry.note === "string" &&
    typeof entry.updatedAt === "string"
  );
}
