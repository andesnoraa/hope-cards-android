import { useEffect, useState } from "react";
import {
  StyleSheet,
  Switch,
  Text,
  View,
} from "react-native";

import {
  getSettings,
  updateSettings,
} from "../../services/settings";

export default function SettingsScreen() {
  const [showDrawButton, setShowDrawButton] =
    useState(true);

  const [enableHaptics, setEnableHaptics] =
    useState(true);

  useEffect(() => {
    async function loadSettings() {
      const settings = await getSettings();

      setShowDrawButton(
        settings.showDrawButton
      );

      setEnableHaptics(
        settings.enableHaptics
      );
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
        Interaction
      </Text>

      <View style={styles.settingCard}>
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
});