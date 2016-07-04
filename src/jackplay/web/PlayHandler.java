package jackplay.web;

import jackplay.JackLogger;
import jackplay.Player;
import static jackplay.PlayCategory.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class PlayHandler implements HttpHandler {
    Instrumentation inst;
    Player player;

    public PlayHandler(Instrumentation inst, Player player) {
        this.inst = inst;
        this.player = player;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"post".equalsIgnoreCase(t.getRequestMethod())) {
            CommonHandling.error_404(t);
            return;
        }

        String className = "Greeter";
        String[] methodNames = new String[] {"beautify"};
        try {
            player.play(MethodLogging, className, methodNames);
            CommonHandling.serveStringBody(t, 200, "OK");
        } catch (Exception e) {
            JackLogger.error(e);
            CommonHandling.serveStringBody(t, 500, e.getMessage());
        }
    }
}