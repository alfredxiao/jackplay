package jackplay.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.*;
import jackplay.Logger;
import jackplay.model.Options;
import jackplay.core.InfoCenter;
import jackplay.core.Jack;

import javax.net.ssl.*;

// singleton
public class BoxOffice extends Thread {
    Options options;
    Jack jack;
    InfoCenter infoCenter;
    Map<String, HttpHandler> contextMap;
    private static int BACKLOG = 50;

    public BoxOffice(Options options, Jack jack, InfoCenter infoCenter) {
        this.options = options;
        this.jack = jack;
        this.infoCenter = infoCenter;

        super.setDaemon(true);
        this.initContextMap();
    }

    private void initContextMap() {
        this.contextMap = new HashMap<>();
        this.contextMap.put("/", new RootHandler());
        this.contextMap.put("/program", new ProgramHandler(this.jack));
        this.contextMap.put("/info", new InfoHandler(infoCenter));
    }

    public void run() {
        try {
            HttpServer server = this.createHttpServer();

            for (String contextPath : contextMap.keySet()) {
                HttpContext context = server.createContext(contextPath, contextMap.get(contextPath));
                context.getFilters().add(new ParameterFilter());
            }
            server.start();

            Logger.info("web-server", "Jackplay web server has now started.");
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    private HttpServer createHttpServer() throws IOException {
        InetSocketAddress address = new InetSocketAddress(options.port());
        if (options.https()) {
            HttpsServer server = HttpsServer.create(address, BACKLOG);
            server.setHttpsConfigurator(getHttpsConfigurator(getSSLContext()));

            return server;
        } else {
            return HttpServer.create(address, BACKLOG);
        }
    }

    private SSLContext getSSLContext() {
        String optionPassword = options.keystorePassword();
        String password = (null == optionPassword || optionPassword.length() == 0)
                          ? "jackplay-demo"
                          : optionPassword;

        String optionPath = options.keystoreFilepath();
        try{
            char[] passphrase = password.toCharArray();

            InputStream storeStream = (null == optionPath || optionPath.length() == 0)
                    ? BoxOffice.class.getResourceAsStream("/jackplay-demo.jks")
                    : new FileInputStream(optionPath);

            KeyStore ks=KeyStore.getInstance("JKS");
            ks.load(storeStream, passphrase);

            KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks,passphrase);

            TrustManagerFactory tmf=TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext ssl=SSLContext.getInstance("TLS");
            ssl.init(kmf.getKeyManagers(),tmf.getTrustManagers(),null);

            return ssl;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    private HttpsConfigurator getHttpsConfigurator(SSLContext ssl) {
        return new HttpsConfigurator(ssl) {
            public void configure(HttpsParameters params) {
                try {
                    // initialise the SSL context
                    SSLContext c = SSLContext.getDefault();
                    SSLEngine engine = c.createSSLEngine();
                    params.setNeedClientAuth(false);
                    params.setCipherSuites(engine.getEnabledCipherSuites());
                    params.setProtocols(engine.getEnabledProtocols());

                    // get the default parameters
                    SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                    params.setSSLParameters(defaultSSLParameters);

                } catch (Exception ex) {
                    throw new RuntimeException("Failed to create HTTPS port");
                }
            }
        };
    }
}
