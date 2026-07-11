import AsyncStorage from "@react-native-async-storage/async-storage";

import type { Verse } from "../types/verse";

import {
    getRandomVerse,
    getVerseById,
} from "./verseService";

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

    const saved = await loadDailyHope();

    if (
        saved &&
        saved.date === today
    ) {
        const verse = getVerseById(
            saved.verseId
        );

        if (verse) {
            return verse;
        }
    }

    const previousVerseId =
        saved?.verseId;

    const verse =
        getRandomVerse(previousVerseId);

    await saveDailyHope({
        date: today,
        verseId: verse.id,
        translation: "en-bbe",
    });

    return verse;
}