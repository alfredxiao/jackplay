package testedapp.myapp;

public abstract class MyBaseClass {

    public String test1(int arg1, String arg2) {
        return arg1 + "." + arg2;
    }

    // will throw exception, for testing tracing exception
    public void test2(Object arg1, java.util.List<String> arg2) {
        int size = arg2.size();
    }

    public Object[] test3(Object[] arg1, int[][] arg2) {
        return arg1;
    }

    public abstract void myAbstract();

    public native void myNative();
}
