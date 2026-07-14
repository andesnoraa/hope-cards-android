import { StyleSheet } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import DrawCard from "../../components/cards/DrawCard";
import { useAppTheme } from "../../theme/appTheme";

export default function HomeScreen() {
  const { theme } = useAppTheme();

  return (
    <SafeAreaView
      style={[
        styles.container,
        {
          backgroundColor:
            theme.homeBackground,
        },
      ]}
    >
      <DrawCard />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 24,
  },
});
