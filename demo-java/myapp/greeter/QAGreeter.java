package myapp.greeter;

import myapp.Demo;

public class QAGreeter implements Greeter {
    public String greet(String name) {
        Demo.sleepSmallRandom();
        testScenarios();
        return "Hello from QA";
    }

    void testScenarios() {
        Demo.sleepSmallRandom();

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
        testReturningString();
        testReturningObject();
        testReturningQAGreeter();
        testReturningArray();
        try {
            testThrowingCheckException();
        } catch (Exception e) {}
        testThrowingRuntimeException();
    }

    private String testReturningString() {
        Demo.sleepSmallRandom();
        return "Hello QA";
    }

    private void testThrowingCheckException() throws Exception {
        Demo.sleepSmallRandom();
        throw new Exception("a QA checked Exception!");
    }

    private void testThrowingRuntimeException() {
        Demo.sleepSmallRandom();
        throw new RuntimeException("a QA runtime Exception!");
    }

    private char testReturningChar() {
        Demo.sleepSmallRandom();
        return 'A';
    }

    private boolean testReturningBoolean() {
        Demo.sleepSmallRandom();
        return true;
    }

    private byte testReturningByte() {
        Demo.sleepSmallRandom();
        return 100;
    }

    private short testReturningShort() {
        Demo.sleepSmallRandom();
        return 200;
    }

    private int testReturningInt() {
        Demo.sleepSmallRandom();
        return 1000;
    }

    private long testReturningLong() {
        Demo.sleepSmallRandom();
        return 203030L;
    }

    private float testReturningFloat() {
        Demo.sleepSmallRandom();
        return 2.33F;
    }

    private double testReturningDouble() {
        Demo.sleepSmallRandom();
        return 100.23D;
    }

    private Object testReturningObject() {
        Demo.sleepSmallRandom();
        return new Object();
    }

    private QAGreeter testReturningQAGreeter() {
        Demo.sleepSmallRandom();
        return new QAGreeter();
    }

    private String[] testReturningArray() {
        Demo.sleepSmallRandom();
        return new String[]{"Result1", "Result2"};
    }

    private void testReturningVoid(String arg1) {
        Demo.sleepSmallRandom();
    }

    private void testArguments(boolean bool, byte byt, short sh, int i, long l, float f, double d, char c,
                               String str, Object o, QAGreeter qaGreeter, String[] strings) {
        Demo.sleepSmallRandom();
    }


}
