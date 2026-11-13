package com.chuxin.wxpoc;

import de.robv.android.xposed.XC_MethodHook;

/**
 * 观测回调: 全部 before-only, 截断参数, 每钩子上限 300 条(防日志洪水)。
 * 撤回事件 = 系统消息内容含 "撤回"/"revoke" → 打 RECALL 标记。
 */
public class Observe {

    static String clip(Object o) {
        if (o == null) return "null";
        String s = String.valueOf(o);
        return s.length() > 90 ? s.substring(0, 90) + "…" : s;
    }

    static int[] counter(Object tag) {
        // 简单每钩子计数(静态表, POC 规模够用)
        return new int[]{0};
    }

    static class sysmsg extends XC_MethodHook {
        int n = 0;

        @Override
        public void beforeHookedMethod(MethodHookParam p) throws Throwable {
            if (n++ > 300) return;
            Object a0 = p.args.length > 0 ? p.args[0] : null;
            Object a1 = p.args.length > 1 ? p.args[1] : null;
            Object a2 = p.args.length > 2 ? p.args[2] : null;
            String flag = "";
            for (Object a : p.args)
                if (a != null && String.valueOf(a).contains("撤回")) { flag = " ★RECALL★"; break; }
            Diag.log("EVT sysmsg d(" + clip(a0) + ", " + clip(a1) + ", " + clip(a2) + ")" + flag);
        }
    }

    static class sns extends XC_MethodHook {
        final String tag;
        int n = 0;

        sns(String tag) { this.tag = tag; }

        @Override
        public void beforeHookedMethod(MethodHookParam p) throws Throwable {
            if (n++ > 300) return;
            StringBuilder sb = new StringBuilder("EVT " + tag + " " + p.method.getName() + "(");
            for (int i = 0; i < p.args.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(clip(p.args[i]));
            }
            sb.append(")");
            Diag.log(sb.toString());
        }
    }
}
