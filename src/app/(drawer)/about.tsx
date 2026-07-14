import {
  ScrollView,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { useAppTheme } from "../../theme/appTheme";

export default function AboutScreen() {
  const { theme } = useAppTheme();

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
        Hope Cards
      </Text>

      <Text
        style={[
          styles.version,
          {
            color: theme.textSecondary,
          },
        ]}
      >
        Version 1.0.0
      </Text>

      <Text
        style={[
          styles.description,
          { color: theme.cardText },
        ]}
      >
        Hope Cards is a beautifully
        designed Bible verse app that
        helps encourage, strengthen, and
        inspire people through God's Word.
      </Text>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Features
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Draw encouraging Bible verses
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Save your favorite verses
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Read a fresh Daily Hope verse
          each day
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Set daily reminder times
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Share verses with family and
          friends
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Backup and restore your saved
          data
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Gentle haptic feedback
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Clean and distraction-free
          experience
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Developed by
        </Text>

        <Text
          style={[
            styles.body,
            { color: theme.cardText },
          ]}
        >
          Aaronsedna
        </Text>

        <Text
          style={[
            styles.footer,
            {
              color: theme.textSecondary,
            },
          ]}
        >
          Created to encourage people
          through God's Word.
        </Text>
      </View>

      <Text
        style={[
          styles.copyright,
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

  version: {
    marginTop: 6,
    fontSize: 16,
  },

  description: {
    marginTop: 24,
    fontSize: 18,
    lineHeight: 30,
  },

  section: {
    marginTop: 36,
  },

  sectionTitle: {
    fontSize: 22,
    fontWeight: "700",
    marginBottom: 16,
  },

  item: {
    fontSize: 17,
    marginBottom: 14,
    lineHeight: 26,
  },

  body: {
    fontSize: 18,
    lineHeight: 28,
  },

  footer: {
    marginTop: 14,
    fontSize: 16,
    lineHeight: 26,
  },

  copyright: {
    marginTop: 48,
    textAlign: "center",
    fontSize: 15,
  },
});
