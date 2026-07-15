import AsyncStorage from "@react-native-async-storage/async-storage";

import type { AppThemeName } from "../theme/appTheme";

const PREMIUM_KEY = "hope_cards_premium";

export const PREMIUM_THEME_NAMES: AppThemeName[] = [
  "serenity",
  "stillWater",
  "midnight",
];

export type PremiumStatus = {
  isPremium: boolean;
};

export function isPremiumTheme(
  themeName: AppThemeName
) {
  return PREMIUM_THEME_NAMES.includes(
    themeName
  );
}

export async function getPremiumStatus(): Promise<PremiumStatus> {
  const value = await AsyncStorage.getItem(
    PREMIUM_KEY
  );

  return {
    isPremium: value === "true",
  };
}

export async function unlockPremium(): Promise<PremiumStatus> {
  await AsyncStorage.setItem(
    PREMIUM_KEY,
    "true"
  );

  return {
    isPremium: true,
  };
}

export async function restorePremium(): Promise<PremiumStatus> {
  return getPremiumStatus();
}
