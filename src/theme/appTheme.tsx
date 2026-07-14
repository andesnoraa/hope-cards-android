import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  getSettings,
} from "../services/settings";

export type AppThemeName =
  | "classic"
  | "serenity"
  | "stillWater";

export type AppTheme = {
  name: AppThemeName;
  label: string;
  description: string;
  background: string;
  homeBackground: string;
  surface: string;
  text: string;
  textSecondary: string;
  textTertiary: string;
  primary: string;
  accent: string;
  accentSoft: string;
  accentLine: string;
  divider: string;
  cardFront: string;
  cardBack: string;
  cardText: string;
  cardMuted: string;
  danger: string;
  shadow: string;
  white: string;
  switchOff: string;
  drawerActiveBackground: string;
};

export const THEMES: Record<
  AppThemeName,
  AppTheme
> = {
  classic: {
    name: "classic",
    label: "Classic",
    description: "Warm cream, navy, and gold.",
    background: "#F8F6F2",
    homeBackground: "#F7F5F1",
    surface: "#FFFFFF",
    text: "#1A2747",
    textSecondary: "#777777",
    textTertiary: "#8C93A3",
    primary: "#1A2747",
    accent: "#C89B3C",
    accentSoft: "#F5EAC8",
    accentLine: "#E7D7B2",
    divider: "#E7E2D8",
    cardFront: "#F8F6F2",
    cardBack: "#1A2747",
    cardText: "#273043",
    cardMuted: "#7A8292",
    danger: "#C0392B",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D9D9D9",
    drawerActiveBackground: "#F5EAC8",
  },

  serenity: {
    name: "serenity",
    label: "Serenity",
    description: "Sage, ivory, and quiet green.",
    background: "#F4F7F1",
    homeBackground: "#F1F5EE",
    surface: "#FFFFFF",
    text: "#20312B",
    textSecondary: "#6F7D73",
    textTertiary: "#879589",
    primary: "#20312B",
    accent: "#5F8D75",
    accentSoft: "#DDEBDD",
    accentLine: "#C9DACD",
    divider: "#D7E0D8",
    cardFront: "#FAFCF7",
    cardBack: "#29463A",
    cardText: "#20312B",
    cardMuted: "#6F7D73",
    danger: "#B84A4A",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D4DAD2",
    drawerActiveBackground: "#DDEBDD",
  },

  stillWater: {
    name: "stillWater",
    label: "Still Water",
    description: "Mist blue, slate, and pearl.",
    background: "#F2F6F8",
    homeBackground: "#EEF4F7",
    surface: "#FFFFFF",
    text: "#1F2D3A",
    textSecondary: "#6B7A86",
    textTertiary: "#8A98A3",
    primary: "#1F2D3A",
    accent: "#4E7D96",
    accentSoft: "#D8E8EF",
    accentLine: "#C6D9E2",
    divider: "#D6E0E5",
    cardFront: "#F8FBFC",
    cardBack: "#263E4B",
    cardText: "#1F2D3A",
    cardMuted: "#6B7A86",
    danger: "#B84A4A",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D2DADE",
    drawerActiveBackground: "#D8E8EF",
  },
};

export const THEME_OPTIONS =
  Object.values(THEMES);

type ThemeContextValue = {
  themeName: AppThemeName;
  theme: AppTheme;
  setThemeName: (
    themeName: AppThemeName
  ) => void;
};

const ThemeContext =
  createContext<ThemeContextValue | null>(
    null
  );

export function AppThemeProvider({
  children,
}: {
  children: ReactNode;
}) {
  const [themeName, setThemeName] =
    useState<AppThemeName>("classic");

  useEffect(() => {
    async function loadTheme() {
      const settings =
        await getSettings();

      if (settings.themeName in THEMES) {
        setThemeName(settings.themeName);
      } else {
        setThemeName("classic");
      }
    }

    loadTheme();
  }, []);

  const value = useMemo(
    () => ({
      themeName,
      theme: THEMES[themeName],
      setThemeName,
    }),
    [themeName]
  );

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useAppTheme() {
  const context = useContext(ThemeContext);

  if (!context) {
    throw new Error(
      "useAppTheme must be used inside AppThemeProvider."
    );
  }

  return context;
}
