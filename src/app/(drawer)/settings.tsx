import Ionicons from "@expo/vector-icons/Ionicons";
import { useEffect, useState } from "react";

import {
  Alert,
  Pressable,
  StyleSheet,
  Switch,
  Text,
  View,
} from "react-native";

import {
  shareBackup,
} from "../../services/backup";

import {
  BackupInfo,
  getBackupInfo,
} from "../../services/backupInfo";

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

  return (
    <View style={styles.container}>
      <Text style={styles.section}>
        Appearance
      </Text>

      <View style={styles.settingCard}>
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

      <Text
        style={[
          styles.section,
          { marginTop: 32 },
        ]}
      >
        Backup & Restore
      </Text>

      <Pressable
        style={styles.settingCard}
        onPress={handleBackup}
        android_ripple={{
          color: "#F3E8C5",
        }}
      >
        <View style={styles.actionRow}>
          <Ionicons
            name="save-outline"
            size={24}
            color="#C5A24C"
            style={styles.actionIcon}
          />

          <View style={styles.textContainer}>
            <Text style={styles.settingTitle}>
              Backup Data
            </Text>

            {backupInfo ? (
              <>
                <Text
                  style={styles.settingSubtitle}
                >
                  Last backup
                </Text>

                <Text style={styles.backupDate}>
                  {formatRelativeDate(
                    backupInfo.createdAt
                  )}
                </Text>

                <Text
                  style={styles.backupCount}
                >
                  {
                    backupInfo.favoriteCount
                  }{" "}
                  favorite
                  {backupInfo.favoriteCount ===
                  1
                    ? ""
                    : "s"}{" "}
                  backed up
                </Text>
              </>
            ) : (
              <Text
                style={styles.settingSubtitle}
              >
                No backups created yet.
              </Text>
            )}
          </View>

          <Ionicons
            name="chevron-forward"
            size={22}
            color="#8C93A3"
          />
        </View>
      </Pressable>

      <Pressable
        style={styles.settingCard}
        onPress={() =>
          Alert.alert(
            "Coming Soon",
            "Restore will be available soon."
          )
        }
        android_ripple={{
          color: "#F3E8C5",
        }}
      >
        <View style={styles.actionRow}>
          <Ionicons
            name="folder-open-outline"
            size={24}
            color="#C5A24C"
            style={styles.actionIcon}
          />

          <View style={styles.textContainer}>
            <Text style={styles.settingTitle}>
              Restore Data
            </Text>

            <Text
              style={styles.settingSubtitle}
            >
              Restore your data from a
              backup file.
            </Text>
          </View>

          <Ionicons
            name="chevron-forward"
            size={22}
            color="#8C93A3"
          />
        </View>
      </Pressable>

      <Text
        style={[
          styles.section,
          { marginTop: 32 },
        ]}
      >
        Interaction
      </Text>

      <View style={styles.settingCard}>
        <View style={styles.textContainer}>
          <Text style={styles.settingTitle}>
            Haptic Feedback
          </Text>

          <Text
            style={styles.settingSubtitle}
          >
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
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8F6F2",
    padding: 20,
  },

  section: {
    fontSize: 14,
    fontWeight: "700",
    color: "#C5A24C",
    letterSpacing: 1.5,
    textTransform: "uppercase",
    marginBottom: 16,
  },

  settingCard: {
    backgroundColor: "#FFFFFF",

    borderRadius: 18,

    padding: 18,

    flexDirection: "row",

    justifyContent: "space-between",

    alignItems: "center",

    shadowColor: "#000",
    shadowOpacity: 0.05,
    shadowRadius: 8,
    shadowOffset: {
      width: 0,
      height: 2,
    },

    elevation: 2,

    marginBottom: 16,
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
    color: "#777",
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

  actionRow: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
  },

  actionIcon: {
    marginRight: 16,
  },
});