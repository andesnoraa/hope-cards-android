import Ionicons from "@expo/vector-icons/Ionicons";
import Drawer from "expo-router/drawer";

import {
  useAppTheme,
} from "../../theme/appTheme";

export default function DrawerLayout() {
  const { theme } = useAppTheme();

  return (
    <Drawer
      screenOptions={{
        headerShown: true,

        headerTitleStyle: {
          fontSize: 24,
          fontWeight: "700",
          color: theme.text,
          letterSpacing: 0.3,
        },

        headerStyle: {
          backgroundColor: theme.background,
        },

        headerShadowVisible: false,

        headerTintColor: theme.text,

        sceneStyle: {
          backgroundColor: theme.background,
        },

        drawerStyle: {
          backgroundColor: theme.background,
          width: 305,
        },

        drawerActiveTintColor: theme.accent,
        drawerInactiveTintColor:
          theme.cardText,

        drawerActiveBackgroundColor:
          theme.drawerActiveBackground,

        drawerItemStyle: {
          marginHorizontal: 12,
          marginVertical: 6,
          borderRadius: 12,
          paddingHorizontal: 8,
        },

        drawerLabelStyle: {
          fontSize: 16,
          fontWeight: "600",
          marginLeft: -2,
          letterSpacing: 0.2,
        },
      }}
    >
      <Drawer.Screen
        name="index"
        options={{
          title: "Hope Cards",
          drawerLabel: "Hope Cards",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="home-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="daily"
        options={{
          title: "Daily Hope",
          drawerLabel: "Daily Hope",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="sunny-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="favorites"
        options={{
          title: "Favorites",
          drawerLabel: "Favorites",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="heart-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="premium"
        options={{
          title: "Premium",
          drawerLabel: "Premium",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="sparkles-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="settings"
        options={{
          title: "Settings",
          drawerLabel: "Settings",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="settings-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="privacy"
        options={{
          title: "Privacy Policy",
          drawerLabel: "Privacy Policy",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="shield-checkmark-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="about"
        options={{
          title: "About",
          drawerLabel: "About",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="information-circle-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />
    </Drawer>
  );
}
