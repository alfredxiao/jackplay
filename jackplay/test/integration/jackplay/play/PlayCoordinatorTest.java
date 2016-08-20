package integration.jackplay.play;

import integration.myapp.MyAbstractClass;
import integration.myapp.MyClass;
import jackplay.TheatreRep;
import jackplay.bootstrap.PlayGround;
import jackplay.bootstrap.TraceKeeper;
import jackplay.bootstrap.TracePoint;
import jackplay.play.InfoCenter;
import jackplay.play.PlayCoordinator;
import jackplay.play.PlayException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

public class PlayCoordinatorTest {
    PlayCoordinator coordinator = TheatreRep.getPlayCoordinator();
    InfoCenter infoCenter = TheatreRep.getInfoCenter();
    PlayGround pg_myfunction1 = new PlayGround("integration.myapp.MyAbstractClass.myfunction1(int,java.lang.String)");
    PlayGround pg_myfunction2 = new PlayGround("integration.myapp.MyAbstractClass.myfunction2(java.lang.Object,java.util.List)");
    MyClass myObj;

    @Before
    public void setup() throws PlayException {
        coordinator.undoAll();
        TraceKeeper.clearLogHistory();
        Class myAbstractClass = MyAbstractClass.class;
        Class myClass = MyClass.class;
        myObj = new MyClass();
    }

    @Test
    public void canAddTraceAndProduceTraceLog() throws PlayException {
        List<Map<String, Object>> logsBefore = infoCenter.getTraceLogs();
        coordinator.trace(pg_myfunction1);
        String returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        List<Map<String, Object>> logsAfter = infoCenter.getTraceLogs();

        assertEquals(2, logsAfter.size() - logsBefore.size());

        Map<String, ?> traceLogOfMethodReturns = logsAfter.get(0);
        assertEquals(TracePoint.MethodReturns.toString(), traceLogOfMethodReturns.get("tracePoint"));
        assertNull(traceLogOfMethodReturns.get("arguments"));
        assertEquals(2, traceLogOfMethodReturns.get("argumentsCount"));
        assertEquals("integration.myapp.MyAbstractClass", traceLogOfMethodReturns.get("classFullName"));
        assertEquals("myfunction1", traceLogOfMethodReturns.get("methodShortName"));
        assertEquals("\"123.ABC\"", traceLogOfMethodReturns.get("returnedValue"));
        assertEquals(Thread.currentThread().getName(), traceLogOfMethodReturns.get("threadName"));

        Map<String, ?> traceLogOfMethodEntry = logsAfter.get(1);
        assertEquals(TracePoint.MethodEntry.toString(), traceLogOfMethodEntry.get("tracePoint"));
        assertTrue(traceLogOfMethodEntry.get("arguments").getClass().isArray());
        assertEquals(2, ((String[]) traceLogOfMethodEntry.get("arguments")).length);
        assertEquals("123", ((String[]) traceLogOfMethodEntry.get("arguments"))[0]);
        assertEquals("\"ABC\"", ((String[]) traceLogOfMethodEntry.get("arguments"))[1]);
        assertEquals(2, traceLogOfMethodEntry.get("argumentsCount"));
        assertEquals("integration.myapp.MyAbstractClass", traceLogOfMethodEntry.get("classFullName"));
        assertEquals("myfunction1", traceLogOfMethodEntry.get("methodShortName"));
        assertEquals(null, traceLogOfMethodEntry.get("returnedValue"));
        assertEquals(Thread.currentThread().getName(), traceLogOfMethodEntry.get("threadName"));
    }
    
    @Test
    public void canTraceException() throws PlayException {
        List<Map<String, Object>> logsBefore = infoCenter.getTraceLogs();
        coordinator.trace(pg_myfunction2);
        Exception thrown = null;
        try {
            myObj.myfunction2("A", null);
        } catch(Exception e) {
            thrown = e;
        }

        assertTrue(thrown instanceof NullPointerException);

        List<Map<String, Object>> logsAfter = infoCenter.getTraceLogs();

        assertEquals(2, logsAfter.size() - logsBefore.size());

        Map<String, ?> traceLogOfMethodThrowsException = logsAfter.get(0);
        assertEquals(TracePoint.MethodThrowsException.toString(), traceLogOfMethodThrowsException.get("tracePoint"));
        assertNull(traceLogOfMethodThrowsException.get("arguments"));
        assertEquals(2, traceLogOfMethodThrowsException.get("argumentsCount"));
        assertEquals("integration.myapp.MyAbstractClass", traceLogOfMethodThrowsException.get("classFullName"));
        assertEquals("myfunction2", traceLogOfMethodThrowsException.get("methodShortName"));
        assertEquals(null, traceLogOfMethodThrowsException.get("returnedValue"));
        assertTrue(((String) traceLogOfMethodThrowsException.get("exceptionStackTrace")).contains("java.lang.NullPointerException"));
        assertEquals(Thread.currentThread().getName(), traceLogOfMethodThrowsException.get("threadName"));
    }

    @Test
    public void canUndoTrace() throws PlayException {
        List<Map<String, Object>> logsBefore = infoCenter.getTraceLogs();
        coordinator.trace(pg_myfunction1);
        myObj.myfunction1(123, "ABC");
        List<Map<String, Object>> logsAfter = infoCenter.getTraceLogs();

        assertEquals(2, logsAfter.size() - logsBefore.size());

        coordinator.undoTrace(pg_myfunction1);
        myObj.myfunction1(234, "DEF");
        List<Map<String, Object>> logsAfterUndo = infoCenter.getTraceLogs();

        assertEquals(0, logsAfterUndo.size() - logsAfter.size());
    }

    @Test
    public void canRedefine() throws PlayException {
        // before redefine, things work as expected
        String returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        // redefine it
        coordinator.redefine(pg_myfunction1, "{ return \"HAS_BEEN_REDEFINED1\";}");

        // after redefinition
        returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED1", returnValue);
    }

    @Test
    public void canUndoRedefine() throws PlayException {
        // redefine it
        coordinator.redefine(pg_myfunction1, "{ return \"HAS_BEEN_REDEFINED4\";}");

        // after redefinition
        String returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED4", returnValue);

        // undo it
        coordinator.undoRedefine(pg_myfunction1);

        // after undo
        returnValue = myObj.myfunction1(345, "GHI");
        assertEquals("345.GHI", returnValue);
    }

    @Test
    public void canRedefineAfterTrace() throws PlayException {
        // start tracing
        coordinator.trace(pg_myfunction1);
        int logCountBegin = infoCenter.getTraceLogs().size();

        // do something, which works as normal
        String returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        // redefine it
        coordinator.redefine(pg_myfunction1, "{ return \"HAS_BEEN_REDEFINED2\";}");

        // after redefinition, which should work as expected
        returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED2", returnValue);

        // trace should works as orthogonal to redefinition
        int logCountEnd = infoCenter.getTraceLogs().size();

        assertEquals(4, logCountEnd - logCountBegin);
    }

    @Test
    public void canTraceAfterRedefine() throws PlayException {
        // log count before anything
        int logCountBegin = infoCenter.getTraceLogs().size();

        // start with redefinition
        coordinator.redefine(pg_myfunction1, "{ return \"HAS_BEEN_REDEFINED3\";}");

        // after redefinition, which should work as expected
        String returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED3", returnValue);

        // start tracing
        coordinator.trace(pg_myfunction1);

        // do something, which should still be under redefinition
        returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("HAS_BEEN_REDEFINED3", returnValue);

        // trace should works as orthogonal to redefinition
        int logCountEnd = infoCenter.getTraceLogs().size();

        assertEquals(2, logCountEnd - logCountBegin);
    }

    @Test
    public void canReportCompilationErrorWhenRedefine() {

    }

    @Test
    public void canReportVerifierErrorWhenRedefine() {

    }

    @Test
    public void compilationErrorShouldNotAffectRedefinitionInAnotherMethod() {
    }

    @Test
    public void verificationErrorShouldNotAffectRedefinitionInAnotherMethod() {
    }

    @Test
    public void compilationErrorShouldNotAffectTraces() {
    }

    @Test
    public void verificationErrorShouldNotAffectTraces() {
    }

    @Test
    public void canTraceAndRedefineBeforeClassLoaded() {

    }

    @Test
    public void canUndoRedefinition() {

    }

    @Test
    public void canTraceAfterUndoTrace() {

    }

    @Test
    public void canRedefineAfterUndoRedefine() {

    }

    @Test
    public void canUndoTraceOnClassLevel() {

    }

    @Test
    public void canUndoRedefinitionOnClassLevel() {

    }
}
