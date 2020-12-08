package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

@SuppressLint("PrivateApi")
class Wrapper {
    public static final int USER_CURRENT = -2;

    static private Method getServiceMethod;

    final IInterface manager;
    Wrapper(IInterface manager) {
        this.manager = manager;
    }

    public IInterface getManager() { return this.manager; }

    static protected IInterface getService(String service, String type) {
        synchronized (Wrapper.class) {
            if (getServiceMethod == null) {
                try {
                    getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
                } catch (Exception e) {
                    Log.e("getServiceMethod failed:", e);
                    throw new AssertionError(e);
                }
            }

            try {
                IBinder binder = (IBinder) getServiceMethod.invoke(null, service);
                Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
                return (IInterface) asInterfaceMethod.invoke(null, binder);
            } catch (Exception e) {
                Log.e("getService failed:", e);
                throw new AssertionError(e);
            }
        }
    }

    static protected Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            Log.e("getClass '"+name+"'failed:", e);
        }
        return null;
    }

    static protected Method getMethod(Class<?> cls, String name, Class<?> ... types) {
        if (cls == null) return null;
        try {
            return cls.getMethod(name, types);
        } catch (Exception e) {
            Log.e("getMethod '"+cls.getName()+":"+name+"' failed:", e);
            try {
                Method[] methods = cls.getMethods();
                Log.e("Total methods: "+methods.length);
                for (int i = 0; i < methods.length; i++)
                    Log.e(methods[i].toString());
            } catch (Exception e1) {
                Log.e("getMethods failed:", e);
            }
        }
        return null;
    }
}
