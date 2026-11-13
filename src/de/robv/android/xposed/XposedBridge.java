// 编译期桩: 运行时由 LSPosed 提供真实 de.robv.android.xposed.* (shadow class 优先)
package de.robv.android.xposed;

public class XposedBridge {
    public static IXUnhook hookMethodMethod(java.lang.reflect.Method m, XC_MethodHook c) { return null; }
    public static boolean isHookActive() { return true; }
}
