import * as Sharing from "expo-sharing";

import {
    File,
    Paths,
} from "expo-file-system";

import {
    getFavorites,
} from "./favorites";

import {
    getSettings,
} from "./settings";

import {
    saveBackupInfo,
} from "./backupInfo";

export interface BackupData {
    version: 1;

    createdAt: string;

    favorites: string[];

    settings: {
        showDrawButton: boolean;
        enableHaptics: boolean;

        /**
         * Reserved for future versions.
         */
        preferredTranslation: string;
    };
}

/**
 * Creates a backup object containing
 * all user data that should be preserved.
 */
export async function createBackup(): Promise<BackupData> {
    const favorites =
        await getFavorites();

    const settings =
        await getSettings();

    return {
        version: 1,

        createdAt:
            new Date().toISOString(),

        favorites,

        settings: {
            ...settings,

            // Default for now.
            // This will become the user's
            // selected Bible translation later.
            preferredTranslation:
                "bbe",
        },
    };
}

/**
 * Creates a backup file in the
 * app's cache directory.
 */
export async function exportBackup(): Promise<File> {
    const backup =
        await createBackup();

    const json = JSON.stringify(
        backup,
        null,
        2
    );

    const today =
        new Date()
            .toISOString()
            .split("T")[0];

    const file = new File(
        Paths.cache,
        `hope-cards-backup-${today}.json`
    );

    if (!file.exists) {
        file.create();
    }

    file.write(json);

    await saveBackupInfo({
        createdAt:
            backup.createdAt,

        fileName:
            file.name,

        version:
            backup.version,

        favoriteCount:
            backup.favorites.length,
    });

    return file;
}

/**
 * Creates a backup file and opens
 * the native Share dialog.
 */
export async function shareBackup(): Promise<void> {
    const file =
        await exportBackup();

    const available =
        await Sharing.isAvailableAsync();

    if (!available) {
        throw new Error(
            "Sharing is not available on this device."
        );
    }

    await Sharing.shareAsync(
        file.uri,
        {
            mimeType:
                "application/json",

            dialogTitle:
                "Backup Hope Cards",

            UTI:
                "public.json",
        }
    );
}