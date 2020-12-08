package com.sagetech.sagevt;

public class Test {
    public static void main(String[] arg) {
        ApkIO io = new ApkIO();
        IOInterface opt = io.newClientInstance();
        if (opt != null) {
            int sendTouch = opt.sendTouch("test", 1024, 1024, true);
            System.out.println("$$$$ret: " + opt.sendTouch("test2", 1024, 10, true));
            io.deleteClient();
        }
    }
}
