package myapp.greeter;

import myapp.Demo;

public class QAGreeter implements Greeter {
    private static String longlongString = "A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789K123456789M123456789N123456789O123456789P123456789";

    public String greet(String name) {
        Demo.sleepSmallRandom();
        testScenarios();
        return "Hello from QA";
    }

    void testScenarios() {
        Demo.sleepSmallRandom();

        byte b = 10;
        short sh = 100;
        testArguments(true, b, sh, 1000, 10000L, 2.3F, 3.8D, 'C', "String", new Object(), new QAGreeter(), new String[]{"S1", "S2", longlongString},
                new byte[] {33, 88}, new int[]{10, 20, 30, 40, 50});
        testReturningVoid("noth\"i'ng");
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
        testReturningByteArray();
        testReturningIntArrayArray();
        testVarargs("A", "B", "C");
        try {
            testThrowingCheckException();
        } catch (Exception e) {}
        testThrowingRuntimeException();
    }

    private String testVarargs(String ... args) {
        StringBuilder result = new StringBuilder();
        for(String arg : args) {
            result.append(arg);
        }

        return result.toString();
    }

    private String testReturningString() {
        Demo.sleepSmallRandom();
        return "Hello QA '$%#@!(*&^\"";
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

    private byte[] testReturningByteArray() {
        Demo.sleepSmallRandom();
        return new byte[]{1,2,3,4,5,6,8,9,10};
    }

    private int[][] testReturningIntArrayArray() {
        Demo.sleepSmallRandom();
        return new int[][]{{1,2,3,4,5,6,8,9,10}, {10,20,30,40,50}, {100,200,300,400,500,600}, {1000,2000}};
    }

    private void testReturningVoid(String arg1) {
        Demo.sleepSmallRandom();
    }

    private void testArguments(boolean bool, byte byt, short sh, int i, long l, float f, double d, char c,
                               String str, Object o, QAGreeter qaGreeter, String[] strings, byte[] bytes, int[] ints) {
        Demo.sleepSmallRandom();
    }

    private class QAPrivateInnerClass {
        public String test1() {
            return "test1";
        }
    }

    protected class QAProtectedInnerClass {
        public String test2() {
            return "test2";
        }
    }

    static Class c1;
    static Class c2;
    static {
        c1 = QAProtectedInnerClass.class;
        c2 = QAPrivateInnerClass.class;
//        System.out.println(c1.getName());
//        System.out.println(c1.getCanonicalName());
//        System.out.println(c2.getName());
//        System.out.println(c2.getCanonicalName());
    }

    public static void main(String[] args) {
        Class c = QAGreeter.class;
        //System.out.println( QAGreeter.QAProtectedInnerClass.class.getCanonicalName() );
//        System.out.println( QAGreeter.QAProtectedInnerClass.class.getName() );
    }
}
