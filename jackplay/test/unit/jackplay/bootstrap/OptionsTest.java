package unit.jackplay.bootstrap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.core.Is.*;

import jackplay.bootstrap.Options;
import org.junit.BeforeClass;
import org.junit.Test;

public class OptionsTest {

    @BeforeClass
    public static void setup() {
    }

    @Test
    public void testDefaults() {
        Options defaultOptions = Options.optionsMergedWithDefaults("");

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
        assertFalse(defaultOptions.canPlayPackage("java.lang"));
        assertFalse(defaultOptions.canPlayPackage("jackplay"));
        assertFalse(defaultOptions.canPlayPackage("jackplay.web"));

        // any package
        assertTrue(defaultOptions.canPlayPackage("java.util"));
    }

    @Test
    public void testWhitelist() {
        Options whitelistOptions = Options.optionsMergedWithDefaults("whitelist=java.net:my.utils");

        assertThat(whitelistOptions.whitelist().size(), is(2));
        assertTrue(whitelistOptions.whitelist().contains("java.net"));
        assertTrue(whitelistOptions.whitelist().contains("my.utils"));

        // built-in
        assertFalse(whitelistOptions.canPlayPackage("java.lang"));
        assertFalse(whitelistOptions.canPlayPackage("jackplay"));
        assertFalse(whitelistOptions.canPlayPackage("jackplay.web"));

        // not in whitelist
        assertFalse(whitelistOptions.canPlayPackage("java.util"));

        // in whitelist
        assertTrue(whitelistOptions.canPlayPackage("java.net"));
        assertTrue(whitelistOptions.canPlayPackage("my.utils"));
    }

    @Test
    public void testBlacklist() {
        Options blacklistOptions = Options.optionsMergedWithDefaults("blacklist=java.net:my.utils");

        assertThat(blacklistOptions.blacklist().size(), is(2));
        assertTrue(blacklistOptions.blacklist().contains("java.net"));
        assertTrue(blacklistOptions.blacklist().contains("my.utils"));

        // built-in
        assertFalse(blacklistOptions.canPlayPackage("java.lang"));
        assertFalse(blacklistOptions.canPlayPackage("jackplay"));
        assertFalse(blacklistOptions.canPlayPackage("jackplay.web"));

        // not in blacklist
        assertTrue(blacklistOptions.canPlayPackage("java.util"));

        // in blacklist
        assertFalse(blacklistOptions.canPlayPackage("java.net"));
        assertFalse(blacklistOptions.canPlayPackage("my.utils"));
    }
}
