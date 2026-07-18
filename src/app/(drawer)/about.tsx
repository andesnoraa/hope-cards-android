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
        Hope Cards helps you find Bible
        verses for daily encouragement.
        Draw a card, save favorite verses,
        and share hope with others.
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
          • Draw encouraging verse cards
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Save verses that speak to you
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Begin each day with Daily Hope
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Set reminders for quiet moments
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Share hope with family and
          friends
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Choose calming visual themes
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Back up and restore your saved
          verses and settings
        </Text>

        <Text
          style={[
            styles.item,
            { color: theme.cardText },
          ]}
        >
          • Read in a clean,
          distraction-free space
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Why Hope Cards Exists
        </Text>

        <Text
          style={[
            styles.body,
            { color: theme.cardText },
          ]}
        >
          Hope Cards was created during a
          difficult season when Scripture
          brought encouragement and
          strength.
        </Text>

        <Text
          style={[
            styles.body,
            styles.bodySpacing,
            { color: theme.cardText },
          ]}
        >
          The vision is simple: to help
          someone facing a difficult
          situation find a timely verse, a
          quiet reminder, and renewed hope.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Bible Translation
        </Text>

        <Text
          style={[
            styles.body,
            { color: theme.cardText },
          ]}
        >
          Hope Cards uses the Bible in
          Basic English (BBE), prepared by
          Professor S. H. Hooke.
        </Text>

        <Text
          style={[
            styles.body,
            styles.bodySpacing,
            { color: theme.cardText },
          ]}
        >
          The BBE uses a limited Basic
          English vocabulary to make
          Scripture easier to read. The New
          Testament was released in 1941,
          the Old Testament in 1949, and
          the revised edition in 1965.
        </Text>
      </View>

      <View style={styles.section}>
        <Text
          style={[
            styles.sectionTitle,
            { color: theme.text },
          ]}
        >
          Copyright
        </Text>

        <Text
          style={[
            styles.body,
            { color: theme.cardText },
          ]}
        >
          The verses in Hope Cards are from
          the Bible in Basic English (BBE).
        </Text>

        <Text
          style={[
            styles.body,
            styles.bodySpacing,
            { color: theme.cardText },
          ]}
        >
          Bible text: Bible in Basic
          English (BBE){"\n"}
          Translator: Professor S. H.
          Hooke{"\n"}
          Status: Public domain
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

  bodySpacing: {
    marginTop: 14,
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
