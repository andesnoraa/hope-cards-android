import Constants from "expo-constants";
import { Platform } from "react-native";

import {
    getSettings,
    updateSettings,
} from "./settings";

export const DAILY_HOPE_CHANNEL_ID =
    "daily-hope";

const DAILY_HOPE_NOTIFICATION_KEY =
    "dailyHopeReminder";

let lastHandledNotificationKey:
    | string
    | null = null;

type NotificationsModule =
    typeof import("expo-notifications");

type DailyHopeReminderOptions = {
    hour: number;
    minute: number;
};

export type DailyHopeReminderStatus =
    | "granted"
    | "denied"
    | "unsupported";

export function formatReminderTime(
    hour: number,
    minute: number
): string {
    const hour12 = hour % 12 || 12;
    const period =
        hour >= 12 ? "pm" : "am";

    return `${hour12}:${String(minute).padStart(2, "0")} ${period}`;
}

function isAndroidExpoGo(): boolean {
    return (
        Platform.OS === "android" &&
        Constants.appOwnership === "expo"
    );
}

async function getNotificationsModule():
    Promise<NotificationsModule | null> {
    if (
        Platform.OS === "web" ||
        isAndroidExpoGo()
    ) {
        return null;
    }

    try {
        return await import("expo-notifications");
    } catch (error) {
        console.warn(error);
        return null;
    }
}

async function ensureAndroidChannel(
    Notifications: NotificationsModule
) {
    if (Platform.OS !== "android") {
        return;
    }

    await Notifications.setNotificationChannelAsync(
        DAILY_HOPE_CHANNEL_ID,
        {
            name: "Daily Hope",
            importance:
                Notifications.AndroidImportance.DEFAULT,
            vibrationPattern: [0, 250, 250, 250],
            lightColor: "#D4AF37",
            description:
                "Daily reminders to read today's hope.",
        }
    );
}

async function requestNotificationPermission():
    Promise<DailyHopeReminderStatus> {
    const Notifications =
        await getNotificationsModule();

    if (!Notifications) {
        return "unsupported";
    }

    await ensureAndroidChannel(Notifications);

    const existing =
        await Notifications.getPermissionsAsync();

    let finalStatus =
        existing.status;

    if (finalStatus !== "granted") {
        const requested =
            await Notifications.requestPermissionsAsync();

        finalStatus = requested.status;
    }

    if (finalStatus !== "granted") {
        return "denied";
    }

    return "granted";
}

async function getExistingPermissionStatus():
    Promise<DailyHopeReminderStatus> {
    const Notifications =
        await getNotificationsModule();

    if (!Notifications) {
        return "unsupported";
    }

    const existing =
        await Notifications.getPermissionsAsync();

    if (existing.status !== "granted") {
        return "denied";
    }

    return "granted";
}

async function cancelExistingDailyHopeReminders(
    Notifications: NotificationsModule
) {
    const scheduled =
        await Notifications.getAllScheduledNotificationsAsync();

    await Promise.all(
        scheduled
            .filter(
                (notification) =>
                    notification.content.data?.kind ===
                    DAILY_HOPE_NOTIFICATION_KEY
            )
            .map((notification) =>
                Notifications.cancelScheduledNotificationAsync(
                    notification.identifier
                )
            )
    );
}

async function scheduleDailyHopeNotification({
    hour,
    minute,
}: DailyHopeReminderOptions) {
    const Notifications =
        await getNotificationsModule();

    if (!Notifications) {
        return;
    }

    await cancelExistingDailyHopeReminders(
        Notifications
    );

    await Notifications.scheduleNotificationAsync({
        content: {
            title: "Today's Hope is ready",
            body: "Take a quiet moment with today's verse.",
            sound: true,
            data: {
                kind: DAILY_HOPE_NOTIFICATION_KEY,
                url: "/daily",
            },
        },
        trigger: {
            type: Notifications
                .SchedulableTriggerInputTypes.DAILY,
            hour,
            minute,
            channelId: DAILY_HOPE_CHANNEL_ID,
        },
    });
}

export async function enableDailyHopeReminder({
    hour,
    minute,
}: DailyHopeReminderOptions):
    Promise<DailyHopeReminderStatus> {
    const status =
        await requestNotificationPermission();

    if (status !== "granted") {
        await updateSettings({
            dailyHopeReminderEnabled: false,
        });

        return status;
    }

    await scheduleDailyHopeNotification({
        hour,
        minute,
    });

    await updateSettings({
        dailyHopeReminderEnabled: true,
        dailyHopeReminderHour: hour,
        dailyHopeReminderMinute: minute,
    });

    return "granted";
}

export async function disableDailyHopeReminder() {
    const Notifications =
        await getNotificationsModule();

    if (Notifications) {
        await cancelExistingDailyHopeReminders(
            Notifications
        );
    }

    await updateSettings({
        dailyHopeReminderEnabled: false,
    });
}

export async function updateDailyHopeReminderTime(
    options: DailyHopeReminderOptions
): Promise<DailyHopeReminderStatus> {
    return enableDailyHopeReminder(options);
}

export async function syncDailyHopeReminderSchedule() {
    const settings =
        await getSettings();

    if (!settings.dailyHopeReminderEnabled) {
        return;
    }

    const status =
        await getExistingPermissionStatus();

    if (status !== "granted") {
        await updateSettings({
            dailyHopeReminderEnabled: false,
        });

        return;
    }

    const Notifications =
        await getNotificationsModule();

    if (!Notifications) {
        await updateSettings({
            dailyHopeReminderEnabled: false,
        });

        return;
    }

    const scheduled =
        await Notifications.getAllScheduledNotificationsAsync();

    const hasDailyHopeReminder =
        scheduled.some(
            (notification) =>
                notification.content.data?.kind ===
                DAILY_HOPE_NOTIFICATION_KEY
        );

    if (!hasDailyHopeReminder) {
        await scheduleDailyHopeNotification({
            hour: settings.dailyHopeReminderHour,
            minute:
                settings.dailyHopeReminderMinute,
        });
    }
}

export async function registerDailyHopeNotificationHandler(
    onOpenDailyHope: () => void
): Promise<() => void> {
    const Notifications =
        await getNotificationsModule();

    if (!Notifications) {
        return () => {};
    }

    Notifications.setNotificationHandler({
        handleNotification: async () => ({
            shouldPlaySound: true,
            shouldSetBadge: false,
            shouldShowBanner: true,
            shouldShowList: true,
        }),
    });

    function redirectFromNotification(
        notification: import("expo-notifications").Notification
    ) {
        const notificationKey = `${notification.request.identifier}:${notification.date}`;

        if (
            notificationKey ===
            lastHandledNotificationKey
        ) {
            return;
        }

        const url =
            notification.request.content.data?.url;

        if (url === "/daily") {
            lastHandledNotificationKey =
                notificationKey;

            onOpenDailyHope();
        }
    }

    const response =
        Notifications.getLastNotificationResponse();

    if (response?.notification) {
        redirectFromNotification(
            response.notification
        );
    }

    const subscription =
        Notifications.addNotificationResponseReceivedListener(
            (nextResponse) => {
                redirectFromNotification(
                    nextResponse.notification
                );
            }
        );

    return () => {
        subscription.remove();
    };
}
