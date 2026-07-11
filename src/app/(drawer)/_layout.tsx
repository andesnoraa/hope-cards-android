import Ionicons from "@expo/vector-icons/Ionicons";
import { Drawer } from "expo-router/drawer";

export default function DrawerLayout() {
  return (
    <Drawer
      screenOptions={{
        headerShown: true,

        headerTitle: "Hope Cards",

        headerStyle: {
          backgroundColor: "#F7F5F1",
        },

        headerTintColor: "#1A2747",

        drawerActiveTintColor: "#C5A24C",
        drawerInactiveTintColor: "#273043",

        drawerActiveBackgroundColor: "#F4EEDC",

        drawerLabelStyle: {
          fontSize: 16,
          fontWeight: "600",
        },
      }}
    >
      <Drawer.Screen
        name="index"
        options={{
          title: "Hope Cards",
          drawerLabel: "Home",
          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="home-outline"
              color={color}
              size={size}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="daily"
        options={{
          title: "Daily Hope",
          drawerLabel: "Daily Hope",
          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="sunny-outline"
              color={color}
              size={size}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="favorites"
        options={{
          title: "Favorites",
          drawerLabel: "Favorites",
          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="heart-outline"
              color={color}
              size={size}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="settings"
        options={{
          title: "Settings",
          drawerLabel: "Settings",
          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="settings-outline"
              color={color}
              size={size}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="about"
        options={{
          title: "About",
          drawerLabel: "About",
          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="information-circle-outline"
              color={color}
              size={size}
            />
          ),
        }}
      />

      <Drawer.Screen
        name="privacy"
        options={{
          title: "Privacy Policy",
          drawerLabel: "Privacy Policy",
          drawerIcon: ({ color, size }) => (
            <Ionicons
              name="shield-checkmark-outline"
              color={color}
              size={size}
            />
          ),
        }}
      />
    </Drawer>
  );
}