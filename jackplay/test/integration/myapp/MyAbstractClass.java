package integration.myapp;

public abstract class MyAbstractClass {

    public String myfunction1(int arg1, String arg2) {
        return arg1 + "." + arg2;
    }

    // will throw exception, for testing tracing exception
    public void myfunction2(Object arg1, java.util.List<String> arg2) {
        arg2.get(100);
    }

    public Object[] myfunction3(Object[] arg1, int[][] arg2) {
        return arg1;
    }

    public abstract void myAbstract();

    public native void myNative();
}
