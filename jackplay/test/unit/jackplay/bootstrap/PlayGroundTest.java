package unit.jackplay.bootstrap;

import jackplay.bootstrap.PlayGround;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlayGroundTest {

    @Test
    public void testNoPackage() {
        PlayGround noPackage = new PlayGround("MyAbstractClass.myFunction()");

        assertEquals("MyAbstractClass", noPackage.classFullName);
        assertEquals("", noPackage.packageName);
        assertEquals("MyAbstractClass.myFunction()", noPackage.methodFullName);
        assertEquals("MyAbstractClass.myFunction", noPackage.methodLongName);
        assertEquals("myFunction", noPackage.methodShortName);
        assertEquals("", noPackage.parameters);
    }

    @Test
    public void testPackage() {
        PlayGround withPackage = new PlayGround("mypackage.utils.MyAbstractClass.myFunction()");

        assertEquals("mypackage.utils.MyAbstractClass", withPackage.classFullName);
        assertEquals("mypackage.utils", withPackage.packageName);
        assertEquals("mypackage.utils.MyAbstractClass.myFunction()", withPackage.methodFullName);
        assertEquals("mypackage.utils.MyAbstractClass.myFunction", withPackage.methodLongName);
        assertEquals("myFunction", withPackage.methodShortName);
    }

    @Test
    public void testDollarSign() {
        PlayGround withDollarSign = new PlayGround("mypackage.utils.My$Class.myFunction()");

        assertEquals("mypackage.utils.My$Class", withDollarSign.classFullName);
        assertEquals("mypackage.utils", withDollarSign.packageName);
        assertEquals("mypackage.utils.My$Class.myFunction()", withDollarSign.methodFullName);
        assertEquals("mypackage.utils.My$Class.myFunction", withDollarSign.methodLongName);
        assertEquals("myFunction", withDollarSign.methodShortName);
    }

    @Test
    public void testWithArguments() {
        PlayGround withArguments = new PlayGround("mypackage.utils.My$Class.myFunction(java.lang.String,int)");

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
        PlayGround withArrayArguments = new PlayGround("mypackage.utils.My$Class.myFunction(java.lang.String[])");

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
        PlayGround pg = new PlayGround("mypackage.MyAbstractClass.test( )");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInvalidFunctionParens() {
        PlayGround pg = new PlayGround("mypackage.MyAbstractClass.test(]");
    }
}
