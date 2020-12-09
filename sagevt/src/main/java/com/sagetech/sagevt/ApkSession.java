package com.sagetech.sagevt;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ApkSession extends Thread {
    private final IOInterface ioInstance;
 
    private ServerSocket serverSocket = null;
    private Socket       clientSocket = null;

    private static ApkSession instance;
    public static ApkSession getInstance() {
        if (instance == null) {
            instance = new ApkSession();
            instance.start();
        }
        return instance;
    }

    private ApkSession() {
        this.ioInstance = AdbIO.getInstance();
    }

    private void closeServerSocket() {
        if (serverSocket == null) return;
        SGVTLog.msg("close server socket.");
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
        serverSocket = null;
    }

    private boolean openServerSocket() {
        closeServerSocket();
        try {
            // Security: accept only 1 connection, listen localhost only
            serverSocket = new ServerSocket(25112, 1, InetAddress.getLocalHost());
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            SGVTLog.exception(e);
            interrupt();
            return false;
        }
    }

    private Socket accept() {
        try {
            if (serverSocket != null) {
                try {
                    return serverSocket.accept();
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    SGVTLog.exception(e);
                    closeServerSocket();
                }
            } else {
                SGVTLog.msg("serversocket is null");
                if (!openServerSocket()) {
                    Thread.sleep(200);
                }
                return null;
            }
        } catch (InterruptedException e) {
        }
        return null;
    }

    private void closeSocket() {
        if (clientSocket == null) return;
        SGVTLog.msg("close client connection");
        try {
            clientSocket.close();
        } catch (Exception e) {
        }
        clientSocket = null;
    }

    public void interrupt() {
        SGVTLog.msg("interrupt requested");
        super.interrupt();
        closeSocket();
        closeServerSocket();
    }

    public void run() {
        Thread.currentThread().setPriority(10);
        openServerSocket();
        while (!isInterrupted()) {
            SGVTLog.msg("Start listening");
            clientSocket = accept();
            if (clientSocket != null) {
                SGVTLog.msg("Client connected");
                try {
                    ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream());
                    try {
                        ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
                        while (!isInterrupted()) {
                            try {
                                ioInstance.getClass().getMethod(input.readUTF(), (Class[]) input.readObject()).invoke(ioInstance, (Object[]) input.readObject());
                            } catch (SocketTimeoutException e) {
                            // continue reading
                            } catch (IOException e) {
                            // socket error, stop
                                break;
                            } catch (Exception e) {
                            // what can it be?
                                SGVTLog.exception(e);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        SGVTLog.exception(e);
                    } catch (Exception e) {
                        SGVTLog.exception(e);
                    }
                } catch (IOException e) {
                    SGVTLog.exception(e);
                } catch (Exception e) {
                    SGVTLog.exception(e);
                }
                closeSocket();
            }
        }
        closeServerSocket();
    }
}
