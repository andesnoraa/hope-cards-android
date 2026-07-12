import AsyncStorage from "@react-native-async-storage/async-storage";

const SETTINGS_KEY = "hope_cards_settings";

export type AppSettings = {
    showDrawButton: boolean;
    enableHaptics: boolean;
};

const DEFAULT_SETTINGS: AppSettings = {
    showDrawButton: true,
    enableHaptics: true,
};

export async function getSettings(): Promise<AppSettings> {
    try {
        const json = await AsyncStorage.getItem(
            SETTINGS_KEY
        );

        if (!json) {
            return DEFAULT_SETTINGS;
        }

        return {
            ...DEFAULT_SETTINGS,
            ...JSON.parse(json),
        };
    } catch {
        return DEFAULT_SETTINGS;
    }
}

export async function saveSettings(
    settings: AppSettings
) {
    await AsyncStorage.setItem(
        SETTINGS_KEY,
        JSON.stringify(settings)
    );
}

/**
 * Replaces all settings.
 * Used when restoring a backup.
 */
export async function replaceSettings(
    settings: AppSettings
): Promise<void> {
    await saveSettings(settings);
}

export async function updateSettings(
    updates: Partial<AppSettings>
) {
    const settings = await getSettings();

    const newSettings = {
        ...settings,
        ...updates,
    };

    await saveSettings(newSettings);

    return newSettings;
}