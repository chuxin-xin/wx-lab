// 编译期桩
package de.robv.android.xposed;

import java.lang.reflect.Method;

public abstract class XC_MethodHook {
    public abstract void beforeHookedMethod(MethodHookParam param) throws Throwable;
    public void afterHookedMethod(MethodHookParam param) throws Throwable { }

    public static class MethodHookParam {
        public Method method;
        public Object thisObject;
        public Object[] args;
        public int methodSerial;
        public Object result;
        public Object getResult() { return result; }
        public void setResult(Object r) { result = r; }
        public void setArg(int i, Object v) { args[i] = v; }
        public String getMethodName() { return method == null ? "?" : method.getName(); }
        public String getClassName() { return method == null ? "?" : method.getDeclaringClass().getName(); }
    }
}
