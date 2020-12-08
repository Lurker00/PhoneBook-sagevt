package com.sagetech.sagevt;

import name.lurker.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class SGVTLog extends Log {
    private static final init TAG = new init("sagevt");
    public static boolean forPC = false;

    public static void msg(String str) {
        Log.i(str);
    }

    public static void err(String str) {
        Log.e(str);
    }

    public static void err(String str, Throwable e) {
        Log.e(str, e);
    }

    public static void exception(Throwable e) {
        Log.e("Exception:", e);
    }
}
