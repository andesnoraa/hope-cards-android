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
    <Stack
      screenOptions={{
        headerShown: false,
      }}
    />
  );
}