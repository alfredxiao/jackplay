package jackplay.bootstrap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

// converts a map, a list, to json
public class JSON {

    public static String objectToJson(Object obj) {
        if (obj instanceof Map) {
            return mapToJson((Map) obj);
        } else if (obj instanceof Object[]) {
            return iterableToJson(Arrays.asList((Object[]) obj));
        } else if (obj instanceof Iterable) {
            return iterableToJson((List) obj);
        } else if (obj instanceof Boolean) {
            return obj.toString();
        } else if (obj instanceof Character) {
            return obj.toString();
        } else if (obj instanceof Number) {
            return obj.toString();
        } else {
            return obj == null ? "null" : escape(obj.toString());
        }
    }

    public static String mapToJson(Map map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean isFirst = true;

        for (Object key : map.keySet()) {
            if (!isFirst) builder.append(',');

            builder.append("\"").append(key.toString()).append("\":").append(objectToJson(map.get(key))).append("");
            isFirst = false;
        }
        builder.append("}");
        return builder.toString();
    }

    public static String iterableToJson(Iterable it) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;

        for (Object obj : it) {
            if (!isFirst) builder.append(',');
            builder.append(objectToJson(obj));
            isFirst = false;
        }
        builder.append("]");
        return builder.toString();
    }

    private static String escape(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

}
