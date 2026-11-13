package com.chuxin.wxpoc;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

/**
 * 本地文件日志: 模块 data 目录 /data/data/com.chuxin.wxpoc/files/poc.log
 * (SSH root 可读; 2MB 截断不轮转, POC 够用)。
 */
public class Diag {
    private static final int MAX = 2_000_000;
    private static File f;
    private static FileWriter w;

    public static void init(de.robv.android.xposed.XC_LoadPackage.LoadPackageParam lpp) {
        try {
            f = new File(lpp.module.application.getFilesDir(), "poc.log");
            if (f.exists() && f.length() > MAX) { // 截断
                w = new FileWriter(f, false);
                w.write("# truncated\n");
                w.close();
                w = null;
            }
            w = new FileWriter(f, true);
        } catch (Throwable ignored) { }
    }

    public static synchronized void log(String msg) {
        try {
            if (w == null) return;
            Calendar c = Calendar.getInstance();
            int hh = c.get(Calendar.HOUR_OF_DAY), mm = c.get(Calendar.MINUTE), ss = c.get(Calendar.SECOND);
            w.write(String.format("%02d:%02d:%02d %s%n", hh, mm, ss, msg));
            w.flush();
        } catch (Throwable ignored) { }
    }

    public static String path() {
        return f == null ? "?" : f.getAbsolutePath();
    }
}
