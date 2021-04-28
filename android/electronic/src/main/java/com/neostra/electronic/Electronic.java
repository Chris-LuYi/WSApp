package com.neostra.electronic;

import android.support.annotation.NonNull;
import com.neostra.serialport.SerialPort;
import com.neostra.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;

/**
 * @author njmsir
 * Created by njmsir on 2019/5/23.
 */
public final class Electronic {
    private final ElectronicCallback mCallback;
    private final StringBuilder sb = new StringBuilder();
    private final DecimalFormat df = new DecimalFormat("#######0.000");
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private SerialPort serialPort;

    private Electronic(String devicePath, int baudrate, ElectronicCallback mCallback) {
        this.mCallback = mCallback;
        try {
            serialPort = new SerialPort(new File(devicePath), baudrate, 0);
        } catch (SecurityException | IOException | InvalidParameterException e) {
            e.printStackTrace();
        }

        if (serialPort != null) {
            mInputStream = serialPort.getInputStream();
            mOutputStream = serialPort.getOutputStream();
            ReadThread mReadThread = new ReadThread();
            mReadThread.start();
        }
    }


    /**
     * 去皮
     */
    public void removePeel() {
        sendCommand(0x3C, 0x54, 0x4B, 0x3E, 0x09);
    }

    /**
     * 置零
     */
    public void turnZero() {
        sendCommand(0x3C, 0x5A, 0x4B, 0x3E, 0x09);
    }

    /**
     * 手动去皮
     */
    public void manualPeel(int peelWeight) {
        int h = Utils.getHexHightBit(peelWeight);
        int l = Utils.getHexLowBit(peelWeight);
        sendCommand(0xAB, 0, 0, 0, 0, 0x80, 0x05, h, l, 0, 0, 0, getCheckBit(h, l));
    }

    private int getCheckBit(int peelH, int peelL) {
        return (0xAB + 0x80 + 0x05 + peelH + peelL) & 0xff;
    }

    /**
     * 发送命令
     *
     * @param mCommand
     */
    private void sendCommand(@NonNull int... mCommand) {
        try {
            for (int command : mCommand) {
                mOutputStream.write(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭电子秤
     */
    public void closeElectronic() {
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        try {
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 称重串口线程
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    Thread.sleep(10L);
                    byte[] buffer = new byte[64];
                    if (null == mInputStream) {
                        return;
                    }
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void onDataReceived(byte[] buffer, int size) {
        for (int i = 0; i < size; i++) {
            //转16进制字符串,不足两位补零，然后拼接
            sb.append(String.format("%02x", buffer[i]));
            //整条称重数据包总长度为32
            if (sb.length() == 32) {
                //0102开头0304结尾，但整条数据的长度为0304结尾后再加一个字节
                String content = sb.toString();
                if (content.startsWith("0102") && content.substring(26, 30).equals("0304")) {
                    String weight = "";
                    try {
                        weight = df.format(Double.parseDouble(Utils.toStringHex1(content.substring(6, 20))));
                        //解析重量，trycatch防止重量不是double引起崩溃
                    } catch (Exception e) {

                    }
                    String mWeightState = content.substring(4, 6);
                    mCallback.electronicStatus(weight, mWeightState);
                    sb.delete(0, sb.length());
                } else if (content.startsWith("ab00")) {
                    String resultCode = content.substring(10, 14);
                    if ("800e".equals(resultCode)) {
                        //手动去皮成功
                        mCallback.electronicStatus("0", "56");
                    } else if ("800d".equals(resultCode)) {
                        //手动去皮失败
                        mCallback.electronicStatus("0", "57");
                    }
                }
            } else if (sb.length() > 32) {
                if (sb.toString().endsWith("0102")) {
                    sb.delete(0, sb.length() - 4);
                }
            }
        }
        StringBuilder ss = new StringBuilder();
        for (int i = 0; i < buffer.length; i++) {
            ss.append(String.format("%02x", buffer[i]));
//            12833248484649504810710398340000000000000000000000000000000000000000000000000
//            0102532030302e3132306b6762030400000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
//            ab00000000800e000000000039000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
        }
    }


    public static final class Builder {
        private String devicePath = "/dev/ttyS4";
        private int baudrate = 9600;
        private ElectronicCallback mCallback;


        public Electronic.Builder setDevicePath(String devicePath) {
            this.devicePath = devicePath;
            return this;
        }

        public Electronic.Builder setBaudrate(int baudrate) {
            this.baudrate = baudrate;
            return this;
        }

        public Electronic.Builder setReceiveCallback(ElectronicCallback mCallback) {
            this.mCallback = mCallback;
            return this;
        }

        public Electronic builder() {
            return new Electronic(devicePath, baudrate, mCallback);
        }
    }
}
