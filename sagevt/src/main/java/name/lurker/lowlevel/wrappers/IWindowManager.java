package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import android.graphics.Point;
import android.os.IInterface;
import android.view.Display;
import android.view.IRotationWatcher;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;

public final class IWindowManager extends Wrapper {
    private Method getRotationMethod;

    public int getRotation() {
        try {
            if (getRotationMethod == null) {
                Class<?> cls = manager.getClass();
                try {
                    getRotationMethod = cls.getMethod("getRotation");
                } catch (NoSuchMethodException e) {
                    // method changed since this commit:
                    // https://android.googlesource.com/platform/frameworks/base/+/8ee7285128c3843401d4c4d0412cd66e86ba49e3%5E%21/#F2
                    getRotationMethod = cls.getMethod("getDefaultDisplayRotation");
                }
            }
            return (Integer)getRotationMethod.invoke(manager);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public void registerRotationWatcher(IRotationWatcher rotationWatcher) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("watchRotation", IRotationWatcher.class).invoke(manager, rotationWatcher);
            } catch (NoSuchMethodException e) {
                // display parameter added since this commit:
                // https://android.googlesource.com/platform/frameworks/base/+/35fa3c26adcb5f6577849fd0df5228b1f67cf2c6%5E%21/#F1
                cls.getMethod("watchRotation", IRotationWatcher.class, int.class).invoke(manager, rotationWatcher, 0);
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public int getBaseDisplayDensity() {
        try {
            Class<?> cls = manager.getClass();
            try {
                Object ret = cls.getMethod("getBaseDisplayDensity", int.class).invoke(manager, Display.DEFAULT_DISPLAY);
                return (Integer)ret;
            } catch (NoSuchMethodException e) {
                Log.e("getBaseDisplayDensity", e);
            }
        } catch (Exception e) {
            Log.e("getBaseDisplayDensity", e);
        }
        return -1;
    }

    public int getInitialDisplayDensity() {
        try {
            Class<?> cls = manager.getClass();
            try {
                Object ret = cls.getMethod("getInitialDisplayDensity", int.class).invoke(manager, Display.DEFAULT_DISPLAY);
                return (Integer)ret;
            } catch (NoSuchMethodException e) {
                Log.e("getInitialDisplayDensity", e);
            }
        } catch (Exception e) {
            Log.e("getInitialDisplayDensity", e);
        }
        return -1;
    }

    public void setForcedDisplayDensity(int density) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("setForcedDisplayDensityForUser", int.class, int.class, int.class).invoke(manager, Display.DEFAULT_DISPLAY, density, USER_CURRENT);
            } catch (NoSuchMethodException e) {
                cls.getMethod("setForcedDisplayDensity", int.class, int.class).invoke(manager, Display.DEFAULT_DISPLAY, density);
            }
        } catch (Exception e) {
            Log.e("setForcedDisplayDensity", e);
        }
    }

    public void clearForcedDisplayDensity() {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("clearForcedDisplayDensityForUser", int.class, int.class).invoke(manager, Display.DEFAULT_DISPLAY, USER_CURRENT);
            } catch (NoSuchMethodException e) {
                Log.e("clearForcedDisplayDensity", e);
            }
        } catch (Exception e) {
            Log.e("clearForcedDisplayDensity", e);
        }
    }

    public void getInitialDisplaySize(Point size) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("getInitialDisplaySize", int.class, Point.class).invoke(manager, Display.DEFAULT_DISPLAY, size);
            } catch (NoSuchMethodException e) {
                Log.e("getInitialDisplaySize", e);
            }
        } catch (Exception e) {
            Log.e("getInitialDisplaySize", e);
        }
    }

    public void getBaseDisplaySize(Point size) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("getBaseDisplaySize", int.class, Point.class).invoke(manager, Display.DEFAULT_DISPLAY, size);
            } catch (NoSuchMethodException e) {
                Log.e("getBaseDisplaySize", e);
            }
        } catch (Exception e) {
            Log.e("getBaseDisplaySize", e);
        }
    }

    public void setForcedDisplaySize(int width, int height) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("setForcedDisplaySize", int.class, int.class, int.class).invoke(manager, Display.DEFAULT_DISPLAY, width, height);
            } catch (NoSuchMethodException e) {
                Log.e("setForcedDisplaySize", e);
            }
        } catch (Exception e) {
            Log.e("setForcedDisplaySize", e);
        }
    }

    public void clearForcedDisplaySize() {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("clearForcedDisplaySize", int.class).invoke(manager, Display.DEFAULT_DISPLAY);
            } catch (NoSuchMethodException e) {
                Log.e("clearForcedDisplaySize", e);
            }
        } catch (Exception e) {
            Log.e("clearForcedDisplaySize", e);
        }
    }

    public void freezeRotation(int rotation) {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("freezeRotation", int.class).invoke(manager, rotation);
            } catch (NoSuchMethodException e) {
                Log.e("freezeRotation", e);
            }
        } catch (Exception e) {
            Log.e("freezeRotation", e);
        }
    }

    public void thawRotation() {
        try {
            Class<?> cls = manager.getClass();
            try {
                cls.getMethod("thawRotation").invoke(manager);
            } catch (NoSuchMethodException e) {
                Log.e("thawRotation", e);
            }
        } catch (Exception e) {
            Log.e("thawRotation", e);
        }
    }

    public boolean isRotationFrozen() {
        try {
            Class<?> cls = manager.getClass();
            try {
                return (Boolean) cls.getMethod("isRotationFrozen").invoke(manager);
            } catch (NoSuchMethodException e) {
                Log.e("isRotationFrozen", e);
            }
        } catch (Exception e) {
            Log.e("isRotationFrozen", e);
        }
        return false;
    }

    private IWindowManager(IInterface manager) {
        super(manager);
    }

    static private IWindowManager instance;
    static public IWindowManager getInstance() {
        synchronized (IWindowManager.class) {
            if (instance == null) {
                instance = new IWindowManager(getService("window", "android.view.IWindowManager"));
            }
            return instance;
        }
    }

    static public android.view.WindowManager getWindowManager(android.content.Context context) {
        try {
            return (android.view.WindowManager)
                getClass("android.view.WindowManagerImpl").getConstructor(new Class[]{android.content.Context.class}).newInstance(context);

        } catch (Exception e) {
            Log.e("getWindowManager", e);
        }
        return null;
    }
}
