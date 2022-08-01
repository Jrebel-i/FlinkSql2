package com.hc.calcite_module;

import org.codehaus.commons.compiler.IScriptEvaluator;
import org.codehaus.janino.ScriptEvaluator;

//vm中配置： -Dorg.codehaus.janino.source_debugging.enable=true -Dorg.codehaus.janino.source_debugging.dir=D:\tmp
//并且需要开启debug（因为运行完成之后，文件会被删除）

public class DemoDebug {
    public static void main(String[] args) throws Exception {
        IScriptEvaluator se = new ScriptEvaluator();

        MyInterface d5 = (MyInterface) se.createFastEvaluator(
                ("System.out.println(\"Hello\");;\n" +
                        "        String s =\"World\";\n" +
                        "        System.out.println(s);\n" +
                        "        return true;"
                ),
                MyInterface.class,
                new String[0]
        );

        boolean res=d5.goForIt();
        System.out.println("res = "+res);


    }
}
