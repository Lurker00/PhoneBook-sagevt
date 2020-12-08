package name.lurker.forsagevt;

public final class DeviceControl extends name.lurker.lowlevel.DeviceControl {

    private static DeviceControl instance;
    static public DeviceControl getInstance() {
        synchronized (DeviceControl.class) {
            if (instance == null) {
                instance = new DeviceControl();
            }
            return instance;
        }
    }

    public void setOptions(final Options options) {
        setLandscapeMode(options.getLandscapeMode());
        setDisableHWOverlays(options.getDisableHWOverlays());
        setSize(options.getSize());
        setDensity(options.getDensity());
        if (options.getDimScreen()) dimScreen();
        setIME(options.getIME());
    }
}
