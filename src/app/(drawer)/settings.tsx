import Ionicons from "@expo/vector-icons/Ionicons";
import {
  router,
  useFocusEffect,
} from "expo-router";
import {
  useCallback,
  useRef,
  useState,
} from "react";

import {
  Alert,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  TextInput,
  View,
} from "react-native";
import Animated, {
  FadeInUp,
  ReduceMotion,
  useReducedMotion,
} from "react-native-reanimated";

import PremiumNoticeModal from "../../components/premium/PremiumNoticeModal";
import StatusNoticeModal from "../../components/common/StatusNoticeModal";
import SubtlePressable from "../../components/common/SubtlePressable";
import useModalTitleFocus from "../../components/common/useModalTitleFocus";

import {
  exportBackup,
  loadBackup,
  loadLatestLocalBackup,
  type BackupData,
} from "../../services/backup";

import {
  disableDailyHopeReminder,
  enableDailyHopeReminder,
  formatReminderTime,
  syncDailyHopeReminderSchedule,
  updateDailyHopeReminderTime,
} from "../../services/dailyHopeNotifications";

import {
  BackupInfo,
  getBackupInfo,
} from "../../services/backupInfo";

import {
  restoreBackup,
} from "../../services/restore";

import {
  success,
} from "../../services/haptics";

import {
  getSettings,
  updateSettings,
} from "../../services/settings";
import {
  TRANSLATION_OPTIONS,
  type TranslationId,
} from "../../services/verseService";
import {
  getPremiumStatus,
  isPremiumTheme,
} from "../../services/premium";

import {
  THEME_OPTIONS,
  useAppTheme,
  type AppThemeName,
} from "../../theme/appTheme";

import {
  formatRelativeDate,
} from "../../utils/formatDate";

type TimeInputSelection = {
  start: number;
  end: number;
};

export default function SettingsScreen() {
  const {
    theme,
    themeName,
    setThemeName,
  } = useAppTheme();
  const reduceMotion = useReducedMotion();

  const [showDrawButton, setShowDrawButton] =
    useState(true);

  const [enableHaptics, setEnableHaptics] =
    useState(true);

  const [preferredTranslation, setPreferredTranslation] =
    useState<TranslationId>("bsb");

  const [
    dailyHopeMusicEnabled,
    setDailyHopeMusicEnabled,
  ] = useState(true);

  const [
    dailyHopeReminderEnabled,
    setDailyHopeReminderEnabled,
  ] = useState(false);

  const [
    dailyHopeReminderHour,
    setDailyHopeReminderHour,
  ] = useState(8);

  const [
    dailyHopeReminderMinute,
    setDailyHopeReminderMinute,
  ] = useState(0);

  const [
    timePickerVisible,
    setTimePickerVisible,
  ] = useState(false);

  const [
    themePickerVisible,
    setThemePickerVisible,
  ] = useState(false);

  const [
    translationPickerVisible,
    setTranslationPickerVisible,
  ] = useState(false);

  const [
    pickerHourInput,
    setPickerHourInput,
  ] = useState("08");

  const [
    pickerMinuteInput,
    setPickerMinuteInput,
  ] = useState("00");

  const [
    pickerHourSelection,
    setPickerHourSelection,
  ] = useState<TimeInputSelection>();

  const [
    pickerMinuteSelection,
    setPickerMinuteSelection,
  ] = useState<TimeInputSelection>();

  const [
    pickerPeriod,
    setPickerPeriod,
  ] = useState<"AM" | "PM">("AM");

  const [backupInfo, setBackupInfo] =
    useState<BackupInfo | null>(null);

  const [isPremium, setIsPremium] =
    useState(false);

  const selectionLock = useRef(false);
  const reminderSaveLock = useRef(false);
  const backupLock = useRef(false);
  const restoreLock = useRef(false);

  const [isReminderSaving, setIsReminderSaving] =
    useState(false);
  const [isBackupProcessing, setIsBackupProcessing] =
    useState(false);
  const [isRestoreProcessing, setIsRestoreProcessing] =
    useState(false);

  const [successNotice, setSuccessNotice] =
    useState<{
      title: string;
      message: string;
      icon: keyof typeof Ionicons.glyphMap;
    } | null>(null);

  const translationTitleRef =
    useModalTitleFocus(
      translationPickerVisible
    );
  const themeTitleRef =
    useModalTitleFocus(themePickerVisible);
  const timeTitleRef =
    useModalTitleFocus(timePickerVisible);

  const [
    premiumPrompt,
    setPremiumPrompt,
  ] = useState<string | null>(null);

  const selectedTheme =
    THEME_OPTIONS.find(
      (option) =>
        option.name === themeName
    ) ?? THEME_OPTIONS[0];

  const selectedTranslation =
    TRANSLATION_OPTIONS.find(
      (option) =>
        option.id === preferredTranslation
    ) ?? TRANSLATION_OPTIONS[0];

  useFocusEffect(
    useCallback(() => {
      let mounted = true;

      async function loadSettings() {
        const premiumStatus =
          await getPremiumStatus();

        const settings =
          await getSettings();

        if (!mounted) {
          return;
        }

        setIsPremium(
          premiumStatus.isPremium
        );

        setShowDrawButton(
          settings.showDrawButton
        );

        setEnableHaptics(
          settings.enableHaptics
        );

        setPreferredTranslation(
          settings.preferredTranslation
        );

        setDailyHopeMusicEnabled(
          settings.dailyHopeMusicEnabled
        );

        setDailyHopeReminderEnabled(
          premiumStatus.isPremium
            ? settings.dailyHopeReminderEnabled
            : false
        );

        setDailyHopeReminderHour(
          settings.dailyHopeReminderHour
        );

        setDailyHopeReminderMinute(
          settings.dailyHopeReminderMinute
        );

        const info =
          await getBackupInfo();

        setBackupInfo(info);

        if (!premiumStatus.isPremium) {
          if (
            settings
              .dailyHopeReminderEnabled
          ) {
            await disableDailyHopeReminder();
            await updateSettings({
              dailyHopeReminderEnabled:
                false,
            });
          }

          if (
            isPremiumTheme(
              settings.themeName
            )
          ) {
            setThemeName("classic");

            await updateSettings({
              themeName: "classic",
              dailyHopeReminderEnabled:
                false,
            });
          }

        }
      }

      loadSettings();

      return () => {
        mounted = false;
      };
    }, [setThemeName])
  );

  function openPremiumPrompt(
    feature: string
  ) {
    setPremiumPrompt(feature);
  }

  function getPremiumPromptMessage(
    feature: string | null
  ) {
    if (!feature) {
      return "";
    }

    const verb =
      [
        "Premium themes",
        "Daily reminders",
      ].includes(feature)
        ? "are"
        : "is";

    return `${feature} ${verb} included with Hope Cards Premium.`;
  }

  async function toggleDrawButton(
    value: boolean
  ) {
    setShowDrawButton(value);

    await updateSettings({
      showDrawButton: value,
    });
  }

  async function toggleHaptics(
    value: boolean
  ) {
    setEnableHaptics(value);

    await updateSettings({
      enableHaptics: value,
    });
  }

  async function selectTranslation(
    translation: TranslationId
  ) {
    if (selectionLock.current) {
      return;
    }

    selectionLock.current = true;

    try {
      setPreferredTranslation(translation);
      setTranslationPickerVisible(false);

      await updateSettings({
        preferredTranslation: translation,
      });
    } finally {
      selectionLock.current = false;
    }
  }

  async function toggleDailyHopeMusic(
    value: boolean
  ) {
    if (!isPremium) {
      openPremiumPrompt(
        "Background music"
      );
      return;
    }

    setDailyHopeMusicEnabled(value);

    await updateSettings({
      dailyHopeMusicEnabled: value,
    });
  }

  function chooseTheme() {
    setThemePickerVisible(true);
  }

  async function selectTheme(
    nextTheme: AppThemeName
  ) {
    if (
      isPremiumTheme(nextTheme) &&
      !isPremium
    ) {
      openPremiumPrompt(
        "Premium themes"
      );
      return;
    }

    if (selectionLock.current) {
      return;
    }

    selectionLock.current = true;

    try {
      setThemeName(nextTheme);
      setThemePickerVisible(false);

      await updateSettings({
        themeName: nextTheme,
      });
    } finally {
      selectionLock.current = false;
    }
  }

  async function toggleDailyHopeReminder(
    value: boolean
  ) {
    if (!isPremium) {
      openPremiumPrompt(
        "Daily reminders"
      );
      return;
    }

    setDailyHopeReminderEnabled(value);

    if (!value) {
      await disableDailyHopeReminder();
      return;
    }

    const status =
      await enableDailyHopeReminder({
        hour: dailyHopeReminderHour,
        minute: dailyHopeReminderMinute,
      });

    if (status !== "granted") {
      setDailyHopeReminderEnabled(false);

      Alert.alert(
        "Notifications Off",
        status === "unsupported"
          ? "Daily reminders require a development build on Android. They are not available in Expo Go."
          : "Allow notifications in system settings to receive Daily Hope reminders."
      );
    }
  }

  function getHourForPicker(hour: number) {
    const hour12 = hour % 12;
    return hour12 === 0 ? 12 : hour12;
  }

  function getPeriodForPicker(hour: number) {
    return hour >= 12 ? "PM" : "AM";
  }

  function sanitizeTimeInput(
    value: string
  ) {
    return value
      .replace(/[^0-9]/g, "")
      .slice(0, 2);
  }

  function updatePickerHourInput(
    value: string
  ) {
    const sanitized = sanitizeTimeInput(value);

    setPickerHourInput(sanitized);
    setPickerHourSelection({
      start: sanitized.length,
      end: sanitized.length,
    });
  }

  function updatePickerMinuteInput(
    value: string
  ) {
    const sanitized = sanitizeTimeInput(value);

    setPickerMinuteInput(sanitized);
    setPickerMinuteSelection({
      start: sanitized.length,
      end: sanitized.length,
    });
  }

  function formatTimePart(value: number) {
    return String(value).padStart(2, "0");
  }

  function getPickerHour() {
    const parsed = Number.parseInt(
      pickerHourInput,
      10
    );

    if (Number.isNaN(parsed)) {
      return 12;
    }

    return Math.min(
      Math.max(parsed, 1),
      12
    );
  }

  function getPickerMinute() {
    const parsed = Number.parseInt(
      pickerMinuteInput,
      10
    );

    if (Number.isNaN(parsed)) {
      return 0;
    }

    return Math.min(
      Math.max(parsed, 0),
      59
    );
  }

  function normalizePickerInputs() {
    const hour = getPickerHour();
    const minute = getPickerMinute();

    setPickerHourInput(
      formatTimePart(hour)
    );

    setPickerMinuteInput(
      formatTimePart(minute)
    );

    return {
      hour,
      minute,
    };
  }

  function getHourFromPicker() {
    const pickerHour =
      getPickerHour();

    if (pickerPeriod === "AM") {
      return pickerHour === 12
        ? 0
        : pickerHour;
    }

    return pickerHour === 12
      ? 12
      : pickerHour + 12;
  }

  function openReminderTimePicker() {
    if (!isPremium) {
      openPremiumPrompt(
        "Reminder scheduling"
      );
      return;
    }

    setPickerHourInput(
      formatTimePart(
        getHourForPicker(
          dailyHopeReminderHour
        )
      )
    );

    setPickerMinuteInput(
      formatTimePart(
        dailyHopeReminderMinute
      )
    );

    setPickerPeriod(
      getPeriodForPicker(
        dailyHopeReminderHour
      )
    );

    setTimePickerVisible(true);
  }

  function adjustPickerHour(
    amount: number
  ) {
    const current = getPickerHour();
    const next =
      ((current - 1 + amount + 12) %
        12) +
      1;

    setPickerHourInput(
      formatTimePart(next)
    );
  }

  function adjustPickerMinute(
    amount: number
  ) {
    const current = getPickerMinute();
    const next =
      (current + amount + 60) % 60;

    setPickerMinuteInput(
      formatTimePart(next)
    );
  }

  async function saveReminderTime() {
    if (reminderSaveLock.current) {
      return;
    }

    reminderSaveLock.current = true;
    setIsReminderSaving(true);

    try {
      const normalized =
        normalizePickerInputs();

      const hour = getHourFromPicker();
      const minute = normalized.minute;

      setDailyHopeReminderHour(hour);
      setDailyHopeReminderMinute(minute);
      setTimePickerVisible(false);

      if (dailyHopeReminderEnabled) {
        const status =
          await updateDailyHopeReminderTime({
            hour,
            minute,
          });

        if (status !== "granted") {
          setDailyHopeReminderEnabled(false);

          Alert.alert(
            "Notifications Off",
            status === "unsupported"
              ? "Daily reminders require a development build on Android. They are not available in Expo Go."
              : "Allow notifications in system settings to receive Daily Hope reminders."
          );
        }

        return;
      }

      await updateSettings({
        dailyHopeReminderHour: hour,
        dailyHopeReminderMinute: minute,
      });
    } finally {
      reminderSaveLock.current = false;
      setIsReminderSaving(false);
    }
  }

  function finishRestoreOperation() {
    restoreLock.current = false;
    setIsRestoreProcessing(false);
  }

  function showRestoreConfirmation(
    backup: BackupData
  ) {
    Alert.alert(
      "Restore Backup",
      `Restore this backup?

Created:
${formatRelativeDate(
        backup.createdAt
      )}

Favorites:
${backup.favorites.length}

Your current favorites and settings will be replaced.`,
      [
        {
          text: "Cancel",
          style: "cancel",
          onPress: finishRestoreOperation,
        },
        {
          text: "Restore",
          style: "destructive",
          onPress: async () => {
            try {
              await restoreBackup(
                backup
              );

              await syncDailyHopeReminderSchedule();

              const settings =
                await getSettings();

              setShowDrawButton(
                settings.showDrawButton
              );

              setEnableHaptics(
                settings.enableHaptics
              );

              setPreferredTranslation(
                settings.preferredTranslation
              );

              setDailyHopeMusicEnabled(
                settings
                  .dailyHopeMusicEnabled
              );

              setDailyHopeReminderEnabled(
                settings
                  .dailyHopeReminderEnabled
              );

              setDailyHopeReminderHour(
                settings
                  .dailyHopeReminderHour
              );

              setDailyHopeReminderMinute(
                settings
                  .dailyHopeReminderMinute
              );

              setThemeName(
                settings.themeName
              );

              await success();

              setSuccessNotice({
                title: "Restore Complete",
                message:
                  "Your favorites and settings are restored and ready to use.",
                icon: "checkmark-circle-outline",
              });
            } catch (error) {
              console.error(error);

              Alert.alert(
                "Restore Failed",
                "Unable to restore the backup."
              );
            } finally {
              finishRestoreOperation();
            }
          },
        },
      ],
      { cancelable: false }
    );
  }

  async function handleBackup() {
    if (!isPremium) {
      openPremiumPrompt(
        "Backup and restore"
      );
      return;
    }

    if (backupLock.current) {
      return;
    }

    backupLock.current = true;
    setIsBackupProcessing(true);

    try {
      const { info } =
        await exportBackup();

      setBackupInfo(info);

      await success();

      setSuccessNotice({
        title: "Backup Ready",
        message:
          "Your favorites and settings are safely backed up on this device.",
        icon: "cloud-done-outline",
      });
    } catch (error) {
      console.error(error);

      Alert.alert(
        "Backup Failed",
        "Unable to create the backup."
      );
    } finally {
      backupLock.current = false;
      setIsBackupProcessing(false);
    }
  }

  async function handleRestore() {
    if (!isPremium) {
      openPremiumPrompt(
        "Backup and restore"
      );
      return;
    }

    if (restoreLock.current) {
      return;
    }

    restoreLock.current = true;
    setIsRestoreProcessing(true);

    try {
      const localBackup =
        await loadLatestLocalBackup();

      const backup =
        localBackup ??
        await loadBackup();

      if (!backup) {
        finishRestoreOperation();
        return;
      }

      showRestoreConfirmation(
        backup
      );
    } catch (error) {
      console.error(error);

      Alert.alert(
        "Restore Failed",
        error instanceof Error
          ? error.message
          : "Unable to restore the selected backup."
      );
      finishRestoreOperation();
    }
  }

  return (
    <>
      <ScrollView
        style={[
          styles.container,
          {
            backgroundColor:
              theme.background,
          },
        ]}
        contentContainerStyle={
          styles.contentContainer
        }
        showsVerticalScrollIndicator={
          false
        }
      >
      <View style={styles.header}>
        <Text
          style={[
            styles.subtitle,
            {
              color:
                theme.textSecondary,
            },
          ]}
        >
          Customize your Hope Cards
          experience.
        </Text>
      </View>

      <View style={styles.sectionHeader}>
        <View
          style={[
            styles.sectionIcon,
            {
              backgroundColor:
                theme.accent,
              shadowColor:
                theme.shadow,
            },
          ]}
        >
          <Ionicons
            name="sparkles-outline"
            size={18}
            color={theme.white}
          />
        </View>

        <Text
          style={[
            styles.sectionLabel,
            { color: theme.accent },
          ]}
        >
          Appearance
        </Text>

        <View
          style={[
            styles.sectionLine,
            {
              backgroundColor:
                theme.accentLine,
            },
          ]}
        />
      </View>

      <Pressable
        style={styles.settingRow}
        onPress={chooseTheme}
        android_ripple={{
          color: theme.accentSoft,
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Theme
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            {selectedTheme.label}
          </Text>
        </View>

        <View style={styles.themePreview}>
          <View style={styles.themePalettePreview}>
            {[
              selectedTheme.background,
              selectedTheme.cardBack,
              selectedTheme.accent,
            ].map((color, index) => (
              <View
                key={`${selectedTheme.name}-${color}-${index}`}
                style={[
                  styles.themePalettePreviewDot,
                  index > 0 &&
                    styles.themePalettePreviewOverlap,
                  {
                    backgroundColor: color,
                    borderColor:
                      selectedTheme.divider,
                  },
                ]}
              />
            ))}
          </View>

          <Ionicons
            name="chevron-forward"
            size={22}
            color={theme.textTertiary}
          />
        </View>
      </Pressable>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <View style={styles.settingRow}>
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Draw Button
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            Show a button for drawing cards,
            or tap the deck instead.
          </Text>
        </View>

        <Switch
          value={showDrawButton}
          onValueChange={toggleDrawButton}
          trackColor={{
            false: theme.switchOff,
            true: theme.accent,
          }}
          thumbColor={theme.white}
        />
      </View>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <Pressable
        style={styles.settingRow}
        onPress={() => {
          setTranslationPickerVisible(true);
        }}
        android_ripple={{
          color: theme.accentSoft,
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Bible Translation
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              { color: theme.textSecondary },
            ]}
            numberOfLines={1}
          >
            {selectedTranslation.label} · {selectedTranslation.name}
          </Text>
        </View>

        <Ionicons
          name="chevron-forward"
          size={22}
          color={theme.textTertiary}
        />
      </Pressable>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <View
        style={[
          styles.sectionHeader,
          { marginTop: 30 },
        ]}
      >
        <View
          style={[
            styles.sectionIcon,
            {
              backgroundColor:
                theme.accent,
              shadowColor:
                theme.shadow,
            },
          ]}
        >
          <Ionicons
            name="notifications-outline"
            size={18}
            color={theme.white}
          />
        </View>

        <Text
          style={[
            styles.sectionLabel,
            { color: theme.accent },
          ]}
        >
          Daily Hope
        </Text>

        <View
          style={[
            styles.sectionLine,
            {
              backgroundColor:
                theme.accentLine,
            },
          ]}
        />
      </View>

      <Pressable
        style={styles.settingRow}
        onPress={() => {
          if (!isPremium) {
            openPremiumPrompt(
              "Background music"
            );
          }
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Background Music
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            {isPremium
              ? "Play peaceful music while reading Daily Hope."
              : "Premium: play peaceful music with Daily Hope."}
          </Text>
        </View>

        <Switch
          value={
            isPremium &&
            dailyHopeMusicEnabled
          }
          onValueChange={
            toggleDailyHopeMusic
          }
          disabled={!isPremium}
          trackColor={{
            false: theme.switchOff,
            true: theme.accent,
          }}
          thumbColor={theme.white}
        />
      </Pressable>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <Pressable
        style={styles.settingRow}
        onPress={() => {
          if (!isPremium) {
            openPremiumPrompt(
              "Daily reminders"
            );
          }
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Daily Reminder
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            {isPremium
              ? "Receive a gentle reminder to open today's hope."
              : "Premium: receive daily verse reminders."}
          </Text>
        </View>

        <Switch
          value={
            isPremium &&
            dailyHopeReminderEnabled
          }
          onValueChange={
            toggleDailyHopeReminder
          }
          disabled={!isPremium}
          trackColor={{
            false: theme.switchOff,
            true: theme.accent,
          }}
          thumbColor={theme.white}
        />
      </Pressable>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <Pressable
        style={styles.settingRow}
        onPress={openReminderTimePicker}
        android_ripple={{
          color: theme.accentSoft,
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Reminder Time
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            {formatReminderTime(
              dailyHopeReminderHour,
              dailyHopeReminderMinute
            )}
            {!isPremium
              ? " • Premium"
              : ""}
          </Text>
        </View>

        <Ionicons
          name={
            isPremium
              ? "chevron-forward"
              : "lock-closed-outline"
          }
          size={22}
          color={theme.textTertiary}
        />
      </Pressable>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <View
        style={[
          styles.sectionHeader,
          { marginTop: 30 },
        ]}
      >
        <View
          style={[
            styles.sectionIcon,
            {
              backgroundColor:
                theme.accent,
              shadowColor:
                theme.shadow,
            },
          ]}
        >
          <Ionicons
            name="hand-left-outline"
            size={18}
            color={theme.white}
          />
        </View>

        <Text
          style={[
            styles.sectionLabel,
            { color: theme.accent },
          ]}
        >
          Interaction
        </Text>

        <View
          style={[
            styles.sectionLine,
            {
              backgroundColor:
                theme.accentLine,
            },
          ]}
        />
      </View>

      <View style={styles.settingRow}>
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Haptic Feedback
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            Vibrate slightly when drawing
            cards and saving favorites.
          </Text>
        </View>

        <Switch
          value={enableHaptics}
          onValueChange={toggleHaptics}
          trackColor={{
            false: theme.switchOff,
            true: theme.accent,
          }}
          thumbColor={theme.white}
        />
      </View>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <View
        style={[
          styles.sectionHeader,
          { marginTop: 30 },
        ]}
      >
        <View
          style={[
            styles.sectionIcon,
            {
              backgroundColor:
                theme.accent,
              shadowColor:
                theme.shadow,
            },
          ]}
        >
          <Ionicons
            name="cloud-upload-outline"
            size={18}
            color={theme.white}
          />
        </View>

        <Text
          style={[
            styles.sectionLabel,
            { color: theme.accent },
          ]}
        >
          Backup & Restore
        </Text>

        <View
          style={[
            styles.sectionLine,
            {
              backgroundColor:
                theme.accentLine,
            },
          ]}
        />
      </View>

      <Pressable
        style={styles.settingRow}
        onPress={handleBackup}
        accessibilityRole="button"
        accessibilityLabel="Back up favorites and settings"
        disabled={isBackupProcessing}
        accessibilityState={{
          disabled: isBackupProcessing,
          busy: isBackupProcessing,
        }}
        android_ripple={{
          color: theme.accentSoft,
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Backup Data
          </Text>

          {backupInfo ? (
            <>
              <Text
                style={[
                  styles.settingSubtitle,
                  {
                    color:
                      theme.textSecondary,
                  },
                ]}
              >
                Last backup
              </Text>

              <Text
                style={[
                  styles.backupDate,
                  { color: theme.text },
                ]}
              >
                {formatRelativeDate(
                  backupInfo.createdAt
                )}
              </Text>

              <Text
                style={[
                  styles.backupCount,
                  {
                    color:
                      theme.textTertiary,
                  },
                ]}
              >
                {backupInfo.favoriteCount}{" "}
                favorite
                {backupInfo.favoriteCount === 1
                  ? ""
                  : "s"}{" "}
                backed up
              </Text>
            </>
          ) : (
            <Text
              style={[
                styles.settingSubtitle,
                {
                  color:
                    theme.textSecondary,
                },
              ]}
            >
              {isPremium
                ? "No backups created yet."
                : "Premium: create a backup file."}
            </Text>
          )}
        </View>

        <Ionicons
          name={
            isPremium
              ? "chevron-forward"
              : "lock-closed-outline"
          }
          size={22}
          color={theme.textTertiary}
        />
      </Pressable>

      <View
        style={[
          styles.divider,
          {
            backgroundColor:
              theme.divider,
          },
        ]}
      />

      <Pressable
        style={styles.settingRow}
        onPress={handleRestore}
        accessibilityRole="button"
        accessibilityLabel="Restore favorites and settings"
        disabled={isRestoreProcessing}
        accessibilityState={{
          disabled: isRestoreProcessing,
          busy: isRestoreProcessing,
        }}
        android_ripple={{
          color: theme.accentSoft,
        }}
      >
        <View style={styles.textContainer}>
          <Text
            style={[
              styles.settingTitle,
              { color: theme.text },
            ]}
          >
            Restore Data
          </Text>

          <Text
            style={[
              styles.settingSubtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            {isPremium
              ? "Restore your data from a backup file."
              : "Premium: restore favorites and settings."}
          </Text>
        </View>

        <Ionicons
          name={
            isPremium
              ? "chevron-forward"
              : "lock-closed-outline"
          }
          size={22}
          color={theme.textTertiary}
        />
      </Pressable>

      </ScrollView>

      <Modal
        transparent
        visible={translationPickerVisible}
        animationType={
          reduceMotion ? "none" : "fade"
        }
        onRequestClose={() => {
          setTranslationPickerVisible(false);
        }}
      >
        <View
          accessibilityViewIsModal
          style={styles.modalBackdrop}
        >
          <Pressable
            accessible={false}
            style={StyleSheet.absoluteFill}
            onPress={() => {
              setTranslationPickerVisible(false);
            }}
          />
          <Animated.View
            entering={FadeInUp.duration(180).reduceMotion(
              ReduceMotion.System
            )}
            style={[
              styles.themePickerCard,
              {
                backgroundColor:
                  theme.surface,
              },
            ]}
          >
            <View style={styles.modalHeader}>
              <View style={styles.themeModalCopy}>
                <Text
                  ref={translationTitleRef}
                  accessible
                  accessibilityRole="header"
                  style={[
                    styles.modalTitle,
                    { color: theme.text },
                  ]}
                >
                  Bible Translation
                </Text>

                <Text
                  style={[
                    styles.modalSubtitle,
                    { color: theme.textSecondary },
                  ]}
                >
                  Select your preferred Bible translation.
                </Text>
              </View>

              <SubtlePressable
                accessibilityRole="button"
                accessibilityLabel="Close Bible translation selector"
                style={[
                  styles.iconButton,
                  {
                    backgroundColor:
                      theme.accentSoft,
                  },
                ]}
                onPress={() => {
                  setTranslationPickerVisible(false);
                }}
                hitSlop={8}
              >
                <Ionicons
                  name="close"
                  size={22}
                  color={theme.text}
                />
              </SubtlePressable>
            </View>

            <ScrollView
              style={styles.themeOptionScroller}
              contentContainerStyle={[
                styles.themeOptionList,
                styles.translationOptionList,
              ]}
              showsVerticalScrollIndicator={false}
            >
              {TRANSLATION_OPTIONS.map((option) => {
                const selected =
                  preferredTranslation === option.id;

                return (
                  <SubtlePressable
                    key={option.id}
                    accessibilityRole="radio"
                    accessibilityState={{ selected }}
                    onPress={() =>
                      selectTranslation(option.id)
                    }
                    style={[
                      styles.themeOptionCard,
                      styles.translationOptionCard,
                      {
                        backgroundColor:
                          selected
                            ? theme.accentSoft
                            : theme.background,
                        borderColor:
                          selected
                            ? theme.accent
                            : theme.divider,
                      },
                    ]}
                  >
                    <View style={styles.themeOptionHeader}>
                      <View style={styles.themeOptionText}>
                        <Text
                          style={[
                            styles.themeOptionTitle,
                            styles.translationOptionTitle,
                            { color: theme.text },
                          ]}
                        >
                          {option.label}
                        </Text>

                        <Text
                          style={[
                            styles.themeOptionDescription,
                            styles.translationOptionDescription,
                            { color: theme.textSecondary },
                          ]}
                        >
                          {option.name}
                        </Text>
                      </View>

                      <Ionicons
                        name={selected
                          ? "radio-button-on"
                          : "radio-button-off"}
                        size={24}
                        color={selected
                          ? theme.accent
                          : theme.textTertiary}
                      />
                    </View>
                  </SubtlePressable>
                );
              })}
            </ScrollView>
          </Animated.View>
        </View>
      </Modal>

      <Modal
        transparent
        visible={themePickerVisible}
        animationType={
          reduceMotion ? "none" : "fade"
        }
        onRequestClose={() => {
          setThemePickerVisible(false);
        }}
      >
        <View
          accessibilityViewIsModal
          style={styles.modalBackdrop}
        >
          <Pressable
            accessible={false}
            style={StyleSheet.absoluteFill}
            onPress={() => {
              setThemePickerVisible(false);
            }}
          />
          <Animated.View
            entering={FadeInUp.duration(180).reduceMotion(
              ReduceMotion.System
            )}
            style={[
              styles.themePickerCard,
              {
                backgroundColor:
                  theme.surface,
              },
            ]}
          >
            <View style={styles.modalHeader}>
              <View style={styles.themeModalCopy}>
                <Text
                  ref={themeTitleRef}
                  accessible
                  accessibilityRole="header"
                  style={[
                    styles.modalTitle,
                    { color: theme.text },
                  ]}
                >
                  Theme
                </Text>

                <Text
                  style={[
                    styles.modalSubtitle,
                    {
                      color:
                        theme.textSecondary,
                    },
                  ]}
                >
                  Choose a color scheme.
                </Text>
              </View>

              <SubtlePressable
                accessibilityRole="button"
                accessibilityLabel="Close theme selector"
                style={[
                  styles.iconButton,
                  {
                    backgroundColor:
                      theme.accentSoft,
                  },
                ]}
                onPress={() => {
                  setThemePickerVisible(false);
                }}
                hitSlop={8}
              >
                <Ionicons
                  name="close"
                  size={22}
                  color={theme.text}
                />
              </SubtlePressable>
            </View>

            <ScrollView
              style={styles.themeOptionScroller}
              contentContainerStyle={
                styles.themeOptionList
              }
              showsVerticalScrollIndicator={false}
            >
              {THEME_OPTIONS.map((option) => {
                const selected =
                  option.name === themeName;
                const locked =
                  isPremiumTheme(
                    option.name
                  ) && !isPremium;

                return (
                  <SubtlePressable
                    key={option.name}
                    accessibilityRole="radio"
                    accessibilityLabel={`${option.label} theme${
                      locked ? ", Premium" : ""
                    }`}
                    accessibilityState={{
                      selected,
                    }}
                    style={[
                      styles.themeOptionCard,
                      {
                        backgroundColor:
                          option.background,
                        borderColor: selected
                          ? option.accent
                          : option.divider,
                        opacity: locked
                          ? 0.72
                          : 1,
                      },
                    ]}
                    onPress={() =>
                      selectTheme(option.name)
                    }
                  >
                    <View
                      style={
                        styles.themeOptionHeader
                      }
                    >
                      <View
                        style={
                          styles.themePalette
                        }
                      >
                        {[
                          option.text,
                          option.accent,
                          option.cardBack,
                        ].map((color, index) => (
                          <View
                            key={`${option.name}-${color}-${index}`}
                            style={[
                              styles.themePaletteDot,
                              {
                                backgroundColor:
                                  color,
                              },
                            ]}
                          />
                        ))}
                      </View>

                      <View
                        style={
                          styles.themeOptionText
                        }
                      >
                        <View
                          style={
                            styles.themeOptionTitleRow
                          }
                        >
                          <Text
                            style={[
                              styles.themeOptionTitle,
                              {
                                color:
                                  option.text,
                              },
                            ]}
                          >
                            {option.label}
                          </Text>

                          {locked ? (
                            <View
                              style={[
                                styles.themePremiumBadge,
                                {
                                  backgroundColor:
                                    option.accentSoft,
                                },
                              ]}
                            >
                              <Text
                                style={[
                                  styles.themePremiumLabel,
                                  {
                                    color:
                                      option.accent,
                                  },
                                ]}
                              >
                                Premium
                              </Text>
                            </View>
                          ) : null}
                        </View>

                        <Text
                          style={[
                            styles.themeOptionDescription,
                            {
                              color:
                                option.textSecondary,
                            },
                          ]}
                        >
                          {option.description}
                        </Text>
                      </View>
                    </View>

                    <View
                      style={[
                        styles.themeCheck,
                        {
                          borderColor:
                            selected || locked
                              ? option.accent
                              : option.divider,
                          backgroundColor:
                            selected
                              ? option.accent
                              : option.surface,
                        },
                      ]}
                    >
                      {locked ? (
                        <Ionicons
                          name="lock-closed-outline"
                          size={14}
                          color={option.accent}
                        />
                      ) : selected ? (
                        <Ionicons
                          name="checkmark"
                          size={16}
                          color={option.white}
                        />
                      ) : null}
                    </View>
                  </SubtlePressable>
                );
              })}
            </ScrollView>
          </Animated.View>
        </View>
      </Modal>

      <Modal
        transparent
        visible={timePickerVisible}
        animationType={
          reduceMotion ? "none" : "fade"
        }
        onRequestClose={() => {
          setTimePickerVisible(false);
        }}
      >
        <View
          accessibilityViewIsModal
          style={styles.modalBackdrop}
        >
          <Pressable
            accessible={false}
            style={StyleSheet.absoluteFill}
            onPress={() => {
              if (!isReminderSaving) {
                setTimePickerVisible(false);
              }
            }}
          />
          <Animated.View
            entering={FadeInUp.duration(180).reduceMotion(
              ReduceMotion.System
            )}
            style={[
              styles.timePickerCard,
              {
                backgroundColor:
                  theme.surface,
              },
            ]}
          >
            <View style={styles.modalHeader}>
              <Text
                ref={timeTitleRef}
                accessible
                accessibilityRole="header"
                style={[
                  styles.modalTitle,
                  { color: theme.text },
                ]}
              >
                Reminder Time
              </Text>

              <SubtlePressable
                accessibilityRole="button"
                accessibilityLabel="Close reminder time selector"
                style={[
                  styles.iconButton,
                  {
                    backgroundColor:
                      theme.accentSoft,
                  },
                ]}
                onPress={() => {
                  setTimePickerVisible(false);
                }}
                hitSlop={8}
              >
                <Ionicons
                  name="close"
                  size={22}
                  color={theme.text}
                />
              </SubtlePressable>
            </View>

            <ScrollView
              contentContainerStyle={
                styles.timePickerScrollContent
              }
              keyboardShouldPersistTaps="handled"
              showsVerticalScrollIndicator={false}
            >
            <View style={styles.timePickerBody}>
              <View style={styles.timeUnit}>
                <SubtlePressable
                  accessibilityRole="button"
                  accessibilityLabel="Increase reminder hour"
                  style={styles.stepButton}
                  onPress={() =>
                    adjustPickerHour(1)
                  }
                >
                  <Ionicons
                    name="chevron-up"
                    size={24}
                    color={theme.accent}
                  />
                </SubtlePressable>

                <TextInput
                  accessibilityLabel="Reminder hour"
                  value={pickerHourInput}
                  onChangeText={
                    updatePickerHourInput
                  }
                  onFocus={() => {
                    setPickerHourSelection({
                      start: 0,
                      end: pickerHourInput.length,
                    });
                  }}
                  onBlur={() => {
                    setPickerHourSelection(
                      undefined
                    );
                    normalizePickerInputs();
                  }}
                  keyboardType="number-pad"
                  maxLength={2}
                  selection={
                    pickerHourSelection
                  }
                  style={[
                    styles.timeInput,
                    {
                      color: theme.text,
                      backgroundColor:
                        theme.background,
                    },
                  ]}
                />

                <SubtlePressable
                  accessibilityRole="button"
                  accessibilityLabel="Decrease reminder hour"
                  style={styles.stepButton}
                  onPress={() =>
                    adjustPickerHour(-1)
                  }
                >
                  <Ionicons
                    name="chevron-down"
                    size={24}
                    color={theme.accent}
                  />
                </SubtlePressable>
              </View>

              <Text
                style={[
                  styles.timeColon,
                  { color: theme.accent },
                ]}
              >
                :
              </Text>

              <View style={styles.timeUnit}>
                <SubtlePressable
                  accessibilityRole="button"
                  accessibilityLabel="Increase reminder minute"
                  style={styles.stepButton}
                  onPress={() =>
                    adjustPickerMinute(1)
                  }
                >
                  <Ionicons
                    name="chevron-up"
                    size={24}
                    color={theme.accent}
                  />
                </SubtlePressable>

                <TextInput
                  accessibilityLabel="Reminder minute"
                  value={pickerMinuteInput}
                  onChangeText={
                    updatePickerMinuteInput
                  }
                  onFocus={() => {
                    setPickerMinuteSelection({
                      start: 0,
                      end: pickerMinuteInput.length,
                    });
                  }}
                  onBlur={() => {
                    setPickerMinuteSelection(
                      undefined
                    );
                    normalizePickerInputs();
                  }}
                  keyboardType="number-pad"
                  maxLength={2}
                  selection={
                    pickerMinuteSelection
                  }
                  style={[
                    styles.timeInput,
                    {
                      color: theme.text,
                      backgroundColor:
                        theme.background,
                    },
                  ]}
                />

                <SubtlePressable
                  accessibilityRole="button"
                  accessibilityLabel="Decrease reminder minute"
                  style={styles.stepButton}
                  onPress={() =>
                    adjustPickerMinute(-1)
                  }
                >
                  <Ionicons
                    name="chevron-down"
                    size={24}
                    color={theme.accent}
                  />
                </SubtlePressable>
              </View>

              <View style={styles.periodControl}>
                {(["AM", "PM"] as const).map(
                  (period) => (
                    <SubtlePressable
                      key={period}
                      accessibilityRole="radio"
                      accessibilityLabel={period}
                      accessibilityState={{
                        selected:
                          pickerPeriod === period,
                      }}
                      style={[
                        styles.periodButton,
                        {
                          backgroundColor:
                            theme.background,
                        },
                        pickerPeriod ===
                          period &&
                          {
                            backgroundColor:
                              theme.text,
                          },
                      ]}
                      onPress={() => {
                        setPickerPeriod(
                          period
                        );
                      }}
                    >
                      <Text
                        style={[
                          styles.periodText,
                          {
                            color:
                              theme.cardMuted,
                          },
                          pickerPeriod ===
                            period &&
                            {
                              color:
                                theme.white,
                            },
                        ]}
                      >
                        {period}
                      </Text>
                    </SubtlePressable>
                  )
                )}
              </View>
            </View>

            <View style={styles.modalActions}>
              <SubtlePressable
                accessibilityRole="button"
                accessibilityLabel="Cancel reminder time changes"
                style={[
                  styles.secondaryButton,
                  {
                    backgroundColor:
                      theme.background,
                  },
                ]}
                onPress={() => {
                  setTimePickerVisible(false);
                }}
              >
                <Text
                  style={[
                    styles.secondaryButtonText,
                    { color: theme.text },
                  ]}
                >
                  Cancel
                </Text>
              </SubtlePressable>

              <SubtlePressable
                accessibilityRole="button"
                accessibilityLabel="Save reminder time"
                accessibilityState={{
                  disabled: isReminderSaving,
                  busy: isReminderSaving,
                }}
                disabled={isReminderSaving}
                style={[
                  styles.primaryButton,
                  {
                    backgroundColor:
                      theme.accent,
                  },
                ]}
                onPress={saveReminderTime}
              >
                <Text
                  style={styles.primaryButtonText}
                >
                  Save
                </Text>
              </SubtlePressable>
            </View>
            </ScrollView>
          </Animated.View>
        </View>
      </Modal>

      <PremiumNoticeModal
        visible={premiumPrompt !== null}
        title="Premium Subscription"
        message={getPremiumPromptMessage(
          premiumPrompt
        )}
        icon="lock-closed-outline"
        secondaryLabel="Not Now"
        onSecondary={() => {
          setPremiumPrompt(null);
        }}
        primaryLabel="View Subscription"
        onPrimary={() => {
          setPremiumPrompt(null);
          router.push("/premium");
        }}
      />

      <StatusNoticeModal
        visible={successNotice !== null}
        title={successNotice?.title ?? ""}
        message={successNotice?.message ?? ""}
        icon={
          successNotice?.icon ??
          "checkmark-circle-outline"
        }
        onDismiss={() => {
          setSuccessNotice(null);
        }}
      />
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8F6F2",
  },

  contentContainer: {
    padding: 24,
    paddingBottom: 40,
  },

  header: {
    marginBottom: 28,
  },

  subtitle: {
    fontSize: 16,
    lineHeight: 28,
    color: "#777777",
  },

  sectionHeader: {
    flexDirection: "row",
    alignItems: "center",
    marginBottom: 10,
  },

  sectionLabel: {
    marginLeft: 14,
    fontSize: 15,
    fontWeight: "600",
    letterSpacing: 1.2,
    textTransform: "uppercase",
    color: "#C89B3C",
  },

  sectionLine: {
    flex: 1,
    height: 1,
    backgroundColor: "#E7D7B2",
    marginLeft: 18,
  },

  section: {
    fontSize: 14,
    fontWeight: "700",
    color: "#C5A24C",
    letterSpacing: 1.5,
    textTransform: "uppercase",
    marginBottom: 18,
  },

  sectionIcon: {
    width: 42,
    height: 42,
    borderRadius: 21,

    backgroundColor: "#D2A63A",

    justifyContent: "center",
    alignItems: "center",

    shadowColor: "#000",
    shadowOpacity: 0.08,
    shadowRadius: 8,
    shadowOffset: {
      width: 0,
      height: 3,
    },

    elevation: 3,
  },

  settingRow: {
    flexDirection: "row",
    alignItems: "center",
    minHeight: 70,
    marginLeft: 56,
    paddingVertical: 14,
  },

  divider: {
    height: 1,
    backgroundColor: "#E7E2D8",
    marginLeft: 56,
  },

  textContainer: {
    flex: 1,
    marginRight: 18,
  },

  settingTitle: {
    fontSize: 18,
    fontWeight: "600",
    color: "#1A2747",
  },

  settingSubtitle: {
    marginTop: 6,
    fontSize: 14,
    lineHeight: 20,
    color: "#777777",
  },

  backupDate: {
    marginTop: 4,
    fontSize: 15,
    fontWeight: "600",
    color: "#273043",
  },

  backupCount: {
    marginTop: 4,
    fontSize: 13,
    color: "#8C93A3",
  },

  themePreview: {
    flexDirection: "row",
    alignItems: "center",
    gap: 10,
  },

  themePalettePreview: {
    flexDirection: "row",
    alignItems: "center",
    paddingLeft: 4,
  },

  themePalettePreviewDot: {
    width: 24,
    height: 24,
    borderRadius: 12,
    borderWidth: 2,
  },

  themePalettePreviewOverlap: {
    marginLeft: -7,
  },

  themePickerCard: {
    width: "100%",
    maxHeight: "86%",
    borderRadius: 8,
    padding: 18,
  },

  themeModalCopy: {
    flex: 1,
    marginRight: 18,
  },

  modalSubtitle: {
    marginTop: 6,
    fontSize: 16,
    lineHeight: 22,
  },

  themeOptionScroller: {
    marginTop: 6,
  },

  themeOptionList: {
    gap: 10,
    paddingBottom: 4,
  },

  translationOptionList: {
    gap: 7,
  },

  themeOptionCard: {
    borderWidth: 1,
    borderRadius: 8,
    paddingVertical: 14,
    paddingHorizontal: 14,
  },

  translationOptionCard: {
    minHeight: 62,
    paddingVertical: 10,
  },

  themeOptionHeader: {
    flexDirection: "row",
    alignItems: "center",
    gap: 14,
  },

  themePalette: {
    flexDirection: "row",
    alignItems: "center",
  },

  themePaletteDot: {
    width: 24,
    height: 24,
    borderRadius: 12,
    marginRight: -6,
    borderWidth: 2,
    borderColor: "#FFFFFF",
  },

  themeCheck: {
    width: 28,
    height: 28,
    borderRadius: 14,
    borderWidth: 1,
    alignItems: "center",
    justifyContent: "center",
    marginLeft: "auto",
  },

  themeOptionText: {
    flex: 1,
    minWidth: 0,
  },

  themeOptionTitleRow: {
    flexDirection: "row",
    alignItems: "center",
    gap: 8,
  },

  themeOptionTitle: {
    flexShrink: 1,
    fontSize: 17,
    fontWeight: "700",
  },

  translationOptionTitle: {
    fontSize: 16,
  },

  themePremiumBadge: {
    borderRadius: 999,
    paddingVertical: 3,
    paddingHorizontal: 8,
  },

  themePremiumLabel: {
    fontSize: 12,
    fontWeight: "700",
  },

  themeOptionDescription: {
    marginTop: 3,
    fontSize: 14,
    lineHeight: 20,
  },

  translationOptionDescription: {
    marginTop: 2,
    fontSize: 13,
    lineHeight: 18,
  },

  modalBackdrop: {
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: 24,
    paddingVertical: 38,
    backgroundColor:
      "rgba(26, 39, 71, 0.42)",
  },

  timePickerCard: {
    maxHeight: "86%",
    borderRadius: 8,
    backgroundColor: "#FFFFFF",
    padding: 22,
  },

  timePickerScrollContent: {
    paddingBottom: 2,
  },

  modalHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 18,
  },

  modalTitle: {
    fontSize: 24,
    fontWeight: "700",
    color: "#1A2747",
  },

  iconButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#F8F6F2",
  },

  timePickerBody: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "center",
    gap: 10,
    paddingVertical: 10,
  },

  timeUnit: {
    alignItems: "center",
    gap: 8,
  },

  stepButton: {
    width: 48,
    height: 40,
    alignItems: "center",
    justifyContent: "center",
  },

  timeInput: {
    minWidth: 72,
    height: 62,
    textAlign: "center",
    fontSize: 42,
    fontWeight: "700",
    color: "#1A2747",
    fontVariant: ["tabular-nums"],
    borderRadius: 8,
    backgroundColor: "#F8F6F2",
    paddingHorizontal: 8,
  },

  timeColon: {
    marginTop: -2,
    fontSize: 38,
    fontWeight: "700",
    color: "#C89B3C",
  },

  periodControl: {
    width: 64,
    gap: 8,
  },

  periodButton: {
    height: 42,
    borderRadius: 8,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#F8F6F2",
  },

  periodText: {
    fontSize: 15,
    fontWeight: "700",
    color: "#7A8292",
  },

  modalActions: {
    flexDirection: "row",
    justifyContent: "flex-end",
    gap: 12,
    marginTop: 24,
  },

  secondaryButton: {
    minWidth: 92,
    height: 46,
    borderRadius: 8,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#F8F6F2",
  },

  secondaryButtonText: {
    fontSize: 16,
    fontWeight: "700",
    color: "#1A2747",
  },

  primaryButton: {
    minWidth: 92,
    height: 46,
    borderRadius: 8,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#C89B3C",
  },

  primaryButtonText: {
    fontSize: 16,
    fontWeight: "700",
    color: "#FFFFFF",
  },
});
