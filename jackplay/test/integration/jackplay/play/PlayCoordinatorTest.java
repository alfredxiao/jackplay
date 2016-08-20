package integration.jackplay.play;

import integration.myapp.MyAbstractClass;
import integration.myapp.MyClass;
import jackplay.TheatreRep;
import jackplay.bootstrap.PlayGround;
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

    @Before
    public void setup() throws PlayException {
        coordinator.undoAll();
        Class myAbstractClass = MyAbstractClass.class;
        Class myClass = MyClass.class;
    }

    @Test
    public void canHandleHappyCaseAddTrace() throws PlayException {
        List<Map<String, Object>> logsBefore = infoCenter.getTraceLogs();
        coordinator.trace(pg_myfunction1);
        MyClass myObj = new MyClass();
        myObj.myfunction1(123, "ABC");
        List<Map<String, Object>> logsAfter = infoCenter.getTraceLogs();

        assertEquals(2, logsAfter.size() - logsBefore.size());

        Map<String, ?> traceLogOfMethodReturns = logsAfter.get(0);
        assertEquals(TracePoint.MethodReturns.toString(), traceLogOfMethodReturns.get("tracePoint"));
        assertNull(traceLogOfMethodReturns.get("arguments"));
        assertEquals(2, traceLogOfMethodReturns.get("argumentsCount"));
        assertEquals("integration.myapp.MyAbstractClass", traceLogOfMethodReturns.get("classFullName"));
        assertEquals("myfunction1", traceLogOfMethodReturns.get("methodShortName"));
        assertEquals("\"ABC.123\"", traceLogOfMethodReturns.get("returnedValue"));
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
        MyClass myObj = new MyClass();
        try {
            myObj.myfunction2("A", null);
        } catch(Exception ignore) {}

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
        MyClass myObj = new MyClass();
        myObj.myfunction1(123, "ABC");
        List<Map<String, Object>> logsAfter = infoCenter.getTraceLogs();

        assertEquals(2, logsAfter.size() - logsBefore.size());

        coordinator.undoTrace(pg_myfunction1);
        myObj.myfunction1(234, "DEF");
        List<Map<String, Object>> logsAfterUndo = infoCenter.getTraceLogs();

        assertEquals(0, logsAfterUndo.size() - logsAfter.size());
    }

    @Test
    public void canHandleHappyCaseRedefine() {
    }

    @Test
    public void canRedefineAfterTrace() {

    }

    @Test
    public void canTraceAfterRedefine() {

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
