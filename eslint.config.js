const {
  defineConfig,
  globalIgnores,
} = require("eslint/config");
const expoConfig = require("eslint-config-expo/flat");

module.exports = defineConfig([
  globalIgnores([
    "dist/*",
    "android/*",
    "ios/*",
  ]),
  expoConfig,
  {
    rules: {
      // Reanimated shared values and Expo Audio players expose intentional
      // mutable APIs that this experimental React rule cannot distinguish.
      "react-hooks/immutability": "off",
    },
  },
]);
