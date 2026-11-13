package com.chuxin.wxpoc;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_LoadPackage;

/**
 * POC 入口: 只进微信主进程, 全本地零网络, 全部 before 观测不碰返回值。
 * 范围: 系统消息解析(撤回事件源) + 朋友圈 DB 操作 + 诊断日志。
 */
public class Entry implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpp) throws Throwable {
        if (!"com.tencent.mm".equals(lpp.packageName)) return;
        if (!"com.tencent.mm".equals(lpp.processName)) return; // 主进程才挂

        Diag.init(lpp);
        Diag.log("LOAD pkg=" + lpp.packageName + " proc=" + lpp.processName + " module=" + lpp.modulePath);
        try {
            Anchors.run(lpp);
        } catch (Throwable t) {
            Diag.log("FATAL anchors: " + t);
        }
    }
}
