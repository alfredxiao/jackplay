package unit.jackplay.play;

import static jackplay.model.Genre.*;

import jackplay.model.Options;
import jackplay.model.Site;
import jackplay.core.Registry;
import jackplay.core.performers.RedefinePerformer;
import org.junit.Test;
import static org.junit.Assert.*;

public class RegistryTest {

    @Test
    public void shouldAddTraces() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.addTraces(new String[]{"a.b.c()", "a.m.n()", "a.m.n2(int[])"});

        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.m").get(TRACE).containsKey("a.m.n()"));
        assertTrue(pm.agendaForClass("a.m").get(TRACE).containsKey("a.m.n2(int[])"));
    }

    @Test
    public void shouldAddTrace() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.register(TRACE, new Site("a.b.c()"), null);

        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.c()"));
    }

    @Test
    public void shouldAddRedefinition() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.register(REDEFINE, new Site("a.b.c()"), "newsource");

        assertTrue(pm.agendaForClass("a.b").get(REDEFINE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.b").get(REDEFINE).get("a.b.c()") instanceof RedefinePerformer);
    }

    @Test
    public void shouldNotAddExistingPlayForTracingButAlwaysAddForRedefinition() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.register(REDEFINE, new Site("a.b.c()"), "newsource");
        pm.register(TRACE, new Site("a.b.c()"), null);

        assertTrue(pm.register(REDEFINE, new Site("a.b.c()"), "newsource"));
        assertFalse(pm.register(TRACE, new Site("a.b.c()"), null));
    }

    @Test
    public void shouldRemoveExistingPlay() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.register(REDEFINE, new Site("a.b.c()"), "newsource");

        assertTrue(pm.unregister(REDEFINE, new Site("a.b.c()")));
    }

    @Test
    public void shouldNotRemoveNoneExistingPlay() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.register(REDEFINE, new Site("a.b.c()"), "newsource");

        assertFalse(pm.unregister(TRACE, new Site("a.b.c()")));
    }

    @Test
    public void shouldFilterAgendaForClass() {
        Registry pm = new Registry(Options.asOptions(""));
        pm.register(REDEFINE, new Site("a.b.c()"), "body1");
        pm.register(TRACE, new Site("a.b.c()"), null);
        pm.register(TRACE, new Site("a.b.d()"), null);
        pm.register(REDEFINE, new Site("a.dd.c()"), "body2");
        pm.register(TRACE, new Site("a.dd.c()"), null);

        assertEquals(1, pm.agendaForClass("a.b").get(REDEFINE).size());
        assertTrue(pm.agendaForClass("a.b").get(REDEFINE).get("a.b.c()") instanceof RedefinePerformer);

        assertEquals(2, pm.agendaForClass("a.b").get(TRACE).size());
        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.d()"));
    }
}
