import { Drawer } from "expo-router/drawer";

export default function DrawerLayout() {
  return (
    <Drawer
      screenOptions={{
        headerShown: true,

        headerTitle: "Hope Cards",

        drawerActiveTintColor: "#C5A24C",
        drawerInactiveTintColor: "#273043",

        drawerLabelStyle: {
          fontSize: 16,
          fontWeight: "600",
        },
      }}
    >
      <Drawer.Screen
        name="index"
        options={{
          drawerLabel: "🏠 Home",
          title: "Home",
        }}
      />

      <Drawer.Screen
        name="favorites"
        options={{
          drawerLabel: "❤️ Favorites",
          title: "Favorites",
        }}
      />

      <Drawer.Screen
        name="daily"
        options={{
          drawerLabel: "☀ Daily Verse",
          title: "Daily Verse",
        }}
      />

      <Drawer.Screen
        name="premium"
        options={{
          drawerLabel: "👑 Premium",
          title: "Premium",
        }}
      />

      <Drawer.Screen
        name="settings"
        options={{
          drawerLabel: "⚙️ Settings",
          title: "Settings",
        }}
      />

      <Drawer.Screen
        name="about"
        options={{
          drawerLabel: "ℹ️ About",
          title: "About",
        }}
      />

      <Drawer.Screen
        name="privacy"
        options={{
          drawerLabel: "📜 Privacy Policy",
          title: "Privacy Policy",
        }}
      />
    </Drawer>
  );
}