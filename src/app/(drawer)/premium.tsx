import Ionicons from "@expo/vector-icons/Ionicons";
import { useFocusEffect } from "expo-router";
import {
  useCallback,
  useState,
} from "react";

import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import PremiumNoticeModal from "../../components/premium/PremiumNoticeModal";
import {
  getPremiumOffering,
  getPremiumStatus,
  purchasePremium,
  restorePremium,
} from "../../services/premium";
import { useAppTheme } from "../../theme/appTheme";

const BENEFITS = [
  {
    title: "Draw encouraging Hope Cards",
    free: true,
    premium: true,
  },
  {
    title: "Read a fresh Daily Hope verse",
    free: false,
    premium: true,
  },
  {
    title: "Schedule verse reminders",
    free: false,
    premium: true,
  },
  {
    title: "Unlock premium themes",
    free: false,
    premium: true,
  },
  {
    title: "Backup and restore saved data",
    free: false,
    premium: true,
  },
] as const;

const PREMIUM_YELLOW = "#F2C84B";
const PREMIUM_BORDER = "#C99A2E";

type Notice = {
  title: string;
  message: string;
  icon: keyof typeof Ionicons.glyphMap;
};

function PlanMark({
  active,
  premium,
}: {
  active: boolean;
  premium?: boolean;
}) {
  const { theme } = useAppTheme();

  if (!active) {
    return (
      <Text
        style={[
          styles.emptyMark,
          {
            color: premium
              ? "#111111"
              : theme.textTertiary,
          },
        ]}
      >
        -
      </Text>
    );
  }

  return (
    <Ionicons
      name={
        premium
          ? "star"
          : "checkmark-circle"
      }
      size={premium ? 18 : 20}
      color={
        premium
          ? "#111111"
          : theme.accent
      }
    />
  );
}

export default function PremiumScreen() {
  const { theme } = useAppTheme();
  const [isPremium, setIsPremium] =
    useState(false);
  const [isLoading, setIsLoading] =
    useState(true);
  const [isPurchasing, setIsPurchasing] =
    useState(false);
  const [isRestoring, setIsRestoring] =
    useState(false);
  const [canPurchase, setCanPurchase] =
    useState(false);
  const [notice, setNotice] =
    useState<Notice | null>(null);

  useFocusEffect(
    useCallback(() => {
      let mounted = true;

      async function load() {
        try {
          const [status, offering] =
            await Promise.all([
              getPremiumStatus(),
              getPremiumOffering(),
            ]);

          if (mounted) {
            setIsPremium(
              status.isPremium
            );
            setCanPurchase(
              Boolean(
                offering.packageToPurchase
              )
            );
          }
        } catch {
          if (mounted) {
            setCanPurchase(false);
          }
        } finally {
          if (mounted) {
            setIsLoading(false);
          }
        }
      }

      load();

      return () => {
        mounted = false;
      };
    }, [])
  );

  function getPurchaseMessage(error: unknown) {
    if (
      typeof error === "object" &&
      error !== null &&
      "userCancelled" in error &&
      error.userCancelled
    ) {
      return null;
    }

    if (error instanceof Error) {
      return error.message;
    }

    return "Premium could not be started. Please try again.";
  }

  async function handleUnlock() {
    setIsPurchasing(true);

    try {
      const status =
        await purchasePremium();

      setIsPremium(status.isPremium);
      setCanPurchase(true);

      setNotice({
        title: "Subscription Active",
        message:
          "Hope Cards Premium is active. Daily Hope, reminders, backup tools, and premium themes are now unlocked.",
        icon: "checkmark-circle-outline",
      });
    } catch (error) {
      const message =
        getPurchaseMessage(error);

      if (message) {
        setNotice({
          title: "Premium Not Ready",
          message,
          icon: "information-circle-outline",
        });
      }
    } finally {
      setIsPurchasing(false);
    }
  }

  async function handleRestore() {
    setIsRestoring(true);

    try {
      const status =
        await restorePremium();

      setIsPremium(status.isPremium);

      setNotice(
        status.isPremium
          ? {
              title: "Subscription Restored",
              message:
                "Your Hope Cards Premium subscription is active on this device.",
              icon: "refresh-circle-outline",
            }
          : {
              title: "Nothing to Restore",
              message:
                status.isConfigured
                  ? "No active Hope Cards Premium subscription was found on this device."
                  : "Premium subscriptions are not configured for this build yet.",
              icon: "information-circle-outline",
            }
      );
    } catch (error) {
      setNotice({
        title: "Restore Failed",
        message:
          error instanceof Error
            ? error.message
            : "Subscription restore could not be completed. Please try again.",
        icon: "information-circle-outline",
      });
    } finally {
      setIsRestoring(false);
    }
  }

  return (
    <>
      <ScrollView
        style={[
          styles.container,
          {
            backgroundColor:
              theme.background,
          },
        ]}
        contentContainerStyle={styles.content}
        contentInsetAdjustmentBehavior="automatic"
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.intro}>
          <Text
            style={[
              styles.title,
              { color: theme.text },
            ]}
          >
            Make hope part of every day
          </Text>

          <Text
            style={[
              styles.subtitle,
              {
                color:
                  theme.textSecondary,
              },
            ]}
          >
            Daily verses, reminders, themes,
            and backup tools in one simple
            upgrade.
          </Text>
        </View>

        <View
          style={[
            styles.compareCard,
            {
              backgroundColor:
                theme.surface,
              borderColor:
                theme.accentLine,
            },
          ]}
        >
          <View style={styles.compareHeader}>
            <View style={styles.benefitColumn} />

            <View style={styles.freeColumn}>
              <Text
                style={[
                  styles.planTitle,
                  { color: theme.text },
                ]}
              >
                Free
              </Text>
            </View>

            <View
              style={[
                styles.premiumColumn,
                {
                  backgroundColor:
                    PREMIUM_YELLOW,
                },
              ]}
            >
              <Text
                style={[
                  styles.planTitle,
                  { color: "#111111" },
                ]}
              >
                Premium
              </Text>
            </View>
          </View>

          {BENEFITS.map((benefit) => (
            <View
              key={benefit.title}
              style={styles.compareRow}
            >
              <View
                style={[
                  styles.benefitColumn,
                  styles.benefitCell,
                  {
                    borderColor:
                      theme.accentLine,
                  },
                ]}
              >
                <Text
                  style={[
                    styles.benefitText,
                    { color: theme.text },
                  ]}
                >
                  {benefit.title}
                </Text>
              </View>

              <View
                style={[
                  styles.freeColumn,
                  styles.markCell,
                  {
                    borderColor:
                      theme.accentLine,
                  },
                ]}
              >
                <PlanMark
                  active={benefit.free}
                />
              </View>

              <View
                style={[
                  styles.premiumColumn,
                  styles.markCell,
                  {
                    backgroundColor:
                      PREMIUM_YELLOW,
                    borderColor:
                      PREMIUM_BORDER,
                  },
                ]}
              >
                <PlanMark
                  active={benefit.premium}
                  premium
                />
              </View>
            </View>
          ))}
        </View>

        {isLoading ? (
          <View
            style={[
              styles.activeBanner,
              {
                backgroundColor:
                  theme.surface,
                borderColor:
                  theme.accentLine,
                borderWidth: 1,
              },
            ]}
          >
            <ActivityIndicator
              color={theme.accent}
            />

            <Text
              style={[
                styles.activeText,
                { color: theme.text },
              ]}
            >
              Checking subscription
            </Text>
          </View>
        ) : isPremium ? (
          <View
            style={[
              styles.activeBanner,
              {
                backgroundColor:
                  theme.text,
              },
            ]}
          >
            <Ionicons
              name="checkmark-circle"
              size={20}
              color={PREMIUM_YELLOW}
            />

            <Text
              style={[
                styles.activeText,
                { color: theme.white },
              ]}
            >
              Premium is active
            </Text>
          </View>
        ) : (
          <Pressable
            style={({ pressed }) => [
              styles.subscribeButton,
              {
                backgroundColor:
                  PREMIUM_YELLOW,
                opacity:
                  pressed || isPurchasing
                    ? 0.78
                    : 1,
              },
            ]}
            disabled={isPurchasing}
            onPress={handleUnlock}
          >
            {isPurchasing ? (
              <ActivityIndicator color="#111111" />
            ) : (
              <>
                <Text
                  style={
                    styles.subscribeButtonText
                  }
                >
                  {canPurchase
                    ? "Start Premium"
                    : "Set Up Premium"}
                </Text>

                <Ionicons
                  name="arrow-forward"
                  size={21}
                  color="#111111"
                />
              </>
            )}
          </Pressable>
        )}

        <Pressable
          style={({ pressed }) => [
            styles.restoreButton,
            { opacity: pressed ? 0.65 : 1 },
          ]}
          disabled={isRestoring}
          onPress={handleRestore}
        >
          {isRestoring ? (
            <ActivityIndicator
              size="small"
              color={theme.textSecondary}
            />
          ) : (
            <Ionicons
              name="refresh-outline"
              size={17}
              color={theme.textSecondary}
            />
          )}

          <Text
            style={[
              styles.restoreButtonText,
              { color: theme.textSecondary },
            ]}
          >
            Restore Subscription
          </Text>
        </Pressable>

        <Text
          style={[
            styles.note,
            { color: theme.textTertiary },
          ]}
        >
          Price and billing details will be
          shown by Google Play before purchase.
        </Text>
      </ScrollView>

      <PremiumNoticeModal
        visible={notice !== null}
        title={notice?.title ?? ""}
        message={notice?.message ?? ""}
        icon={notice?.icon}
        primaryLabel="Done"
        onPrimary={() => setNotice(null)}
      />
    </>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  content: {
    paddingHorizontal: 24,
    paddingTop: 16,
    paddingBottom: 42,
    gap: 16,
  },

  intro: {
    alignItems: "center",
    paddingHorizontal: 6,
  },

  title: {
    fontSize: 25,
    lineHeight: 31,
    fontWeight: "700",
    textAlign: "center",
  },

  subtitle: {
    marginTop: 8,
    fontSize: 14,
    lineHeight: 21,
    textAlign: "center",
  },

  compareCard: {
    marginTop: 12,
    borderWidth: 1,
    borderRadius: 30,
    overflow: "hidden",
  },

  compareHeader: {
    minHeight: 54,
    flexDirection: "row",
  },

  compareRow: {
    minHeight: 64,
    flexDirection: "row",
  },

  benefitColumn: {
    flex: 1,
  },

  freeColumn: {
    width: 58,
    alignItems: "center",
    justifyContent: "center",
  },

  premiumColumn: {
    width: 82,
    alignItems: "center",
    justifyContent: "center",
  },

  planTitle: {
    fontSize: 13,
    fontWeight: "700",
  },

  benefitCell: {
    borderTopWidth: 1,
    justifyContent: "center",
    paddingHorizontal: 16,
    paddingVertical: 12,
  },

  benefitText: {
    fontSize: 13,
    lineHeight: 18,
    fontWeight: "600",
  },

  markCell: {
    borderTopWidth: 1,
  },

  emptyMark: {
    fontSize: 18,
    fontWeight: "700",
  },

  subscribeButton: {
    minHeight: 54,
    borderRadius: 27,
    flexDirection: "row",
    gap: 12,
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 18,
  },

  subscribeButtonText: {
    color: "#111111",
    fontSize: 16,
    fontWeight: "700",
    textAlign: "center",
  },

  activeBanner: {
    minHeight: 54,
    borderRadius: 27,
    flexDirection: "row",
    gap: 8,
    alignSelf: "center",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 26,
  },

  activeText: {
    fontSize: 16,
    fontWeight: "700",
  },

  restoreButton: {
    alignSelf: "center",
    flexDirection: "row",
    alignItems: "center",
    gap: 7,
    paddingVertical: 4,
    paddingHorizontal: 18,
  },

  restoreButtonText: {
    fontSize: 15,
    fontWeight: "700",
  },

  note: {
    marginTop: -8,
    fontSize: 12,
    lineHeight: 18,
    textAlign: "center",
  },
});
