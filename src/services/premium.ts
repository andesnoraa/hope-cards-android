import Constants from "expo-constants";
import { Platform } from "react-native";
import Purchases, {
  LOG_LEVEL,
  type CustomerInfo,
  type PurchasesPackage,
} from "react-native-purchases";

import type { AppThemeName } from "../theme/appTheme";

const REVENUECAT_ANDROID_API_KEY =
  process.env
    .EXPO_PUBLIC_REVENUECAT_ANDROID_API_KEY;

const HAS_VALID_REVENUECAT_ANDROID_API_KEY =
  REVENUECAT_ANDROID_API_KEY?.startsWith(
    "goog_"
  ) === true;

const PREMIUM_ENTITLEMENT_ID = "Hope Cards Pro";

export const PREMIUM_THEME_NAMES: AppThemeName[] = [
  "roseDawn",
  "oliveGrove",
  "serenity",
  "stillWater",
  "midnight",
];

export type PremiumStatus = {
  isPremium: boolean;
  isConfigured: boolean;
  managementURL?: string | null;
};

export type PremiumOffering = {
  packageToPurchase: PurchasesPackage | null;
  priceText: string | null;
  trialDays: number | null;
};

function getTrialDays(
  packageToPurchase: PurchasesPackage | null
) {
  const period =
    packageToPurchase?.product.defaultOption
      ?.freePhase?.billingPeriod;

  if (!period) {
    return null;
  }

  if (period.unit === "DAY") {
    return period.value;
  }

  if (period.unit === "WEEK") {
    return period.value * 7;
  }

  return null;
}

let configurePromise: Promise<boolean> | null = null;

function isRunningInExpoGo() {
  return Constants.appOwnership === "expo";
}

export function isPremiumTheme(
  themeName: AppThemeName
) {
  return PREMIUM_THEME_NAMES.includes(
    themeName
  );
}

function hasPremiumEntitlement(
  customerInfo: CustomerInfo
) {
  const activeEntitlements =
    customerInfo.entitlements.active;

  return (
    activeEntitlements[PREMIUM_ENTITLEMENT_ID]
      ?.isActive === true
  );
}

function getStatusFromCustomerInfo(
  customerInfo: CustomerInfo
): PremiumStatus {
  return {
    isPremium:
      hasPremiumEntitlement(customerInfo),
    isConfigured: true,
    managementURL: customerInfo.managementURL,
  };
}

async function configurePurchases() {
  if (configurePromise) {
    return configurePromise;
  }

  configurePromise = (async () => {
    if (
      Platform.OS !== "android" ||
      isRunningInExpoGo() ||
      !HAS_VALID_REVENUECAT_ANDROID_API_KEY
    ) {
      return false;
    }

    const alreadyConfigured =
      await Purchases.isConfigured();

    if (!alreadyConfigured) {
      if (__DEV__) {
        await Purchases.setLogLevel(
          LOG_LEVEL.DEBUG
        );
      }

      Purchases.configure({
        apiKey:
          REVENUECAT_ANDROID_API_KEY,
        store: "PLAY_STORE",
      });
    }

    return true;
  })();

  return configurePromise;
}

export async function getPremiumStatus(): Promise<PremiumStatus> {
  const isConfigured =
    await configurePurchases();

  if (!isConfigured) {
    return {
      isPremium: false,
      isConfigured: false,
    };
  }

  const customerInfo =
    await Purchases.getCustomerInfo();

  return getStatusFromCustomerInfo(
    customerInfo
  );
}

export async function getPremiumOffering(): Promise<PremiumOffering> {
  const isConfigured =
    await configurePurchases();

  if (!isConfigured) {
    return {
      packageToPurchase: null,
      priceText: null,
      trialDays: null,
    };
  }

  const offerings =
    await Purchases.getOfferings();
  const currentOffering = offerings.current;

  const packageToPurchase =
      currentOffering?.monthly ??
      currentOffering
        ?.availablePackages[0] ??
      null;

  return {
    packageToPurchase,
    priceText:
      packageToPurchase?.product.priceString ?? null,
    trialDays: getTrialDays(packageToPurchase),
  };
}

export async function purchasePremium(): Promise<PremiumStatus> {
  const { packageToPurchase } =
    await getPremiumOffering();

  if (!packageToPurchase) {
    throw new Error(
      "Premium subscription is not ready yet. Check the RevenueCat API key and offering setup."
    );
  }

  const result =
    await Purchases.purchasePackage(
      packageToPurchase
    );

  return getStatusFromCustomerInfo(
    result.customerInfo
  );
}

export async function restorePremium(): Promise<PremiumStatus> {
  const isConfigured =
    await configurePurchases();

  if (!isConfigured) {
    return {
      isPremium: false,
      isConfigured: false,
    };
  }

  const customerInfo =
    await Purchases.restorePurchases();

  return getStatusFromCustomerInfo(
    customerInfo
  );
}
