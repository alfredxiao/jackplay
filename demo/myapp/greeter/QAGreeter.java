package myapp.greeter;

public class QAGreeter implements Greeter {
    public String greet(String name) {
        testScenarios();
        return "Hello from QA";
    }

    void testScenarios() {
        byte b = 10;
        short sh = 100;
        testArguments(true, b, sh, 1000, 10000L, 2.3F, 3.8D, 'C', "String", new Object(), new QAGreeter(), new String[]{"S1", "S2"});
        testReturningVoid("nothing");
        testReturningBoolean();
        testReturningByte();
        testReturningShort();
        testReturningChar();
        testReturningInt();
        testReturningLong();
        testReturningFloat();
        testReturningDouble();
        testReturningObject();
        testReturningQAGreeter();
        testReturningArray();
        try {
            testThrowingCheckException();
        } catch (Exception e) {}
        testThrowingRuntimeException();
    }

    private void testThrowingCheckException() throws Exception {
        throw new Exception("a QA checked Exception!");
    }

    private void testThrowingRuntimeException() {
        throw new RuntimeException("a QA runtime Exception!");
    }

    private char testReturningChar() {
        return 'A';
    }

    private boolean testReturningBoolean() {
        return true;
    }

    private byte testReturningByte() {
        return 100;
    }

    private short testReturningShort() {
        return 200;
    }

    private int testReturningInt() {
        return 1000;
    }

    private long testReturningLong() {
        return 203030L;
    }

    private float testReturningFloat() {
        return 2.33F;
    }

    private double testReturningDouble() {
        return 100.23D;
    }

    private Object testReturningObject() {
        return new Object();
    }

    private QAGreeter testReturningQAGreeter() {
        return new QAGreeter();
    }

    private String[] testReturningArray() {
        return new String[]{"Result1", "Result2"};
    }

    private void testReturningVoid(String arg1) {
    }

    private void testArguments(boolean bool, byte byt, short sh, int i, long l, float f, double d, char c,
                               String str, Object o, QAGreeter qaGreeter, String[] strings) {}


}
