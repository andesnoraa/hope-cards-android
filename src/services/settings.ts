import AsyncStorage from "@react-native-async-storage/async-storage";

import type {
    AppThemeName,
} from "../theme/appTheme";
import {
    isTranslationId,
    type TranslationId,
} from "./verseService";

const SETTINGS_KEY = "hope_cards_settings";

export type AppSettings = {
    showDrawButton: boolean;
    enableHaptics: boolean;
    dailyHopeReminderEnabled: boolean;
    dailyHopeMusicEnabled: boolean;
    dailyHopeReminderHour: number;
    dailyHopeReminderMinute: number;
    themeName: AppThemeName;
    preferredTranslation: TranslationId;
};

export const DEFAULT_SETTINGS: AppSettings = {
    showDrawButton: true,
    enableHaptics: true,
    dailyHopeReminderEnabled: false,
    dailyHopeMusicEnabled: true,
    dailyHopeReminderHour: 8,
    dailyHopeReminderMinute: 0,
    themeName: "classic",
    preferredTranslation: "bsb",
};

export async function getSettings(): Promise<AppSettings> {
    try {
        const json = await AsyncStorage.getItem(
            SETTINGS_KEY
        );

        if (!json) {
            return DEFAULT_SETTINGS;
        }

        const stored = JSON.parse(json);

        return {
            ...DEFAULT_SETTINGS,
            ...stored,
            preferredTranslation:
                isTranslationId(
                    stored.preferredTranslation
                )
                    ? stored.preferredTranslation
                    : DEFAULT_SETTINGS
                        .preferredTranslation,
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
