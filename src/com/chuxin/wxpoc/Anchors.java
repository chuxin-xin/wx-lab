package com.chuxin.wxpoc;

import android.app.Application;
import android.content.pm.PackageInfo;
import de.robv.android.xposed.IXUnhook;
import de.robv.android.xposed.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 锚点加载器: 读 assets/anchors.json (自定 schema) → 按宿主版本取类名表 → 装观测 hook。
 * 设计(借 lexin 思路, 代码自写):
 *  - 版本表精确匹配 versionName; 不在表里 = 只记日志不挂 hook (安全失败)
 *  - 短类名(无点)条目 = 混淆短名, POC 直接跳过并记录 (完整支持需要 dex 自扫, 下一版)
 *  - 全部 before 观测, 不碰 result/args 语义
 */
public class Anchors {

    static final class Plan {
        String version;
        String sysmsgClass;
        List<String> snsClasses = new ArrayList<>();
    }

    public static void run(XC_LoadPackage.LoadPackageParam lpp) throws Exception {
        String json = readAnchors(lpp.module.application);
        JSONObject root = new JSONObject(json);
        String host = hostVersion(lpp);
        Diag.log("HOST wechat versionName=" + host);

        JSONObject vers = root.getJSONObject("versions");
        JSONObject slots = root.getJSONObject("slots");
        Plan p = resolve(vers, host);
        if (p == null) {
            Diag.log("VERSION-MISS host=" + host + " (表: " + vers.names() + ") → 不挂 hook, POC 静默");
            return;
        }

        int hooks = 0, skipped = 0;

        // ---- 系统消息解析器(撤回事件源) ----
        if (p.sysmsgClass != null) {
            String[] m = parseMethod(slots.getString("sysmsg_parser"));
            Object[] cls = resolveClasses(m);
            Class<?> c = XposedHelpers.findClass(p.sysmsgClass, lpp.classLoader);
            if (c == null) {
                Diag.log("MISS slot=sysmsg_parser class=" + p.sysmsgClass);
            } else {
                IXUnhook u = XposedHelpers.findAndHookMethod(c, m[0], append(cls, new Observe.sysmsg()));
                if (u != null) { hooks++; Diag.log("HOOK sysmsg_parser@" + c.getName() + "#" + m[0]); }
                else { Diag.log("HOOK-FAIL sysmsg_parser"); }
            }
        } else skipped++;

        // ---- 朋友圈 DB(删除观测) ----
        for (String tag : new String[]{"sns_db", "sns_db2"}) {
            String[] m = parseMethod(slots.getString(tag));
            Object[] cls = resolveClasses(m);
            for (String cn : p.snsClasses) {
                if (cn.indexOf('.') < 0) { skipped++; Diag.log("SKIP shortname " + tag + "=" + cn); continue; }
                Class<?> c = XposedHelpers.findClass(cn, lpp.classLoader);
                if (c == null) { Diag.log("MISS slot=" + tag + " class=" + cn); continue; }
                IXUnhook u = XposedHelpers.findAndHookMethod(c, m[0], append(cls, new Observe.sns(tag)));
                if (u != null) { hooks++; Diag.log("HOOK " + tag + "@" + cn + "#" + m[0]); }
                else Diag.log("HOOK-FAIL " + tag + "@" + cn);
            }
        }

        Diag.log("REPORT hooks=" + hooks + " skipped=" + skipped + " version=" + p.version);
    }

    static Plan resolve(JSONObject vers, String host) throws org.json.JSONException {
        if (!vers.has(host)) return null;
        JSONObject v = vers.optJSONObject(host);
        if (v == null) return null;
        JSONObject cls = v.optJSONObject("classes");
        if (cls == null) return null;
        Plan p = new Plan();
        p.version = host;
        p.sysmsgClass = cls.optString("sysmsg_parser", null);
        for (String tag : new String[]{"sns_db", "sns_db2"}) {
            if (cls.has(tag)) {
                Object o = cls.get(tag);
                if (o instanceof String) p.snsClasses.add((String) o);
                else if (o instanceof org.json.JSONArray)
                    for (Object e : (List<Object>) o) p.snsClasses.add(String.valueOf(e));
            }
        }
        return p;
    }

    static String hostVersion(XC_LoadPackage.LoadPackageParam lpp) {
        try {
            Application app = (Application) lpp.application;
            PackageInfo pi = app.getPackageManager().getPackageInfo(lpp.packageName, 0);
            return pi.versionName;
        } catch (Throwable t) {
            return "unknown(" + t + ")";
        }
    }

    static String readAnchors(Application app) throws Exception {
        java.io.InputStream in = app.getAssets().open("anchors.json");
        byte[] buf = new byte[16384];
        int n = in.read(buf);
        in.close();
        return new String(buf, 0, n, "UTF-8");
    }

    /** "d(java.lang.String,java.lang.String,java.lang.String):java.util.Map" → [name, 参数类...] */
    static String[] parseMethod(String spec) {
        int paren = spec.indexOf('(');
        int colon = spec.lastIndexOf(')');
        String name = spec.substring(0, paren);
        String inner = spec.substring(paren + 1, colon).trim();
        if (inner.isEmpty()) return new String[]{name};
        String[] args = inner.split(",");
        String[] out = new String[args.length + 1];
        out[0] = name;
        for (int i = 0; i < args.length; i++) out[i + 1] = args[i].trim();
        return out;
    }

    static Object[] resolveClasses(String[] parts) throws Exception {
        Class<?>[] cs = new Class<?>[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            switch (parts[i]) {
                case "java.lang.String": cs[i - 1] = String.class; break;
                case "java.lang.Object": cs[i - 1] = Object.class; break;
                case "java.lang.Object[]": cs[i - 1] = Object[].class; break;
                case "int": cs[i - 1] = int.class; break;
                case "boolean": cs[i - 1] = boolean.class; break;
                case "java.util.Map": cs[i - 1] = java.util.Map.class; break;
                case "java.lang.Boolean": cs[i - 1] = Boolean.class; break;
                case "java.lang.Integer": cs[i - 1] = Integer.class; break;
                default: throw new IllegalStateException("未实现的参数类型: " + parts[i]);
            }
        }
        return cs;
    }

    static Object[] append(Object[] arr, Object extra) {
        Object[] out = java.util.Arrays.copyOf(arr, arr.length + 1);
        out[arr.length] = extra;
        return out;
    }
}
