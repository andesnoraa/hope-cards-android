import type {
    BackupData,
} from "./backup";

import {
    replaceFavorites,
} from "./favorites";

import {
    DEFAULT_SETTINGS,
    replaceSettings,
} from "./settings";

/**
 * Restores user data from a backup.
 */
export async function restoreBackup(
    backup: BackupData
): Promise<void> {
    await replaceFavorites(
        backup.favorites
    );

    await replaceSettings({
        ...DEFAULT_SETTINGS,

        showDrawButton:
            backup.settings.showDrawButton,

        enableHaptics:
            backup.settings.enableHaptics,

        savedVersePatternEnabled:
            backup.settings
                .savedVersePatternEnabled ??
            DEFAULT_SETTINGS
                .savedVersePatternEnabled,

        themeName:
            backup.settings.themeName ??
            DEFAULT_SETTINGS.themeName,
    });
}
