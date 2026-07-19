import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import Constants from "expo-constants";

import {
  TRANSLATION_OPTIONS,
} from "../../services/verseService";
import { useAppTheme } from "../../theme/appTheme";

const FEATURES = [
  "Draw verse cards",
  "Daily Hope",
  "Save favorites",
  "Share verses",
  "Daily reminders",
  "Multiple translations",
  "Custom themes",
  "Background music",
  "Haptic feedback",
  "Backup & restore",
] as const;

const APP_VERSION = Constants.expoConfig?.version ?? "Unknown";

export default function AboutScreen() {
  const { theme } = useAppTheme();

  const sectionStyle = {
    borderColor: theme.divider,
  };

  return (
    <ScrollView
      style={[
        styles.container,
        { backgroundColor: theme.background },
      ]}
      contentContainerStyle={styles.content}
      contentInsetAdjustmentBehavior="automatic"
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.hero}>
        <Text style={[styles.title, { color: theme.text }]}>Hope Cards</Text>
        <Text style={[styles.tagline, { color: theme.textSecondary }]}>Discover, save, and share encouraging Bible verses.</Text>
        <Text style={[styles.version, { color: theme.textTertiary }]}>Version {APP_VERSION}</Text>
      </View>

      <View style={[styles.section, sectionStyle]}>
        <SectionTitle
          title="Features"
          text={theme.text}
        />

        <View style={styles.featureGrid}>
          {FEATURES.map((label) => (
            <View
              key={label}
              style={styles.featureTile}
            >
              <View
                style={[
                  styles.featureBullet,
                  { backgroundColor: theme.accent },
                ]}
              />
              <Text style={[styles.featureLabel, { color: theme.cardText }]}>{label}</Text>
            </View>
          ))}
        </View>
      </View>

      <View style={[styles.section, sectionStyle]}>
        <SectionTitle
          title="Why Hope Cards"
          text={theme.text}
        />

        <View style={styles.purposePanel}>
          <View style={[styles.purposeAccent, { backgroundColor: theme.accent }]} />
          <View style={styles.purposeCopy}>
            <Text style={[styles.purposeText, { color: theme.cardText }]}>Hope Cards was created during a difficult season when Scripture brought strength.</Text>
            <Text style={[styles.purposeVision, { color: theme.textSecondary }]}>The vision is simple: to help someone facing a difficult situation find a timely verse, a quiet reminder, and renewed hope.</Text>
          </View>
        </View>
      </View>

      <View style={[styles.section, sectionStyle]}>
        <SectionTitle
          title="Credits"
          text={theme.text}
        />

        <View style={styles.creditList}>
          {TRANSLATION_OPTIONS.map((translation) => (
            <View
              key={translation.id}
              style={[styles.creditRow, { borderBottomColor: theme.divider }]}
            >
              <Text
                adjustsFontSizeToFit
                minimumFontScale={0.85}
                numberOfLines={1}
                style={[styles.creditCode, { color: theme.accent }]}
              >
                {translation.label}
              </Text>
              <View style={styles.creditCopy}>
                <Text style={[styles.creditDetails, { color: theme.cardText }]}>{translation.attribution}</Text>
                <Text style={[styles.license, { color: theme.textSecondary }]}>{translation.license}</Text>
              </View>
            </View>
          ))}
        </View>
      </View>

      <View
        style={[
          styles.signature,
          { borderTopColor: theme.divider },
        ]}
      >
        <Text style={[styles.signatureLabel, { color: theme.textTertiary }]}>
          Developed with <Text style={{ color: theme.accent }}>♥</Text> by <Text style={[styles.signatureName, { color: theme.text }]}>Aaronsedna</Text>
        </Text>
      </View>

      <Text style={[styles.copyright, { color: theme.textTertiary }]}>© 2026 Hope Cards · All rights reserved</Text>
    </ScrollView>
  );
}

function SectionTitle({
  title,
  text,
}: {
  title: string;
  text: string;
}) {
  return (
    <View style={styles.sectionTitleRow}>
      <Text style={[styles.sectionTitle, { color: text }]}>{title}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1 },
  content: { paddingHorizontal: 24, paddingTop: 14, paddingBottom: 16 },
  hero: { alignItems: "center", paddingTop: 24, paddingBottom: 30, paddingHorizontal: 8 },
  title: { fontSize: 36, lineHeight: 43, fontWeight: "700", letterSpacing: -0.7, textAlign: "center" },
  tagline: { marginTop: 9, maxWidth: 300, fontSize: 17, lineHeight: 25, fontWeight: "400", textAlign: "center" },
  version: { marginTop: 14, fontSize: 13, lineHeight: 18, fontWeight: "500" },
  section: { borderTopWidth: StyleSheet.hairlineWidth, paddingVertical: 32 },
  sectionTitleRow: { flexDirection: "row", alignItems: "center", marginBottom: 24 },
  sectionTitle: { flex: 1, fontSize: 22, lineHeight: 28, fontWeight: "700", letterSpacing: -0.35 },
  featureGrid: { flexDirection: "row", flexWrap: "wrap", rowGap: 21, columnGap: 16 },
  featureTile: { width: "47.4%", minHeight: 30, flexDirection: "row", alignItems: "flex-start", gap: 10 },
  featureBullet: { width: 6, height: 6, borderRadius: 3, marginTop: 8 },
  featureLabel: { flex: 1, fontSize: 14, lineHeight: 20, fontWeight: "500" },
  purposePanel: { position: "relative", paddingLeft: 20 },
  purposeAccent: { position: "absolute", left: 0, top: 6, bottom: 6, width: 2 },
  purposeCopy: { flex: 1 },
  purposeText: { fontSize: 16, lineHeight: 25, fontWeight: "500", letterSpacing: -0.1 },
  purposeVision: { marginTop: 12, fontSize: 14, lineHeight: 21, fontWeight: "400" },
  creditList: {},
  creditRow: { minHeight: 58, flexDirection: "row", alignItems: "flex-start", gap: 12, borderBottomWidth: StyleSheet.hairlineWidth, paddingVertical: 10 },
  creditCode: { width: 52, fontSize: 14, lineHeight: 20, fontWeight: "700", letterSpacing: 0.2 },
  creditCopy: { flex: 1, gap: 1 },
  creditDetails: { fontSize: 14, lineHeight: 20, fontWeight: "500" },
  license: { fontSize: 12, lineHeight: 17, fontWeight: "400" },
  signature: { alignItems: "center", borderTopWidth: StyleSheet.hairlineWidth, paddingTop: 16 },
  signatureLabel: { fontSize: 13, lineHeight: 18, fontWeight: "400" },
  signatureName: { fontWeight: "600" },
  copyright: { marginTop: 12, textAlign: "center", fontSize: 11, lineHeight: 16 },
});
