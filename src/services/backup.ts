import * as DocumentPicker from "expo-document-picker";
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
    type BackupInfo,
    saveBackupInfo,
} from "./backupInfo";

import type {
    AppThemeName,
} from "../theme/appTheme";

const THEME_NAMES: AppThemeName[] = [
    "classic",
    "serenity",
    "stillWater",
    "midnight",
];

export interface BackupData {
    version: 1;

    createdAt: string;

    favorites: string[];

    settings: {
        showDrawButton: boolean;
        enableHaptics: boolean;
        themeName?: AppThemeName;

        /**
         * Reserved for future versions.
         */
        preferredTranslation: string;
    };
}

type BackupExport = {
    file: File;
    info: BackupInfo;
};

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
            showDrawButton:
                settings.showDrawButton,

            enableHaptics:
                settings.enableHaptics,

            themeName:
                settings.themeName,

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
 * app's documents directory.
 */
export async function exportBackup(): Promise<BackupExport> {
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
        Paths.document,
        `hope-cards-backup-${today}.json`
    );

    file.create({
        overwrite: true,
    });

    file.write(json);

    const fileInfo = file.info();

    if (
        !fileInfo.exists ||
        !fileInfo.size
    ) {
        throw new Error(
            "Backup file was not created."
        );
    }

    const info: BackupInfo = {
        createdAt:
            backup.createdAt,

        fileName:
            file.name,

        version:
            backup.version,

        favoriteCount:
            backup.favorites.length,
    };

    return {
        file,
        info,
    };
}

/**
 * Creates a backup file and opens
 * the native Share dialog.
 */
export async function shareBackup(): Promise<BackupInfo> {
    const available =
        await Sharing.isAvailableAsync();

    if (!available) {
        throw new Error(
            "Sharing is not available on this device."
        );
    }

    const {
        file,
        info,
    } = await exportBackup();

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

    await saveBackupInfo(info);

    return info;
}

/**
 * Opens the native file picker and
 * returns the selected backup file.
 */
export async function pickBackupFile():
    Promise<DocumentPicker.DocumentPickerAsset | null> {
    const result =
        await DocumentPicker.getDocumentAsync({
            type: "*/*",
            copyToCacheDirectory: true,
            multiple: false,
        });

    if (result.canceled) {
        return null;
    }

    return result.assets[0];
}
/**
 * Reads and parses a selected
 * backup file.
 */
export async function readBackupFile(
    asset: DocumentPicker.DocumentPickerAsset
): Promise<BackupData> {

    const file = new File(asset.uri);

    const json =
        await file.text();

    return JSON.parse(
        json
    ) as BackupData;
}

/**
 * Validates the structure of a
 * Hope Cards backup.
 */
export function validateBackup(
    backup: unknown
): backup is BackupData {
    if (
        typeof backup !== "object" ||
        backup === null
    ) {
        return false;
    }

    const data =
        backup as Partial<BackupData>;

    if (data.version !== 1) {
        return false;
    }

    if (
        typeof data.createdAt !==
        "string"
    ) {
        return false;
    }

    if (
        !Array.isArray(
            data.favorites
        )
    ) {
        return false;
    }

    if (
        typeof data.settings !==
        "object" ||
        data.settings === null
    ) {
        return false;
    }

    if (
        typeof data.settings
            .showDrawButton !==
        "boolean"
    ) {
        return false;
    }

    if (
        typeof data.settings
            .enableHaptics !==
        "boolean"
    ) {
        return false;
    }

    if (
        data.settings.themeName !==
            undefined &&
        !(
            THEME_NAMES as string[]
        ).includes(
            data.settings.themeName
        )
    ) {
        return false;
    }

    if (
        typeof data.settings
            .preferredTranslation !==
        "string"
    ) {
        return false;
    }

    return true;
}

/**
 * Opens a backup file, reads it and
 * validates its contents.
 *
 * Returns:
 * - BackupData when successful
 * - null if the user cancelled
 *
 * Throws:
 * - Error if the selected file is invalid
 */
export async function loadBackup():
    Promise<BackupData | null> {
    const asset =
        await pickBackupFile();

    if (!asset) {
        return null;
    }

    try {
        const backup =
            await readBackupFile(asset);

        if (
            !validateBackup(backup)
        ) {
            throw new Error(
                "Invalid backup file."
            );
        }

        return backup;
    } catch (error) {
        console.error(error);

        if (error instanceof Error) {
            throw error;
        }

        throw new Error(
            "Unable to read the backup file."
        );
    }
}
