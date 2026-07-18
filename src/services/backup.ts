import * as DocumentPicker from "expo-document-picker";
import * as Sharing from "expo-sharing";

import {
    Directory,
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
    "roseDawn",
    "oliveGrove",
    "serenity",
    "stillWater",
    "midnight",
];

const BACKUP_DIRECTORY_NAME =
    "backups";

const BACKUP_FILE_PREFIX =
    "hope-cards-backup-";

const BACKUP_FILE_EXTENSION =
    ".json";

const MAX_LOCAL_BACKUPS = 10;

export interface BackupData {
    version: 1;

    createdAt: string;

    favorites: string[];

    settings: {
        showDrawButton: boolean;
        enableHaptics: boolean;
        dailyHopeReminderEnabled?: boolean;
        dailyHopeMusicEnabled?: boolean;
        dailyHopeReminderHour?: number;
        dailyHopeReminderMinute?: number;
        themeName?: AppThemeName;

        /**
         * Reserved for future versions.
         */
        preferredTranslation: string;
    };
}

export type BackupExport = {
    backup: BackupData;
    file: File;
    info: BackupInfo;
};

function getBackupDirectory(): Directory {
    const directory = new Directory(
        Paths.document,
        BACKUP_DIRECTORY_NAME
    );

    directory.create({
        idempotent: true,
        intermediates: true,
    });

    return directory;
}

function getBackupFileName(
    createdAt: string
): string {
    const timestamp = createdAt.replace(
        /[:.]/g,
        "-"
    );

    return `${BACKUP_FILE_PREFIX}${timestamp}${BACKUP_FILE_EXTENSION}`;
}

function getLocalBackupFiles(): File[] {
    return getBackupDirectory()
        .list()
        .filter(
            (entry): entry is File =>
                entry instanceof File &&
                entry.name.startsWith(
                    BACKUP_FILE_PREFIX
                ) &&
                entry.name.endsWith(
                    BACKUP_FILE_EXTENSION
                )
        )
        .sort((a, b) =>
            b.name.localeCompare(a.name)
        );
}

function removeOldLocalBackups() {
    const oldBackups =
        getLocalBackupFiles().slice(
            MAX_LOCAL_BACKUPS
        );

    for (const file of oldBackups) {
        file.delete();
    }
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
            showDrawButton:
                settings.showDrawButton,

            enableHaptics:
                settings.enableHaptics,

            dailyHopeReminderEnabled:
                settings
                    .dailyHopeReminderEnabled,

            dailyHopeMusicEnabled:
                settings.dailyHopeMusicEnabled,

            dailyHopeReminderHour:
                settings.dailyHopeReminderHour,

            dailyHopeReminderMinute:
                settings
                    .dailyHopeReminderMinute,

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
 * Creates a timestamped backup in the
 * app's dedicated documents directory.
 */
export async function exportBackup(): Promise<BackupExport> {
    const backup =
        await createBackup();

    return saveBackupLocally(backup);
}

/**
 * Writes validated backup data to local storage.
 * Used for both new and downloaded backups.
 */
export async function saveBackupLocally(
    backup: BackupData
): Promise<BackupExport> {
    if (!validateBackup(backup)) {
        throw new Error(
            "Invalid backup data."
        );
    }

    const json = JSON.stringify(
        backup,
        null,
        2
    );

    const file = new File(
        getBackupDirectory(),
        getBackupFileName(
            backup.createdAt
        )
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

    await saveBackupInfo(info);

    removeOldLocalBackups();

    return {
        backup,
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
    return readBackupFromFile(
        new File(asset.uri)
    );
}

async function readBackupFromFile(
    file: File
): Promise<BackupData> {
    const json = await file.text();

    const backup: unknown =
        JSON.parse(json);

    if (!validateBackup(backup)) {
        throw new Error(
            "Invalid backup file."
        );
    }

    return backup;
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
        ) ||
        !data.favorites.every(
            (favorite) =>
                typeof favorite ===
                "string"
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
        data.settings
            .dailyHopeReminderEnabled !==
            undefined &&
        typeof data.settings
            .dailyHopeReminderEnabled !==
            "boolean"
    ) {
        return false;
    }

    if (
        data.settings
            .dailyHopeMusicEnabled !==
            undefined &&
        typeof data.settings
            .dailyHopeMusicEnabled !==
            "boolean"
    ) {
        return false;
    }

    if (
        data.settings
            .dailyHopeReminderHour !==
            undefined &&
        (
            !Number.isInteger(
                data.settings
                    .dailyHopeReminderHour
            ) ||
            data.settings
                .dailyHopeReminderHour < 0 ||
            data.settings
                .dailyHopeReminderHour > 23
        )
    ) {
        return false;
    }

    if (
        data.settings
            .dailyHopeReminderMinute !==
            undefined &&
        (
            !Number.isInteger(
                data.settings
                    .dailyHopeReminderMinute
            ) ||
            data.settings
                .dailyHopeReminderMinute < 0 ||
            data.settings
                .dailyHopeReminderMinute > 59
        )
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
        return await readBackupFile(
            asset
        );
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

/**
 * Loads the newest backup saved in the
 * app's local backup directory.
 */
export async function loadLatestLocalBackup():
    Promise<BackupData | null> {
    const [latestBackup] =
        getLocalBackupFiles();

    if (!latestBackup) {
        return null;
    }

    return readBackupFromFile(
        latestBackup
    );
}
