package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IInterface;

import java.lang.reflect.Method;

public final class IActivityManager extends Wrapper {
    private Method broadcastIntentMethod;

    public static final int OP_NONE = -1;
    private static final String SHELL_PACKAGE_NAME = "com.android.shell";

/*
    int broadcastIntent(IApplicationThread caller, Intent intent,
                        String resolvedType, IIntentReceiver resultTo, int resultCode,
                        String resultData, Bundle map, String[] requiredPermissions,
                        int appOp, Bundle options, boolean serialized, boolean sticky, int userId);
*/
    public boolean broadcastIntent(Intent intent) {
        Class<?> cls = manager.getClass();
        if (broadcastIntentMethod == null) {
            try {
                Class<?> IApplicationThread = Class.forName("android.app.IApplicationThread");
                Class<?> IIntentReceiver    = Class.forName("android.content.IIntentReceiver");
                broadcastIntentMethod = cls.getMethod("broadcastIntent"
                        , IApplicationThread, Intent.class
                        , String.class, IIntentReceiver, int.class
                        , String.class, Bundle.class, String[].class
                        , int.class, Bundle.class, boolean.class, boolean.class, int.class);
                broadcastIntentMethod.setAccessible(true);
            } catch (Exception e) {
                Log.e("broadcastIntent", e);
            }
        }
        if (broadcastIntentMethod == null) return false;
        try {
            broadcastIntentMethod.invoke(manager,
                    null, intent,
                    null, null/* receiver*/, 0,
                    null, null, null,
                    OP_NONE, null, true, false, USER_CURRENT);
            return true;
        } catch (Exception e) {
            Log.e("broadcastIntent.invoke", e);
        }
        return false;
    }
/*
ComponentName startService(in IApplicationThread caller, in Intent service,
            in String resolvedType, boolean requireForeground,
            in String callingPackage, int userId);
 */
    public boolean startService(final String service) {
        try {
            Class<?> cls = manager.getClass();
            Intent intent = new Intent();
            ComponentName cn = ComponentName.unflattenFromString(service);
            if (cn == null) {
                Log.e("Bad component name:"+service);
                return false;
            }
            intent.setComponent(cn);
            Class<?> IApplicationThread = Class.forName("android.app.IApplicationThread");
            try {
                Object ret = cls.getMethod("startService", IApplicationThread, Intent.class, String.class, boolean.class, String.class, int.class).invoke(manager
                        , null, intent
                        , intent.getType(), false
                        , SHELL_PACKAGE_NAME, USER_CURRENT);
                cn = (ComponentName)ret;
            } catch (NoSuchMethodException e) {
                Object ret = cls.getMethod("startService", IApplicationThread, Intent.class, String.class, String.class, int.class).invoke(manager
                        , null, intent
                        , intent.getType()
                        , SHELL_PACKAGE_NAME, USER_CURRENT);
                cn = (ComponentName)ret;
            }
            if (cn == null) {
                Log.e("Error: Not found; no service started.");
                return false;
            } else if (cn.getPackageName().equals("!")) {
                Log.e("Error: Requires permission " + cn.getClassName());
                return false;
            } else if (cn.getPackageName().equals("!!")) {
                Log.e("Error: " + cn.getClassName());
                return false;
            }
            return true;
        } catch (Exception e) {
            Log.e("startService", e);
        }
        return false;
    }

    private IActivityManager(IInterface manager) {
        super(manager);
    }

    static private IActivityManager instance;
    static public IActivityManager getInstance() {
        synchronized (IActivityManager.class) {
            if (instance == null) {
                instance = new IActivityManager(getService("activity", "android.app.IActivityManager"));
            }
            return instance;
        }
    }
}
