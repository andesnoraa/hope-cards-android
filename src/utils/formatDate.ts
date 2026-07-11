export function formatRelativeDate(
    isoDate: string
): string {
    const date = new Date(isoDate);

    const now = new Date();

    const today = new Date(
        now.getFullYear(),
        now.getMonth(),
        now.getDate()
    );

    const yesterday = new Date(today);

    yesterday.setDate(
        yesterday.getDate() - 1
    );

    const target = new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate()
    );

    const time = new Intl.DateTimeFormat(
        "en-GB",
        {
            hour: "numeric",
            minute: "2-digit",
        }
    ).format(date);

    if (
        target.getTime() ===
        today.getTime()
    ) {
        return `Today • ${time}`;
    }

    if (
        target.getTime() ===
        yesterday.getTime()
    ) {
        return `Yesterday • ${time}`;
    }

    const formattedDate =
        new Intl.DateTimeFormat("en-GB", {
            day: "numeric",
            month: "short",
            year: "numeric",
        }).format(date);

    return `${formattedDate} • ${time}`;
}