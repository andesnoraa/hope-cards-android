import * as Haptics from "expo-haptics";

import { getSettings } from "./settings";

export async function lightImpact() {
    const settings = await getSettings();

    if (!settings.enableHaptics) {
        return;
    }

    await Haptics.impactAsync(
        Haptics.ImpactFeedbackStyle.Light
    );
}

export async function selection() {
    const settings = await getSettings();

    if (!settings.enableHaptics) {
        return;
    }

    await Haptics.selectionAsync();
}

export async function success() {
    const settings = await getSettings();

    if (!settings.enableHaptics) {
        return;
    }

    await Haptics.notificationAsync(
        Haptics.NotificationFeedbackType.Success
    );
}

export async function warning() {
    const settings = await getSettings();

    if (!settings.enableHaptics) {
        return;
    }

    await Haptics.notificationAsync(
        Haptics.NotificationFeedbackType.Warning
    );
}

export async function error() {
    const settings = await getSettings();

    if (!settings.enableHaptics) {
        return;
    }

    await Haptics.notificationAsync(
        Haptics.NotificationFeedbackType.Error
    );
}