package integration.jackplay.play;

import jackplay.TheatreRep;
import jackplay.bootstrap.PlayGround;
import jackplay.play.InfoCenter;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoCenterTest {
    InfoCenter infoCenter = TheatreRep.getInfoCenter();
    final Class MYCLASS = MyClass.class;
    final Class JAVA_UTIL_ARRAY_LIST = java.util.ArrayList.class;

    @Test
    public void shouldFindLoadedClasses() {
        List<Class> myclasses = infoCenter.findLoadedClasses("integration.jackplay.play.MyClass");
        assertEquals(1, myclasses.size());
        assertEquals(MYCLASS, myclasses.get(0));
    }

    @Test
    public void shouldFindMatchingMethod() {
        Method myfunction1 = infoCenter.findMatchingMethod(MyClass.class, new PlayGround("integration.jackplay.play.MyClass.myfunction1(int,java.lang.String)"));
        assertNotNull(myfunction1);
        assertEquals("myfunction1", myfunction1.getName());

        Method myfunction2 = infoCenter.findMatchingMethod(MyClass.class, new PlayGround("integration.jackplay.play.MyClass.myfunction2(java.lang.Object,java.util.List)"));
        assertNotNull(myfunction2);
        assertEquals("myfunction2", myfunction2.getName());

        Method myfunction3 = infoCenter.findMatchingMethod(MyClass.class, new PlayGround("integration.jackplay.play.MyClass.myfunction3(java.lang.Object[],int[][])"));
        assertNotNull(myfunction3);
        assertEquals("myfunction3", myfunction3.getName());
    }

    @Test
    public void shouldGiveLoadedMethods() throws Exception {
        List<Map<String, String>> loadedMethods = infoCenter.getLoadedMethods();

        Map<String, String> myfunction1 = new HashMap<>();
        myfunction1.put("classFullName", "integration.jackplay.play.MyClass");
        myfunction1.put("methodFullName", "integration.jackplay.play.MyClass.myfunction1(int,java.lang.String)");
        myfunction1.put("methodLongName", "integration.jackplay.play.MyClass.myfunction1");
        myfunction1.put("returnType", "java.lang.String");

        assertTrue(loadedMethods.contains(myfunction1));

        Map<String, String> clear = new HashMap<>();
        clear.put("classFullName", "java.util.ArrayList");
        clear.put("methodFullName", "java.util.ArrayList.clear()");
        clear.put("methodLongName", "java.util.ArrayList.clear");
        clear.put("returnType", "void");

        assertTrue(loadedMethods.contains(clear));
    }

    @Test
    public void shouldRecogniseExistenceOfMethodBody() {
        Method myAbstract = infoCenter.findMatchingMethod(MyClass.class, new PlayGround("integration.jackplay.play.MyClass.myAbstract()"));
        Method myNative = infoCenter.findMatchingMethod(MyClass.class, new PlayGround("integration.jackplay.play.MyClass.myNative()"));
        Method myfunction2 = infoCenter.findMatchingMethod(MyClass.class, new PlayGround("integration.jackplay.play.MyClass.myfunction2(java.lang.Object,java.util.List)"));

        assertFalse(infoCenter.hasMethodBody(myAbstract));
        assertFalse(infoCenter.hasMethodBody(myNative));
        assertTrue(infoCenter.hasMethodBody(myfunction2));
    }
}
