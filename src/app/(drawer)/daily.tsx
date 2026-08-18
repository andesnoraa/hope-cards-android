import {
  setAudioModeAsync,
  type AudioPlayer,
  useAudioPlayer,
} from "expo-audio";
import {
  router,
  useFocusEffect,
  useIsFocused,
  useNavigation,
} from "expo-router";
import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  AppState,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  Pressable,
} from "react-native";

import Animated, {
  FadeIn,
  FadeInDown,
} from "react-native-reanimated";
import { captureScreen } from "react-native-view-shot";

import ActionButton from "../../components/common/ActionButton";
import {
  REGULAR_CTA_HEIGHT,
  REGULAR_CTA_RADIUS,
} from "../../components/common/buttonStyles";

import {
  getDailyHope,
} from "../../services/dailyHope";
import {
  getDailyHopeMusic,
} from "../../services/dailyHopeMusic";

import {
  isFavorite,
  subscribeToFavorites,
  toggleFavorite,
} from "../../services/favorites";

import {
  shareVerse,
  shareVerseImage,
} from "../../services/share";
import {
  getPremiumStatus,
} from "../../services/premium";
import {
  getJournalEntry,
  getJournalEntryId,
  getReflectionPrompt,
  saveJournalEntry,
} from "../../services/journal";
import {
  getSettings,
} from "../../services/settings";

import {
  lightImpact,
  selection,
  success,
} from "../../services/haptics";

import type { Verse } from "../../types/verse";
import { useAppTheme } from "../../theme/appTheme";

function getVerseStyle(text: string) {
  const length = text.length;

  if (length <= 90) {
    return {
      fontSize: 24,
      lineHeight: 42,
    };
  }

  if (length <= 150) {
    return {
      fontSize: 22,
      lineHeight: 38,
    };
  }

  if (length <= 220) {
    return {
      fontSize: 20,
      lineHeight: 34,
    };
  }

  return {
    fontSize: 18,
    lineHeight: 31,
  };
}

function safelyPause(player: AudioPlayer) {
  try {
    if (player.isLoaded) {
      player.pause();
    }
  } catch {
    // The hook may release its native shared object before effect cleanup.
  }
}

function safelyPlay(player: AudioPlayer) {
  try {
    if (player.isLoaded) {
      player.play();
    }
  } catch {
    // Ignore a late lifecycle update after the native player is released.
  }
}

function getLocalDateKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(
    date.getMonth() + 1
  ).padStart(2, "0");
  const day = String(date.getDate()).padStart(
    2,
    "0"
  );

  return `${year}-${month}-${day}`;
}

export default function DailyHopeScreen() {
  const { theme } = useAppTheme();
  const isFocused = useIsFocused();
  const navigation = useNavigation();
  const musicPlayer = useAudioPlayer(null);

  const [isPremium, setIsPremium] =
    useState<boolean | null>(null);

  const [musicEnabled, setMusicEnabled] =
    useState(false);

  const [appState, setAppState] =
    useState(AppState.currentState);

  const [verse, setVerse] =
    useState<Verse | null>(null);

  const [favorite, setFavorite] =
    useState(false);

  const [isCapturingShare, setIsCapturingShare] =
    useState(false);

  const [journalNote, setJournalNote] =
    useState("");

  const [savedJournalNote, setSavedJournalNote] =
    useState("");

  const today = useMemo(() => {
    const parts =
      new Intl.DateTimeFormat("en-GB", {
        weekday: "long",
        day: "numeric",
        month: "long",
      }).formatToParts(new Date());

    const weekday =
      parts.find(
        (p) => p.type === "weekday"
      )?.value ?? "";

    const day =
      parts.find(
        (p) => p.type === "day"
      )?.value ?? "";

    const month =
      parts.find(
        (p) => p.type === "month"
      )?.value ?? "";

    return `${weekday} • ${day} ${month}`;
  }, []);

  useFocusEffect(
    useCallback(() => {
      let mounted = true;

      async function loadPremium() {
        const [status, settings] =
          await Promise.all([
            getPremiumStatus(),
            getSettings(),
          ]);

        if (mounted) {
          setIsPremium(
            status.isPremium
          );

          setMusicEnabled(
            status.isPremium &&
              settings
                .dailyHopeMusicEnabled
          );
        }
      }

      loadPremium();

      return () => {
        mounted = false;
      };
    }, [])
  );

  useEffect(() => {
    setAudioModeAsync({
      playsInSilentMode: true,
      shouldPlayInBackground: false,
      interruptionMode: "doNotMix",
    }).catch((error) => {
      console.error(
        "Unable to configure Daily Hope audio:",
        error
      );
    });
  }, []);

  useEffect(() => {
    const subscription =
      AppState.addEventListener(
        "change",
        setAppState
      );

    return () => {
      subscription.remove();
    };
  }, []);

  useFocusEffect(
    useCallback(() => {
      let mounted = true;

      async function load() {
        if (isPremium === null) {
          return;
        }

        const todayVerse =
          await getDailyHope();

        if (!mounted) {
          return;
        }

        setVerse(todayVerse);

        if (isPremium) {
          const entry = getJournalEntry(
            getJournalEntryId(
              getLocalDateKey(),
              todayVerse.id
            )
          );
          const note = entry?.note ?? "";

          setJournalNote(note);
          setSavedJournalNote(note);
        } else {
          setJournalNote("");
          setSavedJournalNote("");
        }

        const saved =
          await isFavorite(todayVerse.id);

        setFavorite(saved);

        if (isPremium) {
          setTimeout(() => {
            lightImpact();
          }, 1300);
        }
      }

      load();

      return () => {
        mounted = false;
      };
    }, [isPremium])
  );

  useEffect(() => {
    if (!verse) {
      return;
    }

    const unsubscribe =
      subscribeToFavorites(async () => {
        const saved =
          await isFavorite(
            verse.id
          );

        setFavorite(saved);
      });

    return unsubscribe;
  }, [verse]);

  useEffect(() => {
    if (!verse) {
      return;
    }

    musicPlayer.replace(
      getDailyHopeMusic(verse.category)
    );
    musicPlayer.loop = true;
    musicPlayer.volume = 0.45;

  }, [musicPlayer, verse]);

  useEffect(() => {
    const shouldPlay =
      isFocused &&
      appState === "active" &&
      isPremium === true &&
      musicEnabled &&
      verse !== null;

    if (shouldPlay) {
      safelyPlay(musicPlayer);
    } else {
      safelyPause(musicPlayer);
    }
  }, [
    appState,
    isFocused,
    isPremium,
    musicEnabled,
    musicPlayer,
    verse,
  ]);

  async function handleFavorite() {
    if (!verse) return;

    const saved =
      await toggleFavorite(
        verse.id
      );

    setFavorite(saved);

    if (saved) {
      await success();
    } else {
      await selection();
    }
  }

  async function handleShare() {
    if (!verse) return;

    try {
      navigation.setOptions({
        headerShown: false,
      });
      setIsCapturingShare(true);

      await new Promise<void>((resolve) => {
        requestAnimationFrame(() => {
          requestAnimationFrame(() => {
            setTimeout(resolve, 120);
          });
        });
      });

      const imageUri = await captureScreen({
        format: "png",
        quality: 1,
        result: "tmpfile",
      });

      setIsCapturingShare(false);
      navigation.setOptions({
        headerShown: true,
      });

      await shareVerseImage(
        imageUri,
        verse
      );
    } catch (error) {
      console.error(
        "Failed to share Daily Hope image:",
        error
      );

      setIsCapturingShare(false);
      navigation.setOptions({
        headerShown: true,
      });
      await shareVerse(verse);
    }
  }

  async function handleSaveReflection() {
    if (!verse || !isPremium) return;

    const date = getLocalDateKey();
    const note = journalNote.trim();

    saveJournalEntry({
      id: getJournalEntryId(date, verse.id),
      date,
      verseId: verse.id,
      reference: verse.reference,
      prompt: getReflectionPrompt(verse.category),
      note,
      updatedAt: new Date().toISOString(),
    });

    setJournalNote(note);
    setSavedJournalNote(note);
    await success();
  }

  if (isPremium === null) {
    return null;
  }

  if (!verse) {
    return null;
  }

  const verseStyle = getVerseStyle(verse.verse);

  if (isCapturingShare) {
    return (
      <View
        style={[
          styles.sharePresentation,
          {
            backgroundColor: theme.background,
          },
        ]}
      >
        <View style={styles.sharePresentationMain}>
          <View style={styles.sharePresentationHeader}>
            <Text
              style={[
                styles.sharePresentationTitle,
                { color: theme.text },
              ]}
            >
              Today’s Hope
            </Text>
            <Text
              style={[
                styles.sharePresentationDate,
                { color: theme.textSecondary },
              ]}
            >
              {today}
            </Text>
          </View>

          <View style={styles.sharePresentationVerse}>
            <Text
              style={[
                styles.sharePresentationReference,
                { color: theme.text },
              ]}
            >
              {verse.reference}
            </Text>
            <View
              style={[
                styles.sharePresentationDivider,
                { backgroundColor: theme.accent },
              ]}
            />
            <Text
              style={[
                styles.sharePresentationText,
                verseStyle,
                { color: theme.cardText },
              ]}
            >
              {verse.verse}
            </Text>
          </View>
        </View>

        <View style={styles.sharePresentationFooter}>
          <Text
            style={[
              styles.sharePresentationTranslation,
              { color: theme.textTertiary },
            ]}
          >
            {verse.translation}
          </Text>
          <Text
            style={[
              styles.sharePresentationBrand,
              { color: theme.accent },
            ]}
          >
            HOPE CARDS
          </Text>
        </View>
      </View>
    );
  }

  return (
    <ScrollView
      contentInsetAdjustmentBehavior="automatic"
      style={[
        styles.container,
        {
          backgroundColor:
            theme.background,
        },
      ]}
      contentContainerStyle={
        styles.content
      }
      showsVerticalScrollIndicator={false}
      keyboardShouldPersistTaps="handled"
    >
      <Animated.View
          entering={FadeIn.duration(600)}
          style={styles.header}
        >
          <Text
            style={[
              styles.title,
              { color: theme.text },
            ]}
          >
            Today’s Hope
          </Text>

          <Animated.Text
            entering={FadeIn.delay(300).duration(
              600
            )}
            style={[
              styles.date,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            {today}
          </Animated.Text>
        </Animated.View>

        <Animated.View
          entering={FadeInDown.delay(900).duration(
            1000
          )}
          style={styles.verseContainer}
        >
          <Text
            style={[
              styles.reference,
              { color: theme.text },
            ]}
          >
            {verse.reference}
          </Text>

          <View
            style={[
              styles.separator,
              {
                backgroundColor:
                  theme.accent,
              },
            ]}
          />

          <Text
            selectable
            style={[
              styles.verse,
              verseStyle,
              { color: theme.cardText },
            ]}
          >
            {verse.verse}
          </Text>
        </Animated.View>

      <Animated.Text
          entering={FadeIn.delay(2100).duration(
            600
          )}
          style={[
            styles.translation,
            {
              color:
                theme.textTertiary,
            },
          ]}
        >
          {verse.translation}
      </Animated.Text>

      <Animated.View
        entering={FadeIn.delay(1900).duration(
          600
        )}
        style={styles.actions}
      >
        <ActionButton
          icon={
            favorite
              ? "heart"
              : "heart-outline"
          }
          label={
            favorite
              ? "Saved"
              : "Save"
          }
          color={
            favorite
              ? theme.danger
              : theme.accent
          }
          onPress={handleFavorite}
        />

        <ActionButton
          icon="share-outline"
          label="Share"
          color={theme.accent}
          onPress={handleShare}
        />
      </Animated.View>

      {isPremium && (
        <View
          style={[
            styles.reflectionCard,
            {
              backgroundColor: theme.surface,
              borderColor: theme.accentLine,
            },
          ]}
        >
          <Text
            style={[
              styles.reflectionEyebrow,
              { color: theme.accent },
            ]}
          >
            GUIDED REFLECTION
          </Text>
          <Text
            selectable
            style={[
              styles.reflectionPrompt,
              { color: theme.text },
            ]}
          >
            {getReflectionPrompt(verse.category)}
          </Text>
          <TextInput
            accessibilityLabel="Private reflection note"
            multiline
            maxLength={1000}
            placeholder="Write what comes to mind…"
            placeholderTextColor={theme.textTertiary}
            selectionColor={theme.accent}
            value={journalNote}
            onChangeText={setJournalNote}
            style={[
              styles.reflectionInput,
              {
                backgroundColor: theme.background,
                borderColor: theme.divider,
                color: theme.cardText,
              },
            ]}
          />
          <View style={styles.reflectionFooter}>
            <Text
              style={[
                styles.reflectionPrivacy,
                { color: theme.textTertiary },
              ]}
            >
              Private on this device
            </Text>
            <Text
              style={[
                styles.reflectionCount,
                { color: theme.textTertiary },
              ]}
            >
              {journalNote.length}/1000
            </Text>
          </View>
          <Pressable
            accessibilityRole="button"
            accessibilityLabel="Save reflection"
            disabled={
              journalNote.trim() === savedJournalNote
            }
            style={[
              styles.reflectionSaveButton,
              {
                backgroundColor:
                  theme.buttonBackground,
                borderColor: theme.buttonBorder,
              },
              journalNote.trim() ===
                savedJournalNote &&
                styles.reflectionSaveButtonDisabled,
            ]}
            onPress={handleSaveReflection}
          >
            <Text
              style={[
                styles.reflectionSaveButtonText,
                { color: theme.buttonText },
              ]}
            >
              {savedJournalNote
                ? "Update Reflection"
                : "Save Reflection"}
            </Text>
          </Pressable>
        </View>
      )}

      {!isPremium && (
        <View
          style={[
            styles.premiumPrompt,
            isCapturingShare &&
              styles.hiddenDuringCapture,
            {
              backgroundColor: theme.surface,
              borderColor: theme.accentLine,
            },
          ]}
        >
          <Text
            style={[
              styles.premiumPromptTitle,
              { color: theme.text },
            ]}
          >
            Make Daily Hope part of your routine
          </Text>
          <Text
            style={[
              styles.premiumPromptCopy,
              { color: theme.textSecondary },
            ]}
          >
            Add guided reflections, a private journal,
            gentle reminders, music, and device backup.
          </Text>
          <Pressable
            style={[
              styles.premiumButton,
              {
                backgroundColor:
                  theme.buttonBackground,
                borderColor:
                  theme.buttonBorder,
              },
            ]}
            onPress={() => {
              router.push("/premium");
            }}
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
      )}

    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  content: {
    alignItems: "center",
    paddingHorizontal: 28,
    paddingTop: 32,
    paddingBottom: 60,
  },

  sharePresentation: {
    flex: 1,
    alignItems: "center",
    paddingHorizontal: 28,
    paddingTop: 96,
    paddingBottom: 76,
  },

  sharePresentationHeader: {
    alignItems: "center",
    gap: 10,
  },

  sharePresentationMain: {
    flex: 1,
    width: "100%",
    alignItems: "center",
    justifyContent: "center",
    gap: 64,
  },

  sharePresentationTitle: {
    fontSize: 36,
    lineHeight: 44,
    fontWeight: "700",
    textAlign: "center",
  },

  sharePresentationDate: {
    fontSize: 16,
    lineHeight: 23,
    letterSpacing: 0.3,
  },

  sharePresentationVerse: {
    width: "82%",
    alignItems: "center",
  },

  sharePresentationReference: {
    fontSize: 28,
    lineHeight: 36,
    fontWeight: "700",
    textAlign: "center",
  },

  sharePresentationDivider: {
    width: 56,
    height: 2,
    borderRadius: 2,
    opacity: 0.75,
    marginTop: 20,
    marginBottom: 38,
  },

  sharePresentationText: {
    fontFamily: "SourceSerif4_400Regular",
    textAlign: "center",
  },

  sharePresentationFooter: {
    alignItems: "center",
    gap: 22,
  },

  sharePresentationTranslation: {
    fontSize: 14,
    lineHeight: 20,
    textAlign: "center",
    letterSpacing: 0.4,
  },

  sharePresentationBrand: {
    fontSize: 13,
    lineHeight: 18,
    fontWeight: "700",
    letterSpacing: 3,
  },

  header: {
    alignItems: "center",
    marginBottom: 24,
  },

  title: {
    fontSize: 36,
    fontWeight: "700",
  },

  date: {
    marginTop: 8,
    fontSize: 16,
    letterSpacing: 0.4,
  },

  verseContainer: {
    width: "82%",
    alignItems: "center",
    marginTop: 12,
  },

  reference: {
    fontSize: 28,
    fontWeight: "700",
    textAlign: "center",
    marginBottom: 14,
  },

  separator: {
    width: 56,
    height: 2,
    borderRadius: 2,
    opacity: 0.75,
    marginBottom: 36,
  },

  verse: {
    textAlign: "center",
    fontFamily:
      "SourceSerif4_400Regular",
  },

  actions: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    gap: 12,
    marginTop: 60,
  },

  translation: {
    marginTop: 48,
    fontSize: 14,
    textAlign: "center",
    letterSpacing: 0.5,
  },

  reflectionCard: {
    width: "100%",
    maxWidth: 420,
    padding: 20,
    gap: 14,
    borderWidth: 1,
    borderRadius: 20,
    borderCurve: "continuous",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.06)",
    marginTop: 40,
  },

  reflectionEyebrow: {
    fontSize: 12,
    lineHeight: 16,
    fontWeight: "700",
    letterSpacing: 1.5,
  },

  reflectionPrompt: {
    fontSize: 20,
    lineHeight: 29,
    fontWeight: "600",
  },

  reflectionInput: {
    minHeight: 132,
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderWidth: 1,
    borderRadius: 14,
    borderCurve: "continuous",
    fontFamily: "SourceSerif4_400Regular",
    fontSize: 18,
    lineHeight: 27,
    textAlignVertical: "top",
  },

  reflectionFooter: {
    flexDirection: "row",
    justifyContent: "space-between",
    gap: 12,
  },

  reflectionPrivacy: {
    flex: 1,
    fontSize: 12,
    lineHeight: 17,
  },

  reflectionCount: {
    fontSize: 12,
    lineHeight: 17,
    fontVariant: ["tabular-nums"],
  },

  reflectionSaveButton: {
    minHeight: 50,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 2,
    borderRadius: 14,
    borderCurve: "continuous",
  },

  reflectionSaveButtonDisabled: {
    opacity: 0.45,
  },

  reflectionSaveButtonText: {
    fontSize: 16,
    lineHeight: 22,
    fontWeight: "700",
  },

  premiumPrompt: {
    width: "100%",
    maxWidth: 360,
    padding: 20,
    borderWidth: 1,
    borderRadius: 20,
    borderCurve: "continuous",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.06)",
    marginTop: 40,
    alignItems: "center",
  },

  hiddenDuringCapture: {
    opacity: 0,
  },

  premiumPromptTitle: {
    fontSize: 18,
    lineHeight: 25,
    fontWeight: "700",
    textAlign: "center",
  },

  premiumPromptCopy: {
    marginTop: 8,
    fontSize: 14,
    lineHeight: 21,
    textAlign: "center",
  },

  premiumButton: {
    width: "100%",
    height: REGULAR_CTA_HEIGHT,
    borderRadius: REGULAR_CTA_RADIUS,
    borderWidth: 2,
    alignItems: "center",
    justifyContent: "center",
    marginTop: 18,
  },

  premiumButtonText: {
    fontSize: 15,
    fontWeight: "700",
  },
});
