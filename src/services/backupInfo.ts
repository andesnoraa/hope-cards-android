import AsyncStorage from "@react-native-async-storage/async-storage";

const BACKUP_INFO_KEY =
    "hope_cards_backup_info";

export interface BackupInfo {
    createdAt: string;

    fileName: string;

    version: number;

    favoriteCount: number;
}

export async function getBackupInfo(): Promise<BackupInfo | null> {
    try {
        const json =
            await AsyncStorage.getItem(
                BACKUP_INFO_KEY
            );

        if (!json) {
            return null;
        }

        return JSON.parse(json);
    } catch {
        return null;
    }
}

export async function saveBackupInfo(
    info: BackupInfo
): Promise<void> {
    await AsyncStorage.setItem(
        BACKUP_INFO_KEY,
        JSON.stringify(info)
    );
}

export async function clearBackupInfo(): Promise<void> {
    await AsyncStorage.removeItem(
        BACKUP_INFO_KEY
    );
}