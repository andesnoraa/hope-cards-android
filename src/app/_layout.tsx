import {
  router,
  Stack,
  useRootNavigationState,
} from "expo-router";

import { useFonts } from "expo-font";
import { StatusBar } from "expo-status-bar";
import { useEffect } from "react";

import {
  Poppins_400Regular,
  Poppins_600SemiBold,
  Poppins_700Bold,
} from "@expo-google-fonts/poppins";

import {
  SourceSerif4_400Regular,
  SourceSerif4_600SemiBold,
} from "@expo-google-fonts/source-serif-4";

import {
  registerDailyHopeNotificationHandler,
  syncDailyHopeReminderSchedule,
} from "../services/dailyHopeNotifications";

import {
  AppThemeProvider,
  useAppTheme,
} from "../theme/appTheme";

function useNotificationObserver(
  enabled: boolean
) {
  useEffect(() => {
    if (!enabled) {
      return;
    }

    let cleanup: (() => void) | undefined;
    let mounted = true;

    registerDailyHopeNotificationHandler(
      () => {
        requestAnimationFrame(() => {
          router.replace("/daily");
        });
      }
    )
      .then((nextCleanup) => {
        if (mounted) {
          cleanup = nextCleanup;
        } else {
          nextCleanup();
        }
      })
      .catch(console.error);

    return () => {
      mounted = false;
      cleanup?.();
    };
  }, [enabled]);
}

function RootStack() {
  const { theme } = useAppTheme();

  const rootNavigationState =
    useRootNavigationState();

  const [fontsLoaded] = useFonts({
    Poppins_400Regular,
    Poppins_600SemiBold,
    Poppins_700Bold,

    SourceSerif4_400Regular,
    SourceSerif4_600SemiBold,
  });

  const navigationReady =
    fontsLoaded &&
    Boolean(rootNavigationState?.key);

  useNotificationObserver(navigationReady);

  useEffect(() => {
    if (!navigationReady) {
      return;
    }

    syncDailyHopeReminderSchedule().catch(
      console.error
    );
  }, [navigationReady]);

  if (!fontsLoaded) {
    return null;
  }

  return (
    <>
      <StatusBar style="dark" />

      <Stack>
        <Stack.Screen
          name="(drawer)"
          options={{
            headerShown: false,
          }}
        />

        <Stack.Screen
          name="verse/[id]"
          options={{
            title: "Verse",

            headerShown: true,

            headerLargeTitle: false,

            headerTintColor: theme.text,

            headerTitleStyle: {
              color: theme.text,
              fontWeight: "600",
            },

            headerStyle: {
              backgroundColor:
                theme.background,
            },

            headerShadowVisible: true,
          }}
        />
      </Stack>
    </>
  );
}

export default function RootLayout() {
  return (
    <AppThemeProvider>
      <RootStack />
    </AppThemeProvider>
  );
}
