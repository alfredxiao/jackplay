package jackplay.web;

import jackplay.PlayBook2;
import jackplay.Player;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;

public class PlayHandler implements HttpHandler {
    Instrumentation inst;
    PlayBook2 playbook;

    public PlayHandler(Instrumentation inst, PlayBook2 playbook) {
        this.inst = inst;
        this.playbook = playbook;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        String response = "jack playing";
        try {
            Player.play(inst, "Greeter", new String[]{"greet", "beautify"});
        } catch (Exception e) {
        }
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}