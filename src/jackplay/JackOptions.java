package jackplay;

import java.util.*;

public class JackOptions {
    final static String OPTION_SPLIT = ",";
    final static char OPTION_EQ_SIGN = '=';
    final static Map<String, String> DEFAULTS = new HashMap<String, String>();

    static {
        DEFAULTS.put("port", "8080");
        DEFAULTS.put("log", "true");
        DEFAULTS.put("debug", "false");
    }

    public static boolean isEmpty(String s) {
        return null == s || s.isEmpty();
    }

    Map<String, String> options;

    public JackOptions(Map<String, String> options) {
        this.options = options;
    }

    public static JackOptions optionsMergedWithDefaults(String args) {
        return new JackOptions(addDefaults(parseArguments(args)));
    }

    private static Map<String, String> parseArguments(String args) {
        Map<String, String> options = new HashMap<String, String>();
        if (!isEmpty(args)) {
            String[] parts = args.split(OPTION_SPLIT);
            for (String part : parts) {
                int eq = part.indexOf(OPTION_EQ_SIGN);
                String name = part.substring(0, eq).trim();
                String value = part.substring(eq + 1).trim();

                options.put(name, value);
            }
        }

        return options;
    }

    private static Map<String, String> addDefaults(Map<String, String> options) {
        Map<String, String> merged = new HashMap<String, String>();
        merged.putAll(DEFAULTS);
        merged.putAll(options);

        return merged;
    }

    public int port() {
        return Integer.parseInt(options.get("port"));
    }

    public boolean log() {
        return Boolean.parseBoolean(options.get("log"));
    }

    public boolean debug() {
        return Boolean.parseBoolean(options.get("debug"));
    }
}
