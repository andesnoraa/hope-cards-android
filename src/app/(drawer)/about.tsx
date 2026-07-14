import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";

export default function AboutScreen() {
  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      showsVerticalScrollIndicator={false}
    >
      <Text style={styles.title}>
        Hope Cards
      </Text>

      <Text style={styles.version}>
        Version 1.0.0
      </Text>

      <Text style={styles.description}>
        Hope Cards is a beautifully
        designed Bible verse app that
        helps encourage, strengthen, and
        inspire people through God's Word.
      </Text>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>
          Features
        </Text>

        <Text style={styles.item}>
          • Draw encouraging Bible verses
        </Text>

        <Text style={styles.item}>
          • Save your favorite verses
        </Text>

        <Text style={styles.item}>
          • Read a fresh Daily Hope verse
          each day
        </Text>

        <Text style={styles.item}>
          • Set daily reminder times
        </Text>

        <Text style={styles.item}>
          • Share verses with family and
          friends
        </Text>

        <Text style={styles.item}>
          • Backup and restore your saved
          data
        </Text>

        <Text style={styles.item}>
          • Gentle haptic feedback
        </Text>

        <Text style={styles.item}>
          • Clean and distraction-free
          experience
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>
          Developed by
        </Text>

        <Text style={styles.body}>
          Aaronsedna
        </Text>

        <Text style={styles.footer}>
          Created to encourage people
          through God's Word.
        </Text>
      </View>

      <Text style={styles.copyright}>
        © 2026 Hope Cards. All rights
        reserved.
      </Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8F6F2",
  },

  content: {
    padding: 24,
    paddingBottom: 40,
  },

  title: {
    fontSize: 34,
    fontWeight: "700",
    color: "#1A2747",
  },

  version: {
    marginTop: 6,
    fontSize: 16,
    color: "#777",
  },

  description: {
    marginTop: 24,
    fontSize: 18,
    lineHeight: 30,
    color: "#333",
  },

  section: {
    marginTop: 36,
  },

  sectionTitle: {
    fontSize: 22,
    fontWeight: "700",
    color: "#1A2747",
    marginBottom: 16,
  },

  item: {
    fontSize: 17,
    color: "#333",
    marginBottom: 14,
    lineHeight: 26,
  },

  body: {
    fontSize: 18,
    color: "#333",
    lineHeight: 28,
  },

  footer: {
    marginTop: 14,
    fontSize: 16,
    color: "#666",
    lineHeight: 26,
  },

  copyright: {
    marginTop: 48,
    textAlign: "center",
    fontSize: 15,
    color: "#999",
  },
});
