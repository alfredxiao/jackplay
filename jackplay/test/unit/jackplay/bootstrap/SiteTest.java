package unit.jackplay.bootstrap;

import jackplay.bootstrap.Site;
import org.junit.Test;

import static org.junit.Assert.*;

public class SiteTest {

    @Test
    public void testNoPackage() {
        Site noPackage = new Site("MyClass.myFunction(int)");

        assertEquals("MyClass", noPackage.classFullName);
        assertEquals("", noPackage.packageName);
        assertEquals("MyClass.myFunction(int)", noPackage.methodFullName);
        assertEquals("MyClass.myFunction", noPackage.methodLongName);
        assertEquals("myFunction", noPackage.methodShortName);
        assertEquals("myFunction(int)", noPackage.methodShortNameWithSignature);
        assertEquals("int", noPackage.parameters);
    }

    @Test
    public void testPackage() {
        Site withPackage = new Site("mypackage.utils.MyClass.myFunction()");

        assertEquals("mypackage.utils.MyClass", withPackage.classFullName);
        assertEquals("mypackage.utils", withPackage.packageName);
        assertEquals("mypackage.utils.MyClass.myFunction()", withPackage.methodFullName);
        assertEquals("mypackage.utils.MyClass.myFunction", withPackage.methodLongName);
        assertEquals("myFunction", withPackage.methodShortName);
    }

    @Test
    public void testDollarSign() {
        Site withDollarSign = new Site("mypackage.utils.My$Class.myFunction()");

        assertEquals("mypackage.utils.My$Class", withDollarSign.classFullName);
        assertEquals("mypackage.utils", withDollarSign.packageName);
        assertEquals("mypackage.utils.My$Class.myFunction()", withDollarSign.methodFullName);
        assertEquals("mypackage.utils.My$Class.myFunction", withDollarSign.methodLongName);
        assertEquals("myFunction", withDollarSign.methodShortName);
    }

    @Test
    public void testWithArguments() {
        Site withArguments = new Site("mypackage.utils.My$Class.myFunction(java.lang.String,int)");

        assertEquals("mypackage.utils.My$Class", withArguments.classFullName);
        assertEquals("mypackage.utils", withArguments.packageName);
        assertEquals("mypackage.utils.My$Class.myFunction(java.lang.String,int)", withArguments.methodFullName);
        assertEquals("mypackage.utils.My$Class.myFunction", withArguments.methodLongName);
        assertEquals("myFunction", withArguments.methodShortName);
        assertEquals("java.lang.String,int", withArguments.parameters);
        assertEquals(2, withArguments.parameterList.size());
        assertEquals("java.lang.String", withArguments.parameterList.get(0));
        assertEquals("int", withArguments.parameterList.get(1));
    }

    @Test
    public void testWithArrayArguments() {
        Site withArrayArguments = new Site("mypackage.utils.My$Class.myFunction(java.lang.String[])");

        assertEquals("mypackage.utils.My$Class", withArrayArguments.classFullName);
        assertEquals("mypackage.utils", withArrayArguments.packageName);
        assertEquals("mypackage.utils.My$Class.myFunction(java.lang.String[])", withArrayArguments.methodFullName);
        assertEquals("mypackage.utils.My$Class.myFunction", withArrayArguments.methodLongName);
        assertEquals("myFunction", withArrayArguments.methodShortName);
        assertEquals("java.lang.String[]", withArrayArguments.parameters);
        assertTrue(withArrayArguments.parameterList.contains("java.lang.String[]"));
        assertEquals(1, withArrayArguments.parameterList.size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidSpaceCharacter() {
        Site pg = new Site("mypackage.MyClass.test( )");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidFunctionParens() {
        Site pg = new Site("mypackage.MyClass.test(]");
    }
}
