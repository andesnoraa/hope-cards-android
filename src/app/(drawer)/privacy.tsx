import {
  Linking,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

import { useAppTheme } from "../../theme/appTheme";

const PRIVACY_POLICY_URL =
  "https://hope-cards-web.vercel.app/privacy";
const REVENUECAT_PRIVACY_URL =
  "https://www.revenuecat.com/privacy";
const GOOGLE_PRIVACY_URL =
  "https://policies.google.com/privacy";

type PolicySectionProps = {
  title: string;
  children: React.ReactNode;
  textColor: string;
  dividerColor: string;
};

export default function PrivacyPolicyScreen() {
  const { theme } = useAppTheme();

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: theme.background }]}
      contentContainerStyle={styles.content}
      contentInsetAdjustmentBehavior="automatic"
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.hero}>
        <Text style={[styles.title, { color: theme.text }]}>Your privacy matters</Text>
        <Text style={[styles.summary, { color: theme.textSecondary }]}>Hope Cards is designed to keep your verses and preferences on your device. It does not require an account and does not use advertising or analytics.</Text>
        <Text style={[styles.updated, { color: theme.textTertiary }]}>Effective July 19, 2026</Text>
      </View>

      <PolicySection
        title="Information We Collect"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>Hope Cards does not ask for your name, email address, phone number, contacts, precise location, photos, or microphone access.</Body>
        <Body color={theme.cardText}>To provide and verify premium access, limited purchase and technical information is processed as described under “Premium Purchases.”</Body>
      </PolicySection>

      <PolicySection
        title="Data Stored on Your Device"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>Your favorites, selected Bible translation, theme, reminder time, music preference, haptic preference, and other app settings are stored locally on your device.</Body>
        <Body color={theme.cardText}>Removing the app may remove this data unless you have exported a backup.</Body>
      </PolicySection>

      <PolicySection
        title="Reminders and Permissions"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>If you enable Daily Hope reminders, the app requests notification permission and schedules reminders on your device. Hope Cards does not use your contacts, location, camera, photos, or microphone.</Body>
        <Body color={theme.cardText}>Background music is included with the app and does not require microphone access.</Body>
      </PolicySection>

      <PolicySection
        title="Sharing and Backups"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>Sharing a verse or backup opens your device’s sharing controls. Hope Cards does not record who you share with or which service you choose.</Body>
        <Body color={theme.cardText}>Backup files contain your favorites and app settings. They are created locally. If you send or save a backup through another app or service, that provider’s privacy practices apply.</Body>
      </PolicySection>

      <PolicySection
        title="Premium Purchases"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>Premium access is processed by Google Play and managed with RevenueCat. These services may process an anonymous app identifier, purchase history, subscription status, purchase tokens, and basic device or app information to offer purchases, verify access, and restore purchases.</Body>
        <Body color={theme.cardText}>Hope Cards does not receive or store your full payment card details.</Body>
        <PolicyLink
          label="RevenueCat Privacy Policy"
          url={REVENUECAT_PRIVACY_URL}
          color={theme.accent}
        />
        <PolicyLink
          label="Google Privacy Policy"
          url={GOOGLE_PRIVACY_URL}
          color={theme.accent}
        />
      </PolicySection>

      <PolicySection
        title="Analytics and Advertising"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>Hope Cards does not include advertising or analytics services and does not sell your personal information.</Body>
      </PolicySection>

      <PolicySection
        title="Children’s Privacy"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>Hope Cards is intended for a general audience. We do not knowingly collect personal information directly from children.</Body>
      </PolicySection>

      <PolicySection
        title="Policy Updates"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>We may update this policy when the app or its services change. The effective date above will show when the policy was last revised.</Body>
      </PolicySection>

      <PolicySection
        title="Contact"
        textColor={theme.text}
        dividerColor={theme.divider}
      >
        <Body color={theme.cardText}>For privacy questions, corrections, or requests, contact the developer through the Hope Cards website.</Body>
        <PolicyLink
          label="View the privacy policy online"
          url={PRIVACY_POLICY_URL}
          color={theme.accent}
        />
      </PolicySection>

      <Text style={[styles.footer, { color: theme.textTertiary }]}>© 2026 Hope Cards · All rights reserved</Text>
    </ScrollView>
  );
}

function PolicySection({
  title,
  children,
  textColor,
  dividerColor,
}: PolicySectionProps) {
  return (
    <View style={[styles.section, { borderTopColor: dividerColor }]}>
      <Text style={[styles.sectionTitle, { color: textColor }]}>{title}</Text>
      <View style={styles.sectionContent}>{children}</View>
    </View>
  );
}

function Body({
  children,
  color,
}: {
  children: React.ReactNode;
  color: string;
}) {
  return <Text style={[styles.body, { color }]}>{children}</Text>;
}

function PolicyLink({
  label,
  url,
  color,
}: {
  label: string;
  url: string;
  color: string;
}) {
  return (
    <Pressable
      accessibilityRole="link"
      accessibilityLabel={label}
      onPress={() => void Linking.openURL(url)}
      style={({ pressed }) => [styles.linkButton, pressed && styles.linkPressed]}
    >
      <Text style={[styles.link, { color }]}>{label}</Text>
      <Text style={[styles.linkArrow, { color }]} accessibilityElementsHidden>↗</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { paddingHorizontal: 24, paddingTop: 14, paddingBottom: 42 },
  hero: { paddingTop: 18, paddingBottom: 30 },
  title: { fontSize: 32, lineHeight: 38, fontWeight: "700", letterSpacing: -0.6 },
  summary: { marginTop: 12, fontSize: 17, lineHeight: 26 },
  updated: { marginTop: 14, fontSize: 13, lineHeight: 18, fontWeight: "500" },
  section: { borderTopWidth: StyleSheet.hairlineWidth, paddingVertical: 32 },
  sectionTitle: { fontSize: 22, lineHeight: 28, fontWeight: "700", letterSpacing: -0.35 },
  sectionContent: { marginTop: 14, gap: 12 },
  body: { fontSize: 15, lineHeight: 23 },
  linkButton: { minHeight: 32, flexDirection: "row", alignItems: "center", alignSelf: "flex-start", gap: 6 },
  linkPressed: { opacity: 0.55 },
  link: { fontSize: 15, lineHeight: 22, fontWeight: "600" },
  linkArrow: { fontSize: 14, lineHeight: 20, fontWeight: "600" },
  footer: { paddingTop: 4, textAlign: "center", fontSize: 11, lineHeight: 16 },
});
