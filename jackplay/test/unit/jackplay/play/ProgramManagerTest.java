package unit.jackplay.play;

import static jackplay.bootstrap.Genre.*;

import jackplay.bootstrap.Site;
import jackplay.play.ProgramManager;
import jackplay.play.performers.RedefinePerformer;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProgramManagerTest {

    @Test
    public void shouldAddTraces() {
        ProgramManager pm = new ProgramManager();
        pm.addTraces(new String[]{"a.b.c()", "a.m.n()", "a.m.n2(int[])"});

        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.m").get(TRACE).containsKey("a.m.n()"));
        assertTrue(pm.agendaForClass("a.m").get(TRACE).containsKey("a.m.n2(int[])"));
    }

    @Test
    public void shouldAddTrace() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(TRACE, new Site("a.b.c()"), null);

        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.c()"));
    }

    @Test
    public void shouldAddRedefinition() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(REDEFINE, new Site("a.b.c()"), "newsource");

        assertTrue(pm.agendaForClass("a.b").get(REDEFINE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.b").get(REDEFINE).get("a.b.c()") instanceof RedefinePerformer);
    }

    @Test
    public void shouldNotAddExistingPlayForTracingButAlwaysAddForRedefinition() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(REDEFINE, new Site("a.b.c()"), "newsource");
        pm.addAgenda(TRACE, new Site("a.b.c()"), null);

        assertTrue(pm.addAgenda(REDEFINE, new Site("a.b.c()"), "newsource"));
        assertFalse(pm.addAgenda(TRACE, new Site("a.b.c()"), null));
    }

    @Test
    public void shouldRemoveExistingPlay() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(REDEFINE, new Site("a.b.c()"), "newsource");

        assertTrue(pm.removeAgenda(REDEFINE, new Site("a.b.c()")));
    }

    @Test
    public void shouldNotRemoveNoneExistingPlay() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(REDEFINE, new Site("a.b.c()"), "newsource");

        assertFalse(pm.removeAgenda(TRACE, new Site("a.b.c()")));
    }

    @Test
    public void shouldFilterAgendaForClass() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(REDEFINE, new Site("a.b.c()"), "body1");
        pm.addAgenda(TRACE, new Site("a.b.c()"), null);
        pm.addAgenda(TRACE, new Site("a.b.d()"), null);
        pm.addAgenda(REDEFINE, new Site("a.dd.c()"), "body2");
        pm.addAgenda(TRACE, new Site("a.dd.c()"), null);

        assertEquals(1, pm.agendaForClass("a.b").get(REDEFINE).size());
        assertTrue(pm.agendaForClass("a.b").get(REDEFINE).get("a.b.c()") instanceof RedefinePerformer);

        assertEquals(2, pm.agendaForClass("a.b").get(TRACE).size());
        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.b").get(TRACE).containsKey("a.b.d()"));
    }
}
