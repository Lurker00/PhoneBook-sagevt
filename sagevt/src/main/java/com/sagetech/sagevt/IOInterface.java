package com.sagetech.sagevt;

public interface IOInterface {
    int cleanOld();

    int getVersion();

    int sendTouch(String name, int x, int y, boolean isDown);
}
