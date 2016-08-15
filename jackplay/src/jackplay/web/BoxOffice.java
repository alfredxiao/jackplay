package jackplay.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.*;
import jackplay.bootstrap.Options;
import jackplay.play.InfoCenter;
import jackplay.play.ProgramManager;

import javax.net.ssl.*;

// singleton
public class BoxOffice extends Thread {
    Options options;
    ProgramManager pm;
    InfoCenter infoCenter;
    Map<String, HttpHandler> contextMap;

    public void init(Options options, ProgramManager pm, InfoCenter infoCenter) {
        this.options = options;
        this.pm = pm;
        this.infoCenter = infoCenter;

        super.setDaemon(true);
        this.initContextMap();
    }

    private void initContextMap() {
        this.contextMap = new HashMap<>();
        this.contextMap.put("/", new RootHandler());
        this.contextMap.put("/program", new ProgramHandler(this.pm, this.infoCenter));
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
        } catch(IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    private HttpServer createHttpServer() throws IOException {
        if (options.https()) {
            HttpsServer server = HttpsServer.create(new InetSocketAddress(options.port()), 0);
            server.setHttpsConfigurator(getHttpsConfigurator(getSSLContext()));

            return server;
        } else {
            return HttpServer.create(new InetSocketAddress(options.port()), 0);
        }
    }

    private SSLContext getSSLContext() {
        try{
            char[] passphrase = options.keystorePassword().toCharArray();
            KeyStore ks=KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(options.keystoreFilepath()),
                    passphrase);

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
