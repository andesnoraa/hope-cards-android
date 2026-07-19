import Ionicons from "@expo/vector-icons/Ionicons";
import Constants from "expo-constants";
import Drawer, {
  DrawerContentScrollView,
  DrawerItem,
  type DrawerContentComponentProps,
} from "expo-router/drawer";
import { StyleSheet, Text, View } from "react-native";

import {
  useAppTheme,
} from "../../theme/appTheme";

const APP_VERSION = Constants.expoConfig?.version ?? "Unknown";

function ThemedDrawerContent({
  state,
  navigation,
  descriptors,
}: DrawerContentComponentProps) {
  const { theme } = useAppTheme();

  return (
    <DrawerContentScrollView
      contentContainerStyle={styles.drawerContent}
    >
      {state.routes.map((route, index) => {
        const focused = state.index === index;
        const options = descriptors[route.key].options;
        const label =
          options.drawerLabel ??
          options.title ??
          route.name;

        return (
          <View key={route.key}>
            <DrawerItem
              route={route}
              label={label}
              icon={options.drawerIcon}
              focused={focused}
              activeTintColor={options.drawerActiveTintColor}
              inactiveTintColor={options.drawerInactiveTintColor}
              activeBackgroundColor={options.drawerActiveBackgroundColor}
              inactiveBackgroundColor={options.drawerInactiveBackgroundColor}
              allowFontScaling={options.drawerAllowFontScaling}
              labelStyle={options.drawerLabelStyle}
              style={options.drawerItemStyle}
              onPress={() => {
                const event = navigation.emit({
                  type: "drawerItemPress",
                  target: route.key,
                  canPreventDefault: true,
                });

                if (event.defaultPrevented) {
                  return;
                }

                if (focused) {
                  navigation.closeDrawer();
                } else {
                  navigation.navigate(route.name, route.params);
                }
              }}
            />

            {(route.name === "premium" || route.name === "settings") && (
              <View
                style={[
                  styles.drawerDivider,
                  { backgroundColor: theme.divider },
                ]}
              />
            )}
          </View>
        );
      })}

      <View style={styles.drawerFooter}>
        <Text
          style={[
            styles.drawerFooterTitle,
            { color: theme.textSecondary },
          ]}
        >
          Hope Cards
        </Text>
        <Text
          style={[
            styles.drawerFooterVersion,
            { color: theme.textTertiary },
          ]}
        >
          Version {APP_VERSION}
        </Text>
      </View>
    </DrawerContentScrollView>
  );
}

export default function DrawerLayout() {
  const { theme } = useAppTheme();

  return (
    <Drawer
      drawerContent={(props) => (
        <ThemedDrawerContent {...props} />
      )}
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
          backgroundColor: theme.homeBackground,
          borderRightColor: theme.divider,
          borderRightWidth: StyleSheet.hairlineWidth,
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

const styles = StyleSheet.create({
  drawerContent: {
    flexGrow: 1,
  },
  drawerDivider: {
    height: StyleSheet.hairlineWidth,
    marginHorizontal: 24,
    marginVertical: 6,
  },
  drawerFooter: {
    marginTop: "auto",
    marginHorizontal: 24,
    paddingTop: 24,
    paddingBottom: 8,
  },
  drawerFooterTitle: {
    fontSize: 13,
    lineHeight: 18,
    fontWeight: "600",
  },
  drawerFooterVersion: {
    marginTop: 2,
    fontSize: 11,
    lineHeight: 16,
    fontWeight: "500",
  },
});
