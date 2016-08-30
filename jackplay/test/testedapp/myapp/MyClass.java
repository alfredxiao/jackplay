package testedapp.myapp;


public class MyClass extends MyBaseClass {
    @Override
    public void myAbstract() {

    }

    private class MyPrivateInnerClass {
        public void test1() {}
    }

    protected class MyProtectedInnerClass {
        public void test2() {}
    }

    private static class MyPrivateStaticInnerClass {
        public void test3() {}
    }

    protected static class MyProtectedStaticInnerClass {
        public void test4() {}
    }

    static Class c1;
    static Class c2;
    static Class c3;
    static Class c4;
    public static void load() {
        c1 = MyPrivateInnerClass.class;
        c2 = MyProtectedInnerClass.class;
        c3 = MyPrivateStaticInnerClass.class;
        c4 = MyProtectedStaticInnerClass.class;
    }
}
