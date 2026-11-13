// 编译期桩
package de.robv.android.xposed;

import android.app.Application;

public interface XC_LoadPackage {
    class LoadPackageParam {
        public String packageName;
        public String processName;
        public ClassLoader classLoader;
        public Application application;
        public String modulePath;
        public XposedModuleContext module;
    }
}
