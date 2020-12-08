package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

public final class InputMethodManager extends Wrapper {

    public boolean setInputMethodEnabled(String id, boolean enabled) {
        // The method was removed in Pie (API 28)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return true;
        try {
            Class<?> cls = manager.getClass();
            try {
                return (Boolean) cls.getMethod("setInputMethodEnabled", String.class, boolean.class).invoke(manager, id, enabled);
            } catch (NoSuchMethodException e) {
                Log.e("setInputMethodEnabled", e);
            }
        } catch (Exception e) {
            Log.e("setInputMethodEnabled", e);
        }
        return true; // Don't try to disable
    }

    public boolean setInputMethod(String id) {
        try {
            Class<?> cls = manager.getClass();
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                // In Android 10, setInputMethod became deprecated in InputMethodManager, and was removed
                // from IInputMethosManager. The deprecated method works essentially this way:
                    Settings.put(Settings.SECURE, Settings.DEFAULT_INPUT_METHOD, id);
                else
                    cls.getMethod("setInputMethod", IBinder.class, String.class).invoke(manager, null, id);
                return true;
            } catch (NoSuchMethodException e) {
                Log.e("setInputMethod", e);
            }
        } catch (Exception e) {
            Log.e("setInputMethod", e);
        }
        return false;
    }

    private InputMethodManager(IInterface manager) {
        super(manager);
    }

    static private InputMethodManager instance;
    static public InputMethodManager getInstance() {
        synchronized (InputMethodManager.class) {
            if (instance == null) {
                instance = new InputMethodManager(getService("input_method", "com.android.internal.view.IInputMethodManager"));
            }
            return instance;
        }
    }
}
