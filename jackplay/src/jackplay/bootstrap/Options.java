package jackplay.bootstrap;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

// singleton
public class Options {
    final static String OPTION_SEPARATOR = ";";
    final static char EQUALS_CHAR = '=';
    final static Map<String, String> DEFAULTS = new HashMap<>();
    Map<String, String> options;
    Set<String> blacklist;
    Set<String> whitelist;
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        DEFAULTS.put("port", "8181");
        DEFAULTS.put("logLevel", "info");
        DEFAULTS.put("traceLogLimit", "300");
        DEFAULTS.put("autoSuggestLimit", "100");
        DEFAULTS.put("https", "false");
        DEFAULTS.put("defaultTrace", "");
        DEFAULTS.put("intervalSyncTraceLogs", "5");
        DEFAULTS.put("intervalSyncModifiableMethods", "120");
        DEFAULTS.put("traceStringLength", "36");
        DEFAULTS.put("traceArrayLength", "3");
    }

    private static boolean isEmpty(String s) {
        return null == s || s.isEmpty();
    }

    private Options(Map<String, String> options) {
        this.options = options;
    }

    public static Options optionsMergedWithDefaults(String args) {
        return new Options(addDefaults(parseArguments(args)));
    }

    private static Map<String, String> parseArguments(String args) {
        Map<String, String> options = new HashMap<>();
        if (!isEmpty(args)) {
            String[] parts = args.split(OPTION_SEPARATOR);
            for (String part : parts) {
                int eq = part.indexOf(EQUALS_CHAR);
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

    static String formatDate(Date when) {
        return SIMPLE_DATE_FORMAT.format(when);
    }

    public static String formateNow() {
        return SIMPLE_DATE_FORMAT.format(new java.util.Date());
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

    public int autoSuggestLimit() {
        return Integer.parseInt(options.get("autoSuggestLimit"));
    }

    public int intervalSyncTraceLogs() {
        return Integer.parseInt(options.get("intervalSyncTraceLogs"));
    }

    public int intervalSyncModifiableMethods() {
        return Integer.parseInt(options.get("intervalSyncModifiableMethods"));
    }

    public boolean https() {
        return Boolean.parseBoolean(options.get("https"));
    }

    public String keystorePassword() {
        return options.get("keystorePassword");
    }

    public String keystoreFilepath() {
        return options.get("keystoreFilepath");
    }

    public String defaultTrace() {
        return options.get("defaultTrace");
    }

    public String logFile() {
        return options.get("logFile");
    }
    public String traceLogFile() {
        return options.get("traceLogFile");
    }

    public int traceStringLength() {
        return Integer.parseInt(options.get("traceStringLength"));
    }

    public int traceArrayLength() {
        return Integer.parseInt(options.get("traceArrayLength"));
    }

    public String[] defaultTraceAsArray() {
        String defaultTrace = this.defaultTrace();
        return (defaultTrace == null || defaultTrace.trim().length() == 0) ? null : defaultTrace.trim().split(":");
    }

    public void updateOption(String key, String value) {
        if (value != null && value.trim().length() > 0) {
            options.put(key, value.trim());
        }
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

    private static boolean matchesPackageName(String pattern, String packageName) {
        return Pattern.matches(pattern, packageName);
    }

    public boolean packageAllowed(String packageName) {
        if (packageName == null) return true;

        if ("java.lang".equals(packageName) ||
                "jackplay".equals(packageName) ||
                packageName.startsWith("jackplay.") ||
                packageName.startsWith("java.lang.")) {
            return false;
        }

        Set<String> blacklist = this.blacklist();
        Set<String> whitelist = this.whitelist();

        if (!blacklist.isEmpty()) {
            for (String black : blacklist) {
                if (matchesPackageName(black, packageName)) return false;
            }

            return true;
        } else if (!whitelist.isEmpty()) {
            for (String white : whitelist) {
                if (matchesPackageName(white, packageName)) return true;
            }

            return false;
        }

        return true;
    }
}
