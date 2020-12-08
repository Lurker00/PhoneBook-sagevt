package name.lurker.lowlevel.wrappers;

import name.lurker.util.Log;

import android.os.IInterface;

public final class IPackageManager extends Wrapper {

    public boolean isPackageAvailable(String packageName) {
        try {
            Class<?> cls = manager.getClass();
            try {
                return (Boolean) cls.getMethod("isPackageAvailable", String.class, int.class).invoke(manager, packageName, 0);
            } catch (NoSuchMethodException e) {
                Log.e("isPackageAvailable", e);
            }
        } catch (Exception e) {
            Log.e("isPackageAvailable", e);
        }
        return false;
    }

    private IPackageManager(IInterface manager) {
        super(manager);
    }

    static private IPackageManager instance;
    static public IPackageManager getInstance() {
        synchronized (IPackageManager.class) {
            if (instance == null) {
                instance = new IPackageManager(getService("package", "android.content.pm.IPackageManager"));
            }
            return instance;
        }
    }
}
