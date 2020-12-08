package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import java.lang.reflect.Method;

public class Process extends Wrapper {
    private static final Class<?> cls         = getClass("android.os.Process");
    private static final Method _getParentPid = getMethod(cls, "getParentPid", int.class);
    private static final Method _getUidForPid = getMethod(cls, "getUidForPid", int.class);
    private static final Method _setArgV0     = getMethod(cls, "setArgV0",     String.class);

    static public int getParentPid(int pid) {
        if (_getParentPid == null) return 0;
        try {
            Object ret = _getParentPid.invoke(null, pid);
            return (Integer)ret;
        } catch (Exception e) {
            Log.e("getParentPid failed:", e);
        }
        return -1;
    }

    static public final int getUidForPid(int pid) {
        if (_getUidForPid == null) return 0;
        try {
            Object ret = _getUidForPid.invoke(null, pid);
            return (Integer)ret;
        } catch (Exception e) {
            Log.e("getUidForPid failed:", e);
        }
        return -1;
    }

    static public void setArgV0(String str) {
        if (_setArgV0 == null) return;
        try {
            _setArgV0.invoke(null, str);
        } catch (Exception e) {
            Log.e("setArgV0 failed:", e);
        }
    }

    static public int myPid() {
        return android.os.Process.myPid();
    }

    private Process() { super(null); }
}
