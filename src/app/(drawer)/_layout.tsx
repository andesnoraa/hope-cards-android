import Ionicons from "@expo/vector-icons/Ionicons";
import Drawer from "expo-router/drawer";

export default function DrawerLayout() {
  return (
    <Drawer
      screenOptions={{
        headerShown: true,

        headerTitle: "Hope Cards",

        headerTitleStyle: {
          fontSize: 24,
          fontWeight: "700",
          color: "#1A2747",
          letterSpacing: 0.3,
        },

        headerStyle: {
          backgroundColor: "#F8F6F2",
        },

        headerShadowVisible: false,

        headerTintColor: "#1A2747",

        sceneStyle: {
          backgroundColor: "#F8F6F2",
        },

        drawerStyle: {
          backgroundColor: "#F8F6F2",
          width: 305,
        },

        drawerActiveTintColor: "#C89B3C",
        drawerInactiveTintColor: "#273043",

        drawerActiveBackgroundColor: "#F5EAC8",

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
          drawerLabel: "Home",
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
        name="premium"
        options={{
          title: "Premium",
          drawerLabel: "Premium",
          drawerIcon: ({ color }) => (
            <Ionicons
              name="diamond-outline"
              color={color}
              size={24}
            />
          ),
        }}
      />
    </Drawer>
  );
}