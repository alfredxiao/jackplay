package integration.jackplay.play;

import static jackplay.bootstrap.Genre.*;

import jackplay.bootstrap.PlayGround;
import jackplay.play.ProgramManager;
import jackplay.play.performers.RedefinePerformer;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProgramManagerTest {

    @Test
    public void shouldAddTraces() {
        ProgramManager pm = new ProgramManager();
        pm.addTraces(new String[]{"a.b.c()", "a.m.n()", "a.m.n2(int[])"});

        assertTrue(pm.agendaForClass("a.b").get(METHOD_TRACE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.m").get(METHOD_TRACE).containsKey("a.m.n()"));
        assertTrue(pm.agendaForClass("a.m").get(METHOD_TRACE).containsKey("a.m.n2(int[])"));
    }

    @Test
    public void shouldAddTrace() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(METHOD_TRACE, new PlayGround("a.b.c()"), null);

        assertTrue(pm.agendaForClass("a.b").get(METHOD_TRACE).containsKey("a.b.c()"));
    }

    @Test
    public void shouldAddRedefinition() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()"), "newsource");

        assertTrue(pm.agendaForClass("a.b").get(METHOD_REDEFINE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.b").get(METHOD_REDEFINE).get("a.b.c()") instanceof RedefinePerformer);
    }

    @Test
    public void shouldNotAddExistingPlay() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()"), "newsource");
        pm.addAgenda(METHOD_TRACE, new PlayGround("a.b.c()"), null);

        assertFalse(pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()"), "newsource"));
        assertFalse(pm.addAgenda(METHOD_TRACE, new PlayGround("a.b.c()"), null));
    }

    @Test
    public void shouldRemoveExistingPlay() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()"), "newsource");

        assertTrue(pm.removeAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()")));
    }

    @Test
    public void shouldNotRemoveNoneExistingPlay() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()"), "newsource");

        assertFalse(pm.removeAgenda(METHOD_TRACE, new PlayGround("a.b.c()")));
    }

    @Test
    public void shouldFilterAgendaForClass() {
        ProgramManager pm = new ProgramManager();
        pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.b.c()"), "body1");
        pm.addAgenda(METHOD_TRACE, new PlayGround("a.b.c()"), null);
        pm.addAgenda(METHOD_TRACE, new PlayGround("a.b.d()"), null);
        pm.addAgenda(METHOD_REDEFINE, new PlayGround("a.dd.c()"), "body2");
        pm.addAgenda(METHOD_TRACE, new PlayGround("a.dd.c()"), null);

        assertEquals(1, pm.agendaForClass("a.b").get(METHOD_REDEFINE).size());
        assertTrue(pm.agendaForClass("a.b").get(METHOD_REDEFINE).get("a.b.c()") instanceof RedefinePerformer);

        assertEquals(2, pm.agendaForClass("a.b").get(METHOD_TRACE).size());
        assertTrue(pm.agendaForClass("a.b").get(METHOD_TRACE).containsKey("a.b.c()"));
        assertTrue(pm.agendaForClass("a.b").get(METHOD_TRACE).containsKey("a.b.d()"));
    }
}
