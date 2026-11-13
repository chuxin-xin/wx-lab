// 编译期桩: 运行时由 LSPosed 提供
package de.robv.android.xposed;

import android.app.Application;
import java.lang.reflect.Member;
import java.util.List;

public class XposedHelpers {
    public static Class<?> findClass(String name, ClassLoader cl) { return null; }
    public static Class<?> findClassExact(String name, ClassLoader cl) { return null; }
    public static Class<?> findClassStartsWith(String prefix, ClassLoader cl) { return null; }
    public static List<Class<?>> findClasses(String filter, ClassLoader cl) { return null; }
    public static IXUnhook findAndHookMethod(Class<?> cls, String name, Object... params) { return null; }
    public static IXUnhook findAndHookMethod(String className, ClassLoader cl, String name, Object... params) { return null; }
    public static void hookAllMethodsWithGivenParameters(Member m, Object... params) { }
    public static Object getFieldValue(Object obj, String field) { return null; }
    public static Object getFieldValue(Object obj, String field, Object defaultValue) { return defaultValue; }
    public static void setStaticField(Member f, Object value) { }
    public static Application getApplication() { return null; }
    public static String getCallingPackage() { return null; }
    public static boolean isClassLoaded(String name, ClassLoader cl) { return false; }
}
