package jackplay.bootstrap;

import java.util.*;

// singleton
public class Options {
    final static String OPTION_SPLIT = ",";
    final static char OPTION_EQ_SIGN = '=';
    final static Map<String, String> DEFAULTS = new HashMap<>();
    static Set<String> blacklist;
    static Set<String> whitelist;

    static {
        DEFAULTS.put("port", "8088");
        DEFAULTS.put("logLevel", "info");
        DEFAULTS.put("traceLogLimit", "100");
    }

    public static boolean isEmpty(String s) {
        return null == s || s.isEmpty();
    }

    Map<String, String> options;

    public Options(Map<String, String> options) {
        this.options = options;
    }

    public static Options optionsMergedWithDefaults(String args) {
        return new Options(addDefaults(parseArguments(args)));
    }

    private static Map<String, String> parseArguments(String args) {
        Map<String, String> options = new HashMap<>();
        if (!isEmpty(args)) {
            String[] parts = args.split(OPTION_SPLIT);
            for (String part : parts) {
                int eq = part.indexOf(OPTION_EQ_SIGN);
                String name = part.substring(0, eq).trim();
                String value = part.substring(eq + 1).trim();

                options.put(name, value);
            }
        }

        if (options.get("blacklist") != null && options.get("whitelist") != null) {
            throw new RuntimeException("You cannot set both blacklist and whitelist");
        }

        return options;
    }

    private static Map<String, String> addDefaults(Map<String, String> options) {
        Map<String, String> merged = new HashMap<>();
        merged.putAll(DEFAULTS);
        merged.putAll(options);

        return merged;
    }

    public int port() {
        return Integer.parseInt(options.get("port"));
    }

    public String logLevel() {
        return options.get("logLevel");
    }

    public int traceLogLimit() {
        return Integer.parseInt(options.get("traceLogLimit"));
    }

    public Set<String> whitelist() {
        if (whitelist == null) {
            whitelist = parseSet(options.get("whitelist"));
        }

        return whitelist;
    }

    public Set<String> blacklist() {
        if (blacklist == null) {
            blacklist = parseSet(options.get("blacklist"));
        }

        return blacklist;
    }

    private Set<String> parseSet(String list) {
        Set<String> set = new HashSet<>();
        if (list != null && list.length() > 0) {
            String[] parts = list.split(":");
            set.addAll(Arrays.asList(parts));
        }

        return set;
    }
}
