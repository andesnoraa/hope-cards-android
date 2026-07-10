import AsyncStorage from "@react-native-async-storage/async-storage";

const SETTINGS_KEY = "hope_cards_settings";

export type AppSettings = {
    showDrawButton: boolean;
};

const DEFAULT_SETTINGS: AppSettings = {
    showDrawButton: true,
};

export async function getSettings(): Promise<AppSettings> {
    try {
        const json = await AsyncStorage.getItem(SETTINGS_KEY);

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

export async function setShowDrawButton(
    value: boolean
) {
    const settings = await getSettings();

    settings.showDrawButton = value;

    await saveSettings(settings);
}