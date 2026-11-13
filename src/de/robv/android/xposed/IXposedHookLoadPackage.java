// 编译期桩
package de.robv.android.xposed;

public interface IXposedHookLoadPackage {
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable;
    static void unhookLoadPackageApplication() { }
}
