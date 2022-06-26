package com.hc;

import org.apache.hadoop.hive.ql.hooks.ExecuteWithHookContext;
import org.apache.hadoop.hive.ql.hooks.HookContext;

public class HookTest implements ExecuteWithHookContext {
    @Override
    public void run(HookContext hookContext) throws Exception {
        System.out.println("the first pre hook");
    }
}