import Ionicons from "@expo/vector-icons/Ionicons";
import { useEffect, useState } from "react";

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

import {
  loadBackup,
  shareBackup,
} from "../../services/backup";

import {
  disableDailyHopeReminder,
  enableDailyHopeReminder,
  formatReminderTime,
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
  getSettings,
  updateSettings,
} from "../../services/settings";

import {
  THEME_OPTIONS,
  useAppTheme,
  type AppThemeName,
} from "../../theme/appTheme";

import {
  formatRelativeDate,
} from "../../utils/formatDate";

export default function SettingsScreen() {
  const {
    theme,
    themeName,
    setThemeName,
  } = useAppTheme();

  const [showDrawButton, setShowDrawButton] =
    useState(true);

  const [enableHaptics, setEnableHaptics] =
    useState(true);

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
    pickerHourInput,
    setPickerHourInput,
  ] = useState("08");

  const [
    pickerMinuteInput,
    setPickerMinuteInput,
  ] = useState("00");

  const [
    pickerPeriod,
    setPickerPeriod,
  ] = useState<"AM" | "PM">("AM");

  const [backupInfo, setBackupInfo] =
    useState<BackupInfo | null>(null);

  const selectedTheme =
    THEME_OPTIONS.find(
      (option) =>
        option.name === themeName
    ) ?? THEME_OPTIONS[0];

  useEffect(() => {
    async function loadSettings() {
      const settings =
        await getSettings();

      setShowDrawButton(
        settings.showDrawButton
      );

      setEnableHaptics(
        settings.enableHaptics
      );

      setDailyHopeReminderEnabled(
        settings.dailyHopeReminderEnabled
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
    }

    loadSettings();
  }, []);

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

  function chooseTheme() {
    setThemePickerVisible(true);
  }

  async function selectTheme(
    nextTheme: AppThemeName
  ) {
    setThemeName(nextTheme);
    setThemePickerVisible(false);

    await updateSettings({
      themeName: nextTheme,
    });
  }

  async function toggleDailyHopeReminder(
    value: boolean
  ) {
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
  }

  async function handleBackup() {
    try {
      const info =
        await shareBackup();

      setBackupInfo(info);
    } catch (error) {
      console.error(error);

      Alert.alert(
        "Backup Failed",
        "Unable to create the backup."
      );
    }
  }
  async function handleRestore() {
    try {
      const backup =
        await loadBackup();

      if (!backup) {
        return;
      }

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
          },
          {
            text: "Restore",
            style: "destructive",
            onPress: async () => {
              try {
                await restoreBackup(
                  backup
                );

                const settings =
                  await getSettings();

                setShowDrawButton(
                  settings.showDrawButton
                );

                setEnableHaptics(
                  settings.enableHaptics
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

                Alert.alert(
                  "Restore Complete",
                  "Your backup has been restored successfully."
                );
              } catch (error) {
                console.error(
                  error
                );

                Alert.alert(
                  "Restore Failed",
                  "Unable to restore the backup."
                );
              }
            },
          },
        ]
      );
    } catch (error) {
      console.error(error);

      Alert.alert(
        "Restore Failed",
        error instanceof Error
          ? error.message
          : "Unable to restore the selected backup."
      );
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
            styles.title,
            { color: theme.text },
          ]}
        >
          Settings
        </Text>

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
                      theme.background,
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

      <View style={styles.settingRow}>
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
            Receive a gentle reminder to
            open today's hope.
          </Text>
        </View>

        <Switch
          value={dailyHopeReminderEnabled}
          onValueChange={
            toggleDailyHopeReminder
          }
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
              No backups created yet.
            </Text>
          )}
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

      <Pressable
        style={styles.settingRow}
        onPress={handleRestore}
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
            Restore your data from a backup
            file.
          </Text>
        </View>

        <Ionicons
          name="chevron-forward"
          size={22}
          color={theme.textTertiary}
        />
      </Pressable>

      </ScrollView>

      <Modal
        transparent
        visible={themePickerVisible}
        animationType="fade"
        onRequestClose={() => {
          setThemePickerVisible(false);
        }}
      >
        <View style={styles.modalBackdrop}>
          <View
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

              <Pressable
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
              </Pressable>
            </View>

            <View style={styles.themeOptionList}>
              {THEME_OPTIONS.map((option) => {
                const selected =
                  option.name === themeName;

                return (
                  <Pressable
                    key={option.name}
                    style={[
                      styles.themeOptionCard,
                      {
                        backgroundColor:
                          option.background,
                        borderColor: selected
                          ? option.accent
                          : option.divider,
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
                        style={[
                          styles.themeCheck,
                          {
                            borderColor:
                              selected
                                ? option.accent
                                : option.divider,
                            backgroundColor:
                              selected
                                ? option.accent
                                : option.surface,
                          },
                        ]}
                      >
                        {selected ? (
                          <Ionicons
                            name="checkmark"
                            size={16}
                            color={option.white}
                          />
                        ) : null}
                      </View>
                    </View>

                    <Text
                      style={[
                        styles.themeOptionTitle,
                        { color: option.text },
                      ]}
                    >
                      {option.label}
                    </Text>

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
                  </Pressable>
                );
              })}
            </View>
          </View>
        </View>
      </Modal>

      <Modal
        transparent
        visible={timePickerVisible}
        animationType="fade"
        onRequestClose={() => {
          setTimePickerVisible(false);
        }}
      >
        <View style={styles.modalBackdrop}>
          <View
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
                style={[
                  styles.modalTitle,
                  { color: theme.text },
                ]}
              >
                Reminder Time
              </Text>

              <Pressable
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
              </Pressable>
            </View>

            <View style={styles.timePickerBody}>
              <View style={styles.timeUnit}>
                <Pressable
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
                </Pressable>

                <TextInput
                  value={pickerHourInput}
                  onChangeText={(value) => {
                    setPickerHourInput(
                      sanitizeTimeInput(value)
                    );
                  }}
                  onBlur={normalizePickerInputs}
                  keyboardType="number-pad"
                  maxLength={2}
                  selectTextOnFocus
                  style={[
                    styles.timeInput,
                    {
                      color: theme.text,
                      backgroundColor:
                        theme.background,
                    },
                  ]}
                />

                <Pressable
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
                </Pressable>
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
                <Pressable
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
                </Pressable>

                <TextInput
                  value={pickerMinuteInput}
                  onChangeText={(value) => {
                    setPickerMinuteInput(
                      sanitizeTimeInput(value)
                    );
                  }}
                  onBlur={normalizePickerInputs}
                  keyboardType="number-pad"
                  maxLength={2}
                  selectTextOnFocus
                  style={[
                    styles.timeInput,
                    {
                      color: theme.text,
                      backgroundColor:
                        theme.background,
                    },
                  ]}
                />

                <Pressable
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
                </Pressable>
              </View>

              <View style={styles.periodControl}>
                {(["AM", "PM"] as const).map(
                  (period) => (
                    <Pressable
                      key={period}
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
                    </Pressable>
                  )
                )}
              </View>
            </View>

            <View style={styles.modalActions}>
              <Pressable
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
              </Pressable>

              <Pressable
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
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
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
    marginBottom: 36,
  },

  title: {
    fontSize: 34,
    fontWeight: "700",
    color: "#1A2747",
  },

  subtitle: {
    marginTop: 12,
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
    borderRadius: 8,
    padding: 22,
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

  themeOptionList: {
    gap: 12,
  },

  themeOptionCard: {
    borderWidth: 1,
    borderRadius: 8,
    padding: 16,
  },

  themeOptionHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginBottom: 12,
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
  },

  themeOptionTitle: {
    fontSize: 18,
    fontWeight: "700",
  },

  themeOptionDescription: {
    marginTop: 4,
    fontSize: 14,
    lineHeight: 20,
  },

  modalBackdrop: {
    flex: 1,
    justifyContent: "center",
    padding: 24,
    backgroundColor:
      "rgba(26, 39, 71, 0.42)",
  },

  timePickerCard: {
    borderRadius: 8,
    backgroundColor: "#FFFFFF",
    padding: 22,
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
