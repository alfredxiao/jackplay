package unit.jackplay.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.core.Is.*;

import jackplay.model.Options;
import org.junit.BeforeClass;
import org.junit.Test;

public class OptionsTest {

    @BeforeClass
    public static void setup() {
    }

    @Test
    public void testDefaults() {
        Options defaultOptions = Options.asOptions("");

        assertEquals(8181, defaultOptions.port());
        assertEquals("info", defaultOptions.logLevel());
        assertEquals(300, defaultOptions.traceLogLimit());
        assertEquals(100, defaultOptions.autoSuggestLimit());
        assertEquals(false, defaultOptions.https());
        assertEquals("", defaultOptions.defaultTrace());
        assertNull(defaultOptions.defaultTraceAsArray());
        assertThat(defaultOptions.whitelist().size(), is(0));
        assertThat(defaultOptions.blacklist().size(), is(0));
        assertNull(defaultOptions.keystorePassword());
        assertNull(defaultOptions.keystoreFilepath());

        // built-in
        assertFalse(defaultOptions.packageAllowed("java.lang"));
        assertFalse(defaultOptions.packageAllowed("jackplay"));
        assertFalse(defaultOptions.packageAllowed("jackplay.web"));

        // any package
        assertTrue(defaultOptions.packageAllowed("java.util"));
    }

    @Test
    public void testWhitelist() {
        Options whitelistOptions = Options.asOptions("whitelist=java.net:my.utils");

        assertThat(whitelistOptions.whitelist().size(), is(2));
        assertTrue(whitelistOptions.whitelist().contains("java.net"));
        assertTrue(whitelistOptions.whitelist().contains("my.utils"));

        // built-in
        assertFalse(whitelistOptions.packageAllowed("java.lang"));
        assertFalse(whitelistOptions.packageAllowed("jackplay"));
        assertFalse(whitelistOptions.packageAllowed("jackplay.web"));

        // not in whitelist
        assertFalse(whitelistOptions.packageAllowed("java.util"));

        // in whitelist
        assertTrue(whitelistOptions.packageAllowed("java.net"));
        assertTrue(whitelistOptions.packageAllowed("my.utils"));
    }

    @Test
    public void testBlacklist() {
        Options blacklistOptions = Options.asOptions("blacklist=java.net:my.utils");

        assertThat(blacklistOptions.blacklist().size(), is(2));
        assertTrue(blacklistOptions.blacklist().contains("java.net"));
        assertTrue(blacklistOptions.blacklist().contains("my.utils"));

        // built-in
        assertFalse(blacklistOptions.packageAllowed("java.lang"));
        assertFalse(blacklistOptions.packageAllowed("jackplay"));
        assertFalse(blacklistOptions.packageAllowed("jackplay.web"));

        // not in blacklist
        assertTrue(blacklistOptions.packageAllowed("java.util"));

        // in blacklist
        assertFalse(blacklistOptions.packageAllowed("java.net"));
        assertFalse(blacklistOptions.packageAllowed("my.utils"));
    }

    @Test
    public void testMultipleOptions() {
        Options options = Options.asOptions("whitelist=java.net;port=3355");
        assertTrue(options.packageAllowed("java.net"));
        assertEquals(3355, options.port());
    }

    @Test
    public void testDefaultTrace() {
        Options options = Options.asOptions("defaultTrace=myapp.Main.loadConfig(String[] args)");
        assertEquals("myapp.Main.loadConfig(String[] args)", options.defaultTrace());

        options = Options.asOptions("defaultTrace=myapp.Main.loadConfig(String,String)");
        assertEquals("myapp.Main.loadConfig(String,String)", options.defaultTrace());
    }
}
