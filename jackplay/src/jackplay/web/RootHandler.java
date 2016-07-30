package jackplay.web;

import com.sun.net.httpserver.HttpExchange;

import java.util.Map;

public class RootHandler extends BaseHandler {

    @Override
    void process(HttpExchange http, String uri, Map<String, String> params) throws Exception {
        if (uri.isEmpty() || "/".equals(uri)) uri = "/index.html";

        CommonHandling.serveStaticResource(http, 200, uri);
    }
}