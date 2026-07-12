import Ionicons from "@expo/vector-icons/Ionicons";
import { useEffect, useState } from "react";

import {
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Switch,
  Text,
  View,
} from "react-native";

import {
  loadBackup,
  shareBackup,
} from "../../services/backup";

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
  formatRelativeDate,
} from "../../utils/formatDate";

export default function SettingsScreen() {
  const [showDrawButton, setShowDrawButton] =
    useState(true);

  const [enableHaptics, setEnableHaptics] =
    useState(true);

  const [backupInfo, setBackupInfo] =
    useState<BackupInfo | null>(null);

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

  async function handleBackup() {
    try {
      await shareBackup();

      const info =
        await getBackupInfo();

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
    <ScrollView
      style={styles.container}
      contentContainerStyle={
        styles.contentContainer
      }
      showsVerticalScrollIndicator={
        false
      }
    >
      <View style={styles.header}>
        <Text style={styles.title}>
          Settings
        </Text>

        <Text style={styles.subtitle}>
          Customize your Hope Cards
          experience.
        </Text>
      </View>

      <View style={styles.sectionHeader}>
        <View style={styles.sectionIcon}>
          <Ionicons
            name="color-palette-outline"
            size={18}
            color="#FFFFFF"
          />
        </View>

        <Text style={styles.sectionLabel}>
          Appearance
        </Text>

        <View style={styles.sectionLine} />
      </View>

      <View style={styles.settingRow}>
        <View style={styles.settingIcon}>
          <Ionicons
            name="eye-outline"
            size={21}
            color="#C89B3C"
          />
        </View>

        <View style={styles.textContainer}>
          <Text style={styles.settingTitle}>
            Show "Draw a Card" Button
          </Text>

          <Text style={styles.settingSubtitle}>
            Hide the button and draw cards by
            tapping the deck.
          </Text>
        </View>

        <Switch
          value={showDrawButton}
          onValueChange={toggleDrawButton}
          trackColor={{
            false: "#D9D9D9",
            true: "#D4AF37",
          }}
          thumbColor="#FFFFFF"
        />
      </View>

      <View style={styles.divider} />

      <View
        style={[
          styles.sectionHeader,
          { marginTop: 36 },
        ]}
      >
        <View style={styles.sectionIcon}>
          <Ionicons
            name="cloud-upload-outline"
            size={18}
            color="#FFFFFF"
          />
        </View>

        <Text style={styles.sectionLabel}>
          Backup & Restore
        </Text>

        <View style={styles.sectionLine} />
      </View>
      <Pressable
        style={styles.settingRow}
        onPress={handleBackup}
        android_ripple={{
          color: "#F3E8C5",
        }}
      >
        <View style={styles.settingIcon}>
          <Ionicons
            name="save-outline"
            size={21}
            color="#C89B3C"
          />
        </View>

        <View style={styles.textContainer}>
          <Text style={styles.settingTitle}>
            Backup Data
          </Text>

          {backupInfo ? (
            <>
              <Text style={styles.settingSubtitle}>
                Last backup
              </Text>

              <Text style={styles.backupDate}>
                {formatRelativeDate(
                  backupInfo.createdAt
                )}
              </Text>

              <Text style={styles.backupCount}>
                {backupInfo.favoriteCount}{" "}
                favorite
                {backupInfo.favoriteCount === 1
                  ? ""
                  : "s"}{" "}
                backed up
              </Text>
            </>
          ) : (
            <Text style={styles.settingSubtitle}>
              No backups created yet.
            </Text>
          )}
        </View>

        <Ionicons
          name="chevron-forward"
          size={22}
          color="#8C93A3"
        />
      </Pressable>

      <View style={styles.divider} />

      <Pressable
        style={styles.settingRow}
        onPress={handleRestore}
        android_ripple={{
          color: "#F3E8C5",
        }}
      >
        <View style={styles.settingIcon}>
          <Ionicons
            name="folder-open-outline"
            size={21}
            color="#C89B3C"
          />
        </View>

        <View style={styles.textContainer}>
          <Text style={styles.settingTitle}>
            Restore Data
          </Text>

          <Text style={styles.settingSubtitle}>
            Restore your data from a backup
            file.
          </Text>
        </View>

        <Ionicons
          name="chevron-forward"
          size={22}
          color="#8C93A3"
        />
      </Pressable>

      <View style={styles.divider} />

      <View
        style={[
          styles.sectionHeader,
          { marginTop: 36 },
        ]}
      >
        <View style={styles.sectionIcon}>
          <Ionicons
            name="hand-left-outline"
            size={18}
            color="#FFFFFF"
          />
        </View>

        <Text style={styles.sectionLabel}>
          Interaction
        </Text>

        <View style={styles.sectionLine} />
      </View>

      <View style={styles.settingRow}>
        <View style={styles.settingIcon}>
          <Ionicons
            name="phone-portrait-outline"
            size={21}
            color="#C89B3C"
          />
        </View>

        <View style={styles.textContainer}>
          <Text style={styles.settingTitle}>
            Haptic Feedback
          </Text>

          <Text style={styles.settingSubtitle}>
            Vibrate slightly when drawing
            cards and saving favorites.
          </Text>
        </View>

        <Switch
          value={enableHaptics}
          onValueChange={toggleHaptics}
          trackColor={{
            false: "#D9D9D9",
            true: "#D4AF37",
          }}
          thumbColor="#FFFFFF"
        />
      </View>
    </ScrollView>
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
    marginBottom: 18,
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
    paddingVertical: 18,
  },

  divider: {
    height: 1,
    backgroundColor: "#E7E2D8",
    marginLeft: 0,
  },

  textContainer: {
    flex: 1,
    marginRight: 16,
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

  settingIcon: {
    width: 46,
    height: 46,
    borderRadius: 23,

    backgroundColor: "#FFFFFF",

    justifyContent: "center",
    alignItems: "center",

    marginRight: 20,

    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 10,
    shadowOffset: {
      width: 0,
      height: 4,
    },

    elevation: 3,
  },

  leadingIcon: {
    marginRight: 18,
  },
});