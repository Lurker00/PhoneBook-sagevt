package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import android.os.IInterface;
import android.view.Display;

public final class IDisplayManager extends Wrapper {

    static public Display getDefaultDisplay() {
        try {
            Class<?> cls = getClass("android.hardware.display.DisplayManagerGlobal"); 
            Object o = cls.getMethod("getInstance").invoke(null);
            return (Display)cls.getMethod("getRealDisplay", int.class).invoke(o, Display.DEFAULT_DISPLAY);

        } catch (Exception e) {
            Log.e("getDefaultDisplay", e);
        }
        return null;
    }

    private IDisplayManager(IInterface manager) {
        super(manager);
    }

    static private IDisplayManager instance;
    static public IDisplayManager getInstance() {
        synchronized (IDisplayManager.class) {
            if (instance == null) {
                instance = new IDisplayManager(getService("display", "android.hardware.display.IDisplayManager"));
            }
            return instance;
        }
    }

    static public android.hardware.display.DisplayManager getDisplayManager(android.content.Context context) {
        try {
            return (android.hardware.display.DisplayManager)
                getClass("android.hardware.display.DisplayManager").getConstructor(new Class[]{android.content.Context.class}).newInstance(context);

        } catch (Exception e) {
            Log.e("getDisplayManager", e);
        }
        return null;
    }
}
