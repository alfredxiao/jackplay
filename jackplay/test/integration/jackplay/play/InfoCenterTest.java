package integration.jackplay.play;

import fortest.myapp.MyBaseClass;
import fortest.myapp.MyClass;
import fortest.myapp.MyClassLoader;
import jackplay.TheatreRep;
import jackplay.bootstrap.Site;
import jackplay.play.InfoCenter;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class InfoCenterTest {
    InfoCenter infoCenter = TheatreRep.getInfoCenter();
    final Class MYBASECLASS = MyBaseClass.class;
    final Class MYCLASS = MyClass.class;
    final Class JAVA_UTIL_ARRAY_LIST = java.util.ArrayList.class;

    @Test
    public void shouldFindLoadedClasses() {
        List<Class> myclasses = infoCenter.findLoadedModifiableClasses("fortest.myapp.MyBaseClass");
        assertEquals(1, myclasses.size());
        assertEquals(MYBASECLASS, myclasses.get(0));
    }

    @Test
    public void shouldFindMatchingMethod() {
        Method myfunction1 = infoCenter.findMatchingMethod(MyBaseClass.class, new Site("fortest.myapp.MyBaseClass.test1(int,java.lang.String)"));
        assertNotNull(myfunction1);
        assertEquals("test1", myfunction1.getName());

        Method myfunction2 = infoCenter.findMatchingMethod(MyBaseClass.class, new Site("fortest.myapp.MyBaseClass.test2(java.lang.Object,java.util.List)"));
        assertNotNull(myfunction2);
        assertEquals("test2", myfunction2.getName());

        Method myfunction3 = infoCenter.findMatchingMethod(MyBaseClass.class, new Site("fortest.myapp.MyBaseClass.test3(java.lang.Object[],int[][])"));
        assertNotNull(myfunction3);
        assertEquals("test3", myfunction3.getName());
    }

    @Test
    public void shouldGiveLoadedMethods() throws Exception {
        Map<String, Map<String, String>> loadedMethods = infoCenter.allModifiableMethods();

        assertEquals("java.lang.String", loadedMethods.get("fortest.myapp.MyBaseClass").get("test1(int,java.lang.String)"));

        assertEquals("void", loadedMethods.get("java.util.ArrayList").get("clear()"));
    }

    @Test
    public void shouldRecogniseExistenceOfMethodBody() {
        Method myAbstract = infoCenter.findMatchingMethod(MyBaseClass.class, new Site("fortest.myapp.MyBaseClass.myAbstract()"));
        Method myNative = infoCenter.findMatchingMethod(MyBaseClass.class, new Site("fortest.myapp.MyBaseClass.myNative()"));
        Method myfunction2 = infoCenter.findMatchingMethod(MyBaseClass.class, new Site("fortest.myapp.MyBaseClass.test2(java.lang.Object,java.util.List)"));

        assertFalse(infoCenter.hasMethodBody(myAbstract));
        assertFalse(infoCenter.hasMethodBody(myNative));
        assertTrue(infoCenter.hasMethodBody(myfunction2));
    }

    @Test
    public void shouldIncludePrivateInnerClass() throws Exception {
        MyClass.load();

        List<Class> privateInnerClasses = infoCenter.findLoadedModifiableClasses("fortest.myapp.MyClass$MyPrivateInnerClass");
        assertEquals(1, privateInnerClasses.size());

        Method test1 = infoCenter.findMatchingMethod(privateInnerClasses.get(0), new Site("fortest.myapp.MyClass$MyPrivateInnerClass.test1()"));
        assertNotNull(test1);

        List<Class> privateStaticInnerClasses = infoCenter.findLoadedModifiableClasses("fortest.myapp.MyClass$MyPrivateStaticInnerClass");
        assertEquals(1, privateStaticInnerClasses.size());

        Method test2 = infoCenter.findMatchingMethod(privateStaticInnerClasses.get(0), new Site("fortest.myapp.MyClass$MyPrivateStaticInnerClass.test3()"));
        assertNotNull(test2);
    }

    @Test
    public void shouldIncludeProtectedInnerClass() throws Exception {
        MyClass.load();
        assertEquals(1, infoCenter.findLoadedModifiableClasses("fortest.myapp.MyClass$MyProtectedInnerClass").size());
        assertEquals(1, infoCenter.findLoadedModifiableClasses("fortest.myapp.MyClass$MyProtectedStaticInnerClass").size());
    }

    @Test
    public void shouldNotUseCanonicalNameForClassName() throws Exception {
        MyClass.load();
        assertEquals(1, infoCenter.findLoadedModifiableClasses("fortest.myapp.MyClass$MyProtectedInnerClass").size());
        assertEquals(0, infoCenter.findLoadedModifiableClasses("fortest.myapp.MyClass.MyProtectedInnerClass").size());
    }

    @Test
    public void shouldFindClassesLoadedByCustomClassLoader() throws Exception {
        MyClassLoader loader = new MyClassLoader();
        Class clz = loader.findClass("CustomLoadedClass");

        List<Class> customLoadedClasses = infoCenter.findLoadedModifiableClasses("fortest.dynaloaded.CustomLoadedClass");
        assertEquals(1, customLoadedClasses.size());

        Method test1 = infoCenter.findMatchingMethod(customLoadedClasses.get(0), new Site("fortest.dynaloaded.CustomLoadedClass.test1(java.lang.String)"));
        assertNotNull(test1);
    }
}
