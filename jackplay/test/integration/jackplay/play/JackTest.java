package integration.jackplay.play;

import testedapp.myapp.MyBaseClass;
import testedapp.myapp.MyClass;
import testedapp.myapp.MyLateLoadingClass;
import jackplay.TheatreRep;
import static jackplay.bootstrap.Genre.*;

import jackplay.bootstrap.Genre;
import jackplay.bootstrap.PlayGround;
import jackplay.bootstrap.TraceKeeper;
import jackplay.bootstrap.TracePoint;
import jackplay.play.InfoCenter;
import jackplay.play.Jack;
import jackplay.play.PlayException;
import jackplay.play.performers.Performer;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JackTest {
    Jack jack = TheatreRep.getJack();
    InfoCenter infoCenter = TheatreRep.getInfoCenter();

    PlayGround test1 = new PlayGround("testedapp.myapp.MyBaseClass.test1(int,java.lang.String)");
    PlayGround test2 = new PlayGround("testedapp.myapp.MyBaseClass.test2(java.lang.Object,java.util.List)");
    PlayGround test3 = new PlayGround("testedapp.myapp.MyBaseClass.test3(java.lang.Object[],int[][])");
    PlayGround lateLoading = new PlayGround("testedapp.myapp.MyLateLoadingClass.lateLoadingFunction(java.lang.String)");

    MyClass myObj;

    @Before
    public void setup() throws PlayException {
        jack.undoAll();
        TraceKeeper.clearLogHistory();
        Class myAbstractClass = MyBaseClass.class;
        Class myClass = MyClass.class;
        myObj = new MyClass();
    }

    @Test
    public void canAddTraceAndProduceTraceLog() throws PlayException {
        List<Map<String, Object>> logsBefore = TraceKeeper.getTraceLogs();
        jack.trace(test1);
        String returnValue = myObj.test1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        List<Map<String, Object>> logsAfter = TraceKeeper.getTraceLogs();

        assertEquals(2, logsAfter.size() - logsBefore.size());

        Map<String, ?> traceLogOfMethodReturns = logsAfter.get(0);
        assertEquals(TracePoint.MethodReturns.toString(), traceLogOfMethodReturns.get("tracePoint"));
        assertNull(traceLogOfMethodReturns.get("arguments"));
        assertEquals(2, traceLogOfMethodReturns.get("argumentsCount"));
        assertEquals("testedapp.myapp.MyBaseClass", traceLogOfMethodReturns.get("classFullName"));
        assertEquals("test1", traceLogOfMethodReturns.get("methodShortName"));
        assertEquals("\"123.ABC\"", traceLogOfMethodReturns.get("returnedValue"));
        assertEquals(Thread.currentThread().getName(), traceLogOfMethodReturns.get("threadName"));

        Map<String, ?> traceLogOfMethodEntry = logsAfter.get(1);
        assertEquals(TracePoint.MethodEntry.toString(), traceLogOfMethodEntry.get("tracePoint"));
        assertTrue(traceLogOfMethodEntry.get("arguments").getClass().isArray());
        assertEquals(2, ((String[]) traceLogOfMethodEntry.get("arguments")).length);
        assertEquals("123", ((String[]) traceLogOfMethodEntry.get("arguments"))[0]);
        assertEquals("\"ABC\"", ((String[]) traceLogOfMethodEntry.get("arguments"))[1]);
        assertEquals(2, traceLogOfMethodEntry.get("argumentsCount"));
        assertEquals("testedapp.myapp.MyBaseClass", traceLogOfMethodEntry.get("classFullName"));
        assertEquals("test1", traceLogOfMethodEntry.get("methodShortName"));
        assertEquals(null, traceLogOfMethodEntry.get("returnedValue"));
        assertEquals(Thread.currentThread().getName(), traceLogOfMethodEntry.get("threadName"));

        assertProgramSize(1);
        assertProgramContains(METHOD_TRACE, test1);
    }

    private int assertProgramSize(int size) {
        int total = 0;
        for (Genre g : infoCenter.getCurrentProgram().keySet()) {
            Map<String, Map<String, Performer>> gMap = infoCenter.getCurrentProgram().get(g);
            for (String cls : gMap.keySet()) {
                Map<String, Performer> clsMap = gMap.get(cls);
                total += clsMap.size();
            }
        }

        return total;
    }

    private void assertProgramContains(Genre g, PlayGround pg) {
        Map<String, Map<String, Performer>> gMap = infoCenter.getCurrentProgram().get(g);

        boolean contains = false;
        try {
            Performer p = gMap.get(pg.classFullName).get(pg.methodFullName);
            contains = (p != null);
        } catch(NullPointerException npe) {
            contains = false;
        }

        assertTrue(contains);
    }

    @Test
    public void canTraceException() throws PlayException {
        List<Map<String, Object>> logsBefore = TraceKeeper.getTraceLogs();
        jack.trace(test2);
        Exception thrown = null;
        try {
            myObj.test2("A", null);
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
        assertEquals("testedapp.myapp.MyBaseClass", traceLogOfMethodThrowsException.get("classFullName"));
        assertEquals("test2", traceLogOfMethodThrowsException.get("methodShortName"));
        assertEquals(null, traceLogOfMethodThrowsException.get("returnedValue"));
        assertTrue(((String) traceLogOfMethodThrowsException.get("exceptionStackTrace")).contains("java.lang.NullPointerException"));
        assertEquals(Thread.currentThread().getName(), traceLogOfMethodThrowsException.get("threadName"));
    }

    @Test
    public void canUndoTrace() throws PlayException {
        int logsCountStart = getTraceLogSize();
        jack.trace(test1);
        myObj.test1(123, "ABC");
        int logsCountEnd = getTraceLogSize();

        assertEquals(2, logsCountEnd - logsCountStart);

        jack.undoTrace(test1);
        myObj.test1(234, "DEF");
        int logsCountAfterUndo = getTraceLogSize();

        assertEquals(0, logsCountAfterUndo - logsCountEnd);

        assertProgramSize(0);
    }

    private int getTraceLogSize() {
        return TraceKeeper.getTraceLogs().size();
    }

    @Test
    public void canRedefine() throws PlayException {
        // before redefine, things work as expected
        String returnValue = myObj.test1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        // redefine it
        jack.redefine(test1, "{ return \"HAS_BEEN_REDEFINED1\";}");

        // after redefinition
        returnValue = myObj.test1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED1", returnValue);

        assertProgramSize(1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void canUndoRedefine() throws PlayException {
        // redefine it
        jack.redefine(test1, "{ return \"HAS_BEEN_REDEFINED4\";}");

        // after redefinition
        String returnValue = myObj.test1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED4", returnValue);

        // undo it
        jack.undoRedefine(test1);

        // after undo
        returnValue = myObj.test1(345, "GHI");
        assertEquals("345.GHI", returnValue);
    }

    @Test
    public void canRedefineAfterTrace() throws PlayException {
        // start tracing
        jack.trace(test1);
        int logCountBegin = getTraceLogSize();

        // do something, which works as normal (before redefinition)
        String returnValue = myObj.test1(123, "ABC");
        assertEquals("123.ABC", returnValue);

        // redefine it
        jack.redefine(test1, "{ return \"HAS_BEEN_REDEFINED2\";}");

        // after redefinition, which should work as expected
        returnValue = myObj.test1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED2", returnValue);

        // trace should works as orthogonal to redefinition
        int logCountEnd = getTraceLogSize();

        assertEquals(4, logCountEnd - logCountBegin);

        assertProgramSize(2);
        assertProgramContains(METHOD_TRACE, test1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void canTraceAfterRedefine() throws PlayException {
        // log count before anything
        int logCountBegin = getTraceLogSize();

        // start with redefinition
        jack.redefine(test1, "{ return \"HAS_BEEN_REDEFINED3\";}");

        // after redefinition, which should work as expected
        String returnValue = myObj.test1(234, "DEF");
        assertEquals("HAS_BEEN_REDEFINED3", returnValue);

        // start tracing
        jack.trace(test1);

        // do something, which should still be under redefinition
        returnValue = myObj.test1(123, "ABC");
        assertEquals("HAS_BEEN_REDEFINED3", returnValue);

        // trace should works as orthogonal to redefinition
        int logCountEnd = getTraceLogSize();

        assertEquals(2, logCountEnd - logCountBegin);

        assertProgramSize(2);
        assertProgramContains(METHOD_REDEFINE, test1);
        assertProgramContains(METHOD_TRACE, test1);
    }

    @Test
    public void handlesAndReportsCompilationErrorWhenRedefine() {
        Exception exp = null;
        try {
            jack.redefine(test1, "{ return no_such_thing; }");
        } catch(Exception e) {
            exp = e;
        }

        assertNotNull(exp);
        assertTrue(exp instanceof PlayException);
        assertTrue(exp.getMessage().contains("CannotCompileException"));

        assertEquals("123.ABC", myObj.test1(123, "ABC"));

        assertProgramSize(0);
    }

    @Test
    public void compilationErrorShouldNotAffectTraces() throws PlayException {
        int logCountBegin = getTraceLogSize();
        jack.trace(test1);

        try {
            jack.redefine(test1, "{ return no_such_thing; }");
        } catch(Exception e) {}

        myObj.test1(1, "A");
        int logCountEnd = getTraceLogSize();

        // expect trace works as normal
        assertEquals(2, logCountEnd - logCountBegin);

        assertProgramSize(1);
    }

    @Test
    public void handlesAndReportsVerifierErrorWhenRedefine() {
        Exception exp = null;
        try {
            jack.redefine(test1, "{ return 2; }");
        } catch(Exception e) {
            exp = e;
        }

        assertNotNull(exp);
        assertTrue(exp instanceof PlayException);
        assertTrue(exp.getMessage().contains("java.lang.VerifyError"));

        // then redefinition is undone
        assertEquals("123.ABC", myObj.test1(123, "ABC"));

        assertProgramSize(0);
    }

    @Test
    public void verificationErrorShouldNotAffectTraces() throws PlayException {
        jack.trace(test1);
        int logCountBegin = getTraceLogSize();

        try {
            jack.redefine(test1, "{ return 2; }");
        } catch(Exception e) {}

        myObj.test1(1, "A");
        int logCountEnd = getTraceLogSize();

        // expect trace works as normal
        assertEquals(2, logCountEnd - logCountBegin);

        assertProgramSize(1);
        assertProgramContains(METHOD_TRACE, test1);
    }

    @Test
    public void compilationErrorShouldNotAffectRedefinitionInAnotherMethod() throws PlayException {
        jack.redefine(test1, "{ return \"REDEFINED1\"; }");
        try {
            jack.redefine(test3, "{ return no_such_var2; }");
        } catch(Exception e) {}

        assertEquals("REDEFINED1", myObj.test1(200, "BCD"));

        assertProgramSize(1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void verificationErrorShouldNotAffectRedefinitionInAnotherMethod() throws PlayException {
        jack.redefine(test1, "{ return \"REDEFINED1\"; }");
        try {
            jack.redefine(test3, "{ return 3; }");
        } catch(Exception e) {}

        assertEquals("REDEFINED1", myObj.test1(200, "BCD"));

        assertProgramSize(1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void canTraceAndRedefineBeforeClassLoaded() throws Exception {
        long now = System.currentTimeMillis();

        jack.trace(lateLoading);
        jack.redefine(lateLoading, "{ return \"REDEFINED2\";}");

        assertProgramSize(2);
        assertProgramContains(METHOD_TRACE, lateLoading);
        assertProgramContains(METHOD_REDEFINE, lateLoading);

        int logCountStart = getTraceLogSize();
        Thread.currentThread().sleep(10);

        // loaded after we start tracing/redefinition
        assertTrue(MyLateLoadingClass.whenLoaded > now);

        MyLateLoadingClass lateLoading = new MyLateLoadingClass();
        assertEquals("REDEFINED2", lateLoading.lateLoadingFunction("Alfred"));

        int logCountEnd = getTraceLogSize();
        assertEquals(2, logCountEnd - logCountStart);

        assertProgramSize(2);
        assertProgramContains(METHOD_TRACE, this.lateLoading);
        assertProgramContains(METHOD_REDEFINE, this.lateLoading);
    }

    @Test
    public void canContinueToTraceAfterUndoTrace() throws PlayException {
        jack.trace(test1);
        jack.undoTrace(test1);

        int logCountStart = getTraceLogSize();
        jack.trace(test1);
        myObj.test1(1, "A");
        int logCountEnd = getTraceLogSize();

        assertEquals(2, logCountEnd - logCountStart);

        assertProgramSize(1);
        assertProgramContains(METHOD_TRACE, test1);
    }

    @Test
    public void canRedefineAfterUndoRedefine() throws PlayException {
        jack.redefine(test1, "{ return \"A\"; }");
        jack.undoRedefine(test1);
        jack.redefine(test1, "{ return \"B\"; }");

        assertEquals("B", myObj.test1(1, "A"));

        assertProgramSize(1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void canRedefineAfterRedefine() throws PlayException {
        jack.redefine(test1, "{ return \"A\"; }");
        myObj.test1(1, "AA");
        jack.redefine(test1, "{ return \"B\"; }");

        assertEquals("B", myObj.test1(2, "AA"));

        assertProgramSize(1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void failedRedefinitionDoesNotChangePreviousRedefinition() throws PlayException {
        jack.redefine(test1, "{ return \"A\"; }");
        assertEquals("A", myObj.test1(2, "AA"));
        try {
            jack.redefine(test1, "{ return no_such_thing; }");
        } catch (PlayException ignore) {}

        assertEquals("A", myObj.test1(3, "AA"));

        assertProgramSize(1);
        assertProgramContains(METHOD_REDEFINE, test1);
    }

    @Test
    public void canUndoTraceOnClassLevel() throws PlayException {
        jack.trace(test1);
        jack.trace(test2);
        jack.trace(test3);

        assertProgramSize(3);

        int logCountStart = getTraceLogSize();
        jack.undoClass(METHOD_TRACE, MyBaseClass.class.getName());

        myObj.test1(1, "A");
        myObj.test2("A", new LinkedList<>());
        myObj.test3(null, null);

        int logCountEnd = getTraceLogSize();
        assertEquals(0, logCountEnd - logCountStart);

        assertProgramSize(0);
    }

    @Test
    public void canUndoRedefinitionOnClassLevel() throws PlayException {
        jack.redefine(test1, "{ return \"A\"; }");
        jack.redefine(test2, "{ throw new RuntimeException(); }");
        jack.redefine(test3, "return null; ");

        assertProgramSize(3);

        jack.undoClass(METHOD_REDEFINE, MyBaseClass.class.getName());

        assertEquals("1.A", myObj.test1(1, "A"));
        myObj.test2("A", new LinkedList<>());
        assertNotNull(myObj.test3(new Object[]{"A"}, null));

        assertProgramSize(0);
    }
}
