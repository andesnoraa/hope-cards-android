import type {
    BackupData,
} from "./backup";

import {
    replaceFavorites,
} from "./favorites";

import {
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
        showDrawButton:
            backup.settings.showDrawButton,

        enableHaptics:
            backup.settings.enableHaptics,
    });
}