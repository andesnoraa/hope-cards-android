import {
    createBackup,
    type BackupData,
} from "./backup";

import {
    replaceFavorites,
} from "./favorites";

import {
    DEFAULT_SETTINGS,
    replaceSettings,
} from "./settings";
import {
    replaceJournalEntries,
} from "./journal";

/**
 * Restores user data from a backup.
 */
export async function restoreBackup(
    backup: BackupData
): Promise<void> {
    const previous = await createBackup();

    try {
        await applyBackup(backup);
    } catch (error) {
        try {
            await applyBackup(previous);
        } catch (rollbackError) {
            console.error("Restore rollback failed:", rollbackError);
        }

        throw error;
    }
}

async function applyBackup(
    backup: BackupData
): Promise<void> {
    await replaceFavorites(backup.favorites);

    await replaceSettings({
        ...DEFAULT_SETTINGS,

        showDrawButton:
            backup.settings.showDrawButton,

        enableHaptics:
            backup.settings.enableHaptics,

        dailyHopeReminderEnabled:
            backup.settings
                .dailyHopeReminderEnabled ??
            DEFAULT_SETTINGS
                .dailyHopeReminderEnabled,

        dailyHopeMusicEnabled:
            backup.settings
                .dailyHopeMusicEnabled ??
            DEFAULT_SETTINGS
                .dailyHopeMusicEnabled,

        dailyHopeReminderHour:
            backup.settings
                .dailyHopeReminderHour ??
            DEFAULT_SETTINGS
                .dailyHopeReminderHour,

        dailyHopeReminderMinute:
            backup.settings
                .dailyHopeReminderMinute ??
            DEFAULT_SETTINGS
                .dailyHopeReminderMinute,

        themeName:
            backup.settings.themeName ??
            DEFAULT_SETTINGS.themeName,

        preferredTranslation:
            backup.settings
                .preferredTranslation,
    });

    replaceJournalEntries(backup.journalEntries ?? []);
}
