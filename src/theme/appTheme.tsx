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
import {
  getPremiumStatus,
  isPremiumTheme,
} from "../services/premium";

export type AppThemeName =
  | "classic"
  | "serenity"
  | "stillWater"
  | "midnight";

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
  cardBackText: string;
  cardBackAccent: string;
  cardText: string;
  cardMuted: string;
  buttonBackground: string;
  buttonBorder: string;
  buttonText: string;
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
    cardBackText: "#F8F6F2",
    cardBackAccent: "#C89B3C",
    cardText: "#273043",
    cardMuted: "#7A8292",
    buttonBackground: "#1A2747",
    buttonBorder: "#C89B3C",
    buttonText: "#FFFFFF",
    danger: "#C0392B",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D9D9D9",
    drawerActiveBackground: "#F5EAC8",
  },

  serenity: {
    name: "serenity",
    label: "Sage",
    description: "Deep sage with fresh green.",
    background: "#F6F8F3",
    homeBackground: "#F3F7F0",
    surface: "#FFFFFF",
    text: "#20312B",
    textSecondary: "#6F7D73",
    textTertiary: "#879589",
    primary: "#20312B",
    accent: "#6FA083",
    accentSoft: "#E1EEE3",
    accentLine: "#BFD6C7",
    divider: "#DCE5DB",
    cardFront: "#FBFCF8",
    cardBack: "#285443",
    cardBackText: "#FFFDF4",
    cardBackAccent: "#8FC3A3",
    cardText: "#20312B",
    cardMuted: "#6F7D73",
    buttonBackground: "#285443",
    buttonBorder: "#8FC3A3",
    buttonText: "#FFFFFF",
    danger: "#B84A4A",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D4DAD2",
    drawerActiveBackground: "#DDEBDD",
  },

  stillWater: {
    name: "stillWater",
    label: "Still Water",
    description: "Deep teal with clear blue.",
    background: "#F4F8FA",
    homeBackground: "#F0F6F9",
    surface: "#FFFFFF",
    text: "#1F2D3A",
    textSecondary: "#6B7A86",
    textTertiary: "#8A98A3",
    primary: "#1F2D3A",
    accent: "#5F9EC1",
    accentSoft: "#DDEEF6",
    accentLine: "#BBD8E7",
    divider: "#D6E0E5",
    cardFront: "#FAFCFD",
    cardBack: "#234D63",
    cardBackText: "#F8FCFD",
    cardBackAccent: "#83C3E2",
    cardText: "#1F2D3A",
    cardMuted: "#6B7A86",
    buttonBackground: "#234D63",
    buttonBorder: "#83C3E2",
    buttonText: "#FFFFFF",
    danger: "#B84A4A",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D2DADE",
    drawerActiveBackground: "#D8E8EF",
  },

  midnight: {
    name: "midnight",
    label: "Midnight",
    description: "Black cards with antique gold.",
    background: "#F7F4EE",
    homeBackground: "#F3EFE7",
    surface: "#FFFFFF",
    text: "#1B1A18",
    textSecondary: "#726B61",
    textTertiary: "#91887B",
    primary: "#1B1A18",
    accent: "#B98F3B",
    accentSoft: "#EFE2C5",
    accentLine: "#D9C28D",
    divider: "#E5DED2",
    cardFront: "#FBF8F1",
    cardBack: "#121212",
    cardBackText: "#F7F1E5",
    cardBackAccent: "#C9A24F",
    cardText: "#25221D",
    cardMuted: "#7A7165",
    buttonBackground: "#121212",
    buttonBorder: "#C9A24F",
    buttonText: "#FFFFFF",
    danger: "#B84A4A",
    shadow: "#000",
    white: "#FFFFFF",
    switchOff: "#D7D1C7",
    drawerActiveBackground: "#EFE2C5",
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

      const premiumStatus =
        await getPremiumStatus();

      const savedTheme =
        settings.themeName;

      if (
        savedTheme in THEMES &&
        (!isPremiumTheme(savedTheme) ||
          premiumStatus.isPremium)
      ) {
        setThemeName(savedTheme);
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
