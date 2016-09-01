package fortest.myapp;

import java.io.*;


public class MyClassLoader extends ClassLoader{

    public MyClassLoader(){
        super(MyClassLoader.class.getClassLoader());
    }

    public byte[] findClassBytes(String classShortName){

        try{
            InputStream resourceStream = MyClassLoader.class.getResourceAsStream("/classes/fortest/dynaloaded/" + classShortName + ".class");
            byte[] classBytes = new byte[resourceStream.available()];
            resourceStream.read(classBytes);
            return classBytes;
        } catch (java.io.IOException ioEx){
            return null;
        }
    }

    public Class findClass(String name) throws ClassNotFoundException{

        byte[] classBytes = findClassBytes(name);
        if (classBytes==null){
            throw new ClassNotFoundException();
        }
        else{
            return defineClass("fortest.dynaloaded." + name, classBytes, 0, classBytes.length);
        }
    }
}