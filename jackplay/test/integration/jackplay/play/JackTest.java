package integration.jackplay.play;

import integration.myapp.MyAbstractClass;
import integration.myapp.MyClass;
import integration.myapp.MyLateLoadingClass;
import jackplay.TheatreRep;
import static jackplay.bootstrap.Genre.*;
import jackplay.bootstrap.PlayGround;
import jackplay.bootstrap.TraceKeeper;
import jackplay.bootstrap.TracePoint;
import jackplay.play.Jack;
import jackplay.play.PlayException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JackTest {
    Jack jack = TheatreRep.getJack();

    PlayGround pg_myfunction01 = new PlayGround("integration.myapp.MyAbstractClass.myfunction1(int,java.lang.String)");
    PlayGround pg_myfunction02 = new PlayGround("integration.myapp.MyAbstractClass.myfunction2(java.lang.Object,java.util.List)");
    PlayGround pg_myfunction03 = new PlayGround("integration.myapp.MyAbstractClass.myfunction3(java.lang.Object[],int[][])");
    PlayGround pg_myfunction11 = new PlayGround("integration.myapp.MyLateLoadingClass.myfunction11(java.lang.String)");

    MyClass myObj;

    @Before
    public void setup() throws PlayException {
        jack.undoAll();
        TraceKeeper.clearLogHistory();
        Class myAbstractClass = MyAbstractClass.class;
        Class myClass = MyClass.class;
        myObj = new MyClass();
    }

    @Test
    public void canAddTraceAndProduceTraceLog() throws PlayException {
        List<Map<String, Object>> logsBefore = TraceKeeper.getTraceLogs();
        jack.trace(pg_myfunction01);
        String returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        List<Map<String, Object>> logsAfter = TraceKeeper.getTraceLogs();

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
        List<Map<String, Object>> logsBefore = TraceKeeper.getTraceLogs();
        jack.trace(pg_myfunction02);
        Exception thrown = null;
        try {
            myObj.myfunction2("A", null);
        } catch(Exception e) {
            thrown = e;
        }

        assertTrue(thrown instanceof NullPointerException);

        List<Map<String, Object>> logsAfter = TraceKeeper.getTraceLogs();

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
        int logsCountStart = getTraceLogSize();
        jack.trace(pg_myfunction01);
        myObj.myfunction1(123, "ABC");
        int logsCountEnd = getTraceLogSize();

        assertEquals(2, logsCountEnd - logsCountStart);

        jack.undoTrace(pg_myfunction01);
        myObj.myfunction1(234, "DEF");
        int logsCountAfterUndo = getTraceLogSize();

        assertEquals(0, logsCountAfterUndo - logsCountEnd);
    }

    private int getTraceLogSize() {
        return TraceKeeper.getTraceLogs().size();
    }

    @Test
    public void canRedefine() throws PlayException {
        // before redefine, things work as expected
        String returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        // redefine it
        jack.redefine(pg_myfunction01, "{ return \"HAS_BEEN_REDEFINED1\";}");

        // after redefinition
        returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED1", returnValue);
    }

    @Test
    public void canUndoRedefine() throws PlayException {
        // redefine it
        jack.redefine(pg_myfunction01, "{ return \"HAS_BEEN_REDEFINED4\";}");

        // after redefinition
        String returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED4", returnValue);

        // undo it
        jack.undoRedefine(pg_myfunction01);

        // after undo
        returnValue = myObj.myfunction1(345, "GHI");
        assertEquals("345.GHI", returnValue);
    }

    @Test
    public void canRedefineAfterTrace() throws PlayException {
        // start tracing
        jack.trace(pg_myfunction01);
        int logCountBegin = getTraceLogSize();

        // do something, which works as normal (before redefinition)
        String returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        // redefine it
        jack.redefine(pg_myfunction01, "{ return \"HAS_BEEN_REDEFINED2\";}");

        // after redefinition, which should work as expected
        returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED2", returnValue);

        // trace should works as orthogonal to redefinition
        int logCountEnd = getTraceLogSize();

        assertEquals(4, logCountEnd - logCountBegin);
    }

    @Test
    public void canTraceAfterRedefine() throws PlayException {
        // log count before anything
        int logCountBegin = getTraceLogSize();

        // start with redefinition
        jack.redefine(pg_myfunction01, "{ return \"HAS_BEEN_REDEFINED3\";}");

        // after redefinition, which should work as expected
        String returnValue = myObj.myfunction1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED3", returnValue);

        // start tracing
        jack.trace(pg_myfunction01);

        // do something, which should still be under redefinition
        returnValue = myObj.myfunction1(123, "ABC");
        assertEquals("HAS_BEEN_REDEFINED3", returnValue);

        // trace should works as orthogonal to redefinition
        int logCountEnd = getTraceLogSize();

        assertEquals(2, logCountEnd - logCountBegin);
    }

    @Test
    public void handlesAndReportsCompilationErrorWhenRedefine() {
        Exception exp = null;
        try {
            jack.redefine(pg_myfunction01, "{ return no_such_thing; }");
        } catch(Exception e) {
            exp = e;
        }

        assertNotNull(exp);
        assertTrue(exp instanceof PlayException);
        assertTrue(exp.getMessage().contains("CannotCompileException"));

        assertEquals("123.ABC", myObj.myfunction1(123, "ABC"));
    }

    @Test
    public void compilationErrorShouldNotAffectTraces() throws PlayException {
        int logCountBegin = getTraceLogSize();
        jack.trace(pg_myfunction01);

        try {
            jack.redefine(pg_myfunction01, "{ return no_such_thing; }");
        } catch(Exception e) {}

        myObj.myfunction1(1, "A");
        int logCountEnd = getTraceLogSize();

        // expect trace works as normal
        assertEquals(2, logCountEnd - logCountBegin);
    }

    @Test
    public void handlesAndReportsVerifierErrorWhenRedefine() {
        Exception exp = null;
        try {
            jack.redefine(pg_myfunction01, "{ return 2; }");
        } catch(Exception e) {
            exp = e;
        }

        assertNotNull(exp);
        assertTrue(exp instanceof PlayException);
        assertTrue(exp.getMessage().contains("java.lang.VerifyError"));

        // then redefinition is undone
        assertEquals("123.ABC", myObj.myfunction1(123, "ABC"));
    }

    @Test
    public void verificationErrorShouldNotAffectTraces() throws PlayException {
        jack.trace(pg_myfunction01);
        int logCountBegin = getTraceLogSize();

        try {
            jack.redefine(pg_myfunction01, "{ return 2; }");
        } catch(Exception e) {}

        myObj.myfunction1(1, "A");
        int logCountEnd = getTraceLogSize();

        // expect trace works as normal
        assertEquals(2, logCountEnd - logCountBegin);
    }

    @Test
    public void compilationErrorShouldNotAffectRedefinitionInAnotherMethod() throws PlayException {
        jack.redefine(pg_myfunction01, "{ return \"REDEFINED1\"; }");
        try {
            jack.redefine(pg_myfunction03, "{ return no_such_var2; }");
        } catch(Exception e) {}

        assertEquals("REDEFINED1", myObj.myfunction1(200, "BCD"));
    }

    @Test
    public void verificationErrorShouldNotAffectRedefinitionInAnotherMethod() throws PlayException {
        jack.redefine(pg_myfunction01, "{ return \"REDEFINED1\"; }");
        try {
            jack.redefine(pg_myfunction03, "{ return 3; }");
        } catch(Exception e) {}

        assertEquals("REDEFINED1", myObj.myfunction1(200, "BCD"));
    }

    @Test
    public void canTraceAndRedefineBeforeClassLoaded() throws Exception {
        long now = System.currentTimeMillis();

        jack.trace(pg_myfunction11);
        jack.redefine(pg_myfunction11, "{ return \"REDEFINED2\";}");

        int logCountStart = getTraceLogSize();
        Thread.currentThread().sleep(10);

        // loaded after we start tracing/redefinition
        assertTrue(MyLateLoadingClass.whenLoaded > now);

        MyLateLoadingClass lateLoading = new MyLateLoadingClass();
        assertEquals("REDEFINED2", lateLoading.myfunction11("Alfred"));

        int logCountEnd = getTraceLogSize();
        assertEquals(2, logCountEnd - logCountStart);
    }

    @Test
    public void canContinueToTraceAfterUndoTrace() throws PlayException {
        jack.trace(pg_myfunction01);
        jack.undoTrace(pg_myfunction01);

        int logCountStart = getTraceLogSize();
        jack.trace(pg_myfunction01);
        myObj.myfunction1(1, "A");
        int logCountEnd = getTraceLogSize();

        assertEquals(2, logCountEnd - logCountStart);
    }

    @Test
    public void canRedefineAfterUndoRedefine() throws PlayException {
        jack.redefine(pg_myfunction01, "{ return \"A\"; }");
        jack.undoRedefine(pg_myfunction01);
        jack.redefine(pg_myfunction01, "{ return \"B\"; }");

        assertEquals("B", myObj.myfunction1(1, "A"));
    }

    @Test
    public void canRedefineAfterRedefine() throws PlayException {
        jack.redefine(pg_myfunction01, "{ return \"A\"; }");
        myObj.myfunction1(1, "AA");
        jack.redefine(pg_myfunction01, "{ return \"B\"; }");

        assertEquals("B", myObj.myfunction1(2, "AA"));
    }

    @Test
    public void failedRedefinitionDoesNotChangePreviousRedefinition() throws PlayException {
        jack.redefine(pg_myfunction01, "{ return \"A\"; }");
        assertEquals("A", myObj.myfunction1(2, "AA"));
        try {
            jack.redefine(pg_myfunction01, "{ return no_such_thing; }");
        } catch (PlayException ignore) {}

        assertEquals("A", myObj.myfunction1(3, "AA"));
    }

    @Test
    public void canUndoTraceOnClassLevel() throws PlayException {
        jack.trace(pg_myfunction01);
        jack.trace(pg_myfunction02);
        jack.trace(pg_myfunction03);

        int logCountStart = getTraceLogSize();
        jack.undoClass(METHOD_TRACE, MyAbstractClass.class.getName());

        myObj.myfunction1(1, "A");
        myObj.myfunction2("A", new LinkedList<>());
        myObj.myfunction3(null, null);

        int logCountEnd = getTraceLogSize();
        assertEquals(0, logCountEnd - logCountStart);
    }

    @Test
    public void canUndoRedefinitionOnClassLevel() throws PlayException {
        jack.redefine(pg_myfunction01, "{ return \"A\"; }");
        jack.redefine(pg_myfunction02, "{ throw new RuntimeException(); }");
        jack.redefine(pg_myfunction03, "return null; ");

        jack.undoClass(METHOD_REDEFINE, MyAbstractClass.class.getName());

        assertEquals("1.A", myObj.myfunction1(1, "A"));
        myObj.myfunction2("A", new LinkedList<>());
        assertNotNull(myObj.myfunction3(new Object[]{"A"}, null));
    }
}
