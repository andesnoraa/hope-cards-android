import { Stack } from "expo-router";

import { useFonts } from "expo-font";

import {
  Poppins_400Regular,
  Poppins_600SemiBold,
  Poppins_700Bold,
} from "@expo-google-fonts/poppins";

import {
  SourceSerif4_400Regular,
  SourceSerif4_600SemiBold,
} from "@expo-google-fonts/source-serif-4";

export default function RootLayout() {
  const [fontsLoaded] = useFonts({
    Poppins_400Regular,
    Poppins_600SemiBold,
    Poppins_700Bold,

    SourceSerif4_400Regular,
    SourceSerif4_600SemiBold,
  });

  if (!fontsLoaded) {
    return null;
  }

  return (
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

          headerTintColor: "#1A2747",

          headerTitleStyle: {
            color: "#1A2747",
            fontWeight: "600",
          },

          headerStyle: {
            backgroundColor: "#F8F6F2",
          },

          headerShadowVisible: true,
        }}
      />
    </Stack>
  );
}