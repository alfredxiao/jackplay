package fortest.myapp;


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

    public void invokeInnerClassMethods() {
        MyPrivateInnerClass o1 = new MyPrivateInnerClass();
        o1.test1();

        MyPrivateStaticInnerClass o3 = new MyPrivateStaticInnerClass();
        o3.test3();

        MyProtectedInnerClass o2 = new MyProtectedInnerClass();
        o2.test2();

        MyProtectedStaticInnerClass o4 = new MyProtectedStaticInnerClass();
        o4.test4();
    }
}
