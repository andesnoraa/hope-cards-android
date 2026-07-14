import {
  Linking,
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { useAppTheme } from "../../theme/appTheme";

export default function PrivacyPolicyScreen() {
  const { theme } = useAppTheme();

  function openPrivacyPolicy() {
    Linking.openURL(
      "https://hope-cards-web.vercel.app/privacy"
    );
  }

  return (
    <ScrollView
      style={[
        styles.container,
        {
          backgroundColor:
            theme.background,
        },
      ]}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      <Text
        style={[
          styles.title,
          { color: theme.text },
        ]}
      >
        Privacy Policy
      </Text>

      <Text
        style={[
          styles.updated,
          {
            color: theme.textSecondary,
          },
        ]}
      >
        Last updated: July 2026
      </Text>

      <Text
        style={[
          styles.description,
          { color: theme.cardText },
        ]}
      >
        Hope Cards respects your privacy.
        We are committed to protecting your
        personal information and providing
        a safe, secure experience.
      </Text>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Information We Collect
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          Hope Cards does not collect,
          store, or transmit personal
          information such as your name,
          email address, phone number,
          location, contacts, or device
          identifiers.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Local Storage
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          Favorites and app preferences,
          including settings such as haptic
          feedback and the Draw Card button,
          are stored only on your device
          using local storage. This
          information never leaves your
          device.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Sharing Bible Verses
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          When you choose to share a Bible
          verse, Hope Cards uses your
          device's built-in sharing menu.
          The app does not receive, store,
          or track any information about
          what you share or whom you share
          it with.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Third-Party Services
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          Hope Cards does not use
          advertising services, analytics
          platforms, or third-party SDKs
          that collect personal
          information.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Children's Privacy
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          Hope Cards is suitable for users
          of all ages and does not
          knowingly collect personal
          information from children or
          adults.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Changes to This Privacy Policy
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          We may update this Privacy Policy
          from time to time. Any changes
          will be posted on this page and
          on our website.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Privacy Policy Website
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          A copy of this Privacy Policy is
          also available on our website:
        </Text>

        <TouchableOpacity
          onPress={openPrivacyPolicy}
          activeOpacity={0.7}
        >
          <Text
            style={[
              styles.link,
              { color: theme.accent },
            ]}
          >
            https://hope-cards-web.vercel.app/privacy
          </Text>
        </TouchableOpacity>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Contact
        </Text>

        <Text style={[styles.body, { color: theme.cardText }]}>
          If you have any questions about
          this Privacy Policy, please visit
          our website or contact the
          developer through the Hope Cards
          website.
        </Text>
      </View>

      <Text
        style={[
          styles.footer,
          { color: theme.textTertiary },
        ]}
      >
        © 2026 Hope Cards. All rights
        reserved.
      </Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  content: {
    padding: 24,
    paddingBottom: 40,
  },

  title: {
    fontSize: 34,
    fontWeight: "700",
  },

  updated: {
    marginTop: 6,
    fontSize: 16,
  },

  description: {
    marginTop: 24,
    fontSize: 18,
    lineHeight: 30,
  },

  section: {
    marginTop: 34,
  },

  sectionTitle: {
    fontSize: 22,
    fontWeight: "700",
    marginBottom: 14,
  },

  body: {
    fontSize: 17,
    lineHeight: 28,
  },

  link: {
    marginTop: 12,
    fontSize: 17,
    fontWeight: "600",
    textDecorationLine: "underline",
  },

  footer: {
    marginTop: 48,
    textAlign: "center",
    fontSize: 15,
  },
});
