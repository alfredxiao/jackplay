package jackplay.web;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebUtils {
    public static Map<String,String> parseParams(URI requestURI) {
        Map<String, String> params = new HashMap<String, String>();
        String query = requestURI.getRawQuery();
        String[] paramParts = query.split("&");
        for (String part : paramParts) {
            int eq = part.indexOf('=');
            if (eq > 0) {
                params.put(part.substring(0, eq), part.substring(eq + 1));
            }
        }
        return params;
    }
}
