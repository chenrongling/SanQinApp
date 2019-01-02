package com.gotop.sanqinapp.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

public class SanQinClient{
    private String serverIp = "127.0.0.1";
    private int serverPort = 10000;
    
    private static SanQinClient instance;
    private Socket socketTcp;
    
    private static Object syncObj = new Object();
    private MsgPackage temp;
    private Thread receiveThread;
    private boolean isClosed = false;
    
    private SanQinClient() {
        init();
    }
    
    private void init() {
        System.out.println("init start");
        int count = 10;
        while (socketTcp == null || socketTcp.isClosed()) {
            try {
                socketTcp = new Socket(serverIp, serverPort);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (--count == 0) {
                throw new RuntimeException("连接失败");
            }
        }
        receiveThread = new Thread(new Runnable() {
            public void run() {
                synchronized (socketTcp) {
                    socketTcp.notify();
                }
                doReceiveData();
                if (!isClosed) {
                    init();
                }
            }
        });
        receiveThread.start();
        synchronized(socketTcp) {
            try {
                socketTcp.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static SanQinClient getInstance() {
        if (instance == null) {
            synchronized(SanQinClient.class) {
                if (instance == null) {
                    instance = new SanQinClient();
                }
            }
        }
        return instance;
    }
    
    private Set<CloseCharListener> closeCharListeners;

    public boolean login(Long userid, String username, String role) {
        System.out.println("login...");
        MsgPackage msgPackage = new MsgPackage();
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("userid", userid);
        msg.put("username", username);
        msg.put("role", role);

        String msgStr = JSON.toJSONString(msg);
        msgPackage.setLength(msgStr.length());
        msgPackage.setMsg(msgStr);
        msgPackage.setType(MsgPackage.TYPE_LOGIN);
        MsgPackage result = sentData(msgPackage);
        if (result != null) {
            String rmg = result.getMsg();
            Map rHashMap = JSON.parseObject(rmg, HashMap.class);
            if (rHashMap != null && "0".equals(rHashMap.get("errorcode"))) {
                return true;
            }
        }
        return false;
    }

    public boolean logout(Long userid, String username, String role) {
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("userid", userid);
        msg.put("username", username);
        msg.put("role", role);

        MsgPackage msgPackage = createMsgPackage(MsgPackage.TYPE_LOGOUT, msg);
        MsgPackage result = sentData(msgPackage);
        if (result != null) {
            String rmg = result.getMsg();
            Map rHashMap = JSON.parseObject(rmg, HashMap.class);
            if ("0".equals(rHashMap.get("errorcode"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 终端向柜台发起请求，如果成功，返回url和截止时间
     * 1. userid: 可选，存在该字段则向指定的柜台发起会话
     * @return
     */
    public Map<String, String> request(Long userId) {
        HashMap<String, Object> msg = new HashMap<String, Object>();
        if (userId != null) {
            msg.put("userid", userId);
        }
        MsgPackage msgPackage = createMsgPackage(MsgPackage.TYPE_REQUEST, msg);
        MsgPackage result = sentData(msgPackage);
        if (result != null) {
            Map rMap = JSON.parseObject(result.getMsg(), HashMap.class);
            if ("0".equals(rMap.get("errorcode"))) {
                return rMap;
            }
        }
        return null;
    }
    

    /**
     * 关闭回话
     * @return
     */
    public void setCloseCharListener(CloseCharListener listener) {
        closeCharListeners.add(listener);
    }
    
    public void doNotifyClose() {
        for (CloseCharListener listener : closeCharListeners) {
            if (listener != null) {
                listener.onCloseChar();
            }
        }
        Map<String, Object> msg = new HashMap<String, Object>();
        msg.put("errorcode", "0");
        sentData(createMsgPackage(MsgPackage.TYPE_CLOSE, msg));
    }

    public static interface CloseCharListener {
        void onCloseChar();
    }

    public MsgPackage sentData(MsgPackage data) {
        synchronized (syncObj) {
            try {
                OutputStream outputStream = socketTcp.getOutputStream();
                outputStream.write(ByteBuffer.allocate(4).putInt(data.getLength()).array());
                outputStream.write(ByteBuffer.allocate(4).putInt(data.getType()).array());
                if (data.getMsg() != null) {
                    outputStream.write(data.getMsg().getBytes());
                }
                outputStream.flush();
                syncObj.wait();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("return temp");
            return temp;
        }
    }
    

    public void closeConnect() {
        if (socketTcp != null && socketTcp.isConnected()){
            try {
                socketTcp.shutdownInput();
                socketTcp.shutdownOutput();
                socketTcp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        isClosed = true;
        socketTcp = null;
    }
    
    public void reConnect() {
        init();
    }
    
    private void doReceiveData() {
        while (socketTcp != null && socketTcp.isConnected()) {
            try {
                InputStream inputStream = socketTcp.getInputStream();
                DataInputStream dis = new DataInputStream(inputStream);
                int len = dis.readInt();
                if (len > 0) {
                    int type = dis.readInt();
                    if (MsgPackage.TYPE_CLOSE == type) {
                        doNotifyClose();
                        continue;
                    }
                    MsgPackage msg = new MsgPackage();
                    msg.setLength(len);
                    msg.setType(type);
                    byte[] b = new byte[len];
                    dis.read(b);
                    msg.setMsg(new String(b));
                    temp  = msg;
                    synchronized (syncObj) {
                        syncObj.notify();
                    }
                } else {
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.getStackTrace();
                break;
            }
        }
    }
    
    public static MsgPackage createMsgPackage(int type, Map<String, Object> msg) {
        MsgPackage msgpkg = new MsgPackage();
        msgpkg.setType(type);
        if(msg != null || !msg.isEmpty()) {
            String msgStr  = JSON.toJSONString(msg);
            msgpkg.setLength(msgStr.length());
            msgpkg.setMsg(msgStr);
        } else {
            msgpkg.setLength(0);
        }
        return msgpkg;
    }
}
