import AsyncStorage from "@react-native-async-storage/async-storage";

import type { Verse } from "../types/verse";

import {
    getRandomVerse,
    getVerseById,
} from "./verseService";
import { getSettings } from "./settings";

const DAILY_HOPE_KEY = "hope_cards_daily_hope";

type DailyHopeRecord = {
    date: string;
    verseId: string;
    translation: string;
};

function getTodayString(): string {
    return new Date().toISOString().split("T")[0];
}

async function saveDailyHope(
    record: DailyHopeRecord
): Promise<void> {
    await AsyncStorage.setItem(
        DAILY_HOPE_KEY,
        JSON.stringify(record)
    );
}

async function loadDailyHope(): Promise<DailyHopeRecord | null> {
    const value = await AsyncStorage.getItem(
        DAILY_HOPE_KEY
    );

    if (!value) {
        return null;
    }

    return JSON.parse(value);
}

export async function clearDailyHope() {
    await AsyncStorage.removeItem(
        DAILY_HOPE_KEY
    );
}

export async function getDailyHope(): Promise<Verse> {
    const today = getTodayString();
    const settings = await getSettings();
    const translation =
        settings.preferredTranslation;

    const saved = await loadDailyHope();

    if (
        saved &&
        saved.date === today
    ) {
        const verse = getVerseById(
            saved.verseId,
            translation
        );

        if (verse) {
            if (saved.translation !== translation) {
                await saveDailyHope({
                    ...saved,
                    translation,
                });
            }
            return verse;
        }
    }

    const previousVerseId =
        saved?.verseId;

    const verse =
        getRandomVerse(
            previousVerseId,
            translation
        );

    await saveDailyHope({
        date: today,
        verseId: verse.id,
        translation,
    });

    return verse;
}
