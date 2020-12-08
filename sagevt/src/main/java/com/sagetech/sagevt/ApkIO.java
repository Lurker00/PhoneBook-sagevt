package com.sagetech.sagevt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ApkIO implements InvocationHandler {
    private ObjectInputStream input = null;
    private ObjectOutputStream output = null;
    private Socket socket = null;

    private int connect() {
        this.socket = new Socket();
        try {
            this.socket.connect(new InetSocketAddress("127.0.0.1", 25112));
            this.output = new ObjectOutputStream(this.socket.getOutputStream());
            this.input = new ObjectInputStream(this.socket.getInputStream());
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int deleteClient() {
        try {
            this.socket.close();
        } catch (Exception e) {
        }
        try {
            this.output.close();
        } catch (Exception e) {
        }
        try {
            this.input.close();
        } catch (Exception e) {
        }
        return 0;
    }

    public IOInterface newClientInstance() {
        if (connect() != 0) {
            return null;
        }
        return (IOInterface) Proxy.newProxyInstance(IOInterface.class.getClassLoader(), new Class[]{IOInterface.class}, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        this.output.writeUTF(method.getName());
        this.output.writeObject(method.getParameterTypes());
        this.output.writeObject(args);
        return this.input.readObject();
    }
}
