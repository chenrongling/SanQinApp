package com.gotop.sanqinapp.msg;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

public class SanQinClient{
//    private String serverIp = "39.105.135.60";
    private String serverIp = "39.105.135.60";
    private int serverPort = 8090;

    private static SanQinClient instance;
    private Socket socketTcp;

    private static Object syncObj = new Object();
    private volatile MsgPackage temp;
    private Thread receiveThread;
    private Thread liveThread;
    private volatile boolean isClosed = false;
    
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
                doReceiveData2();
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
        
        liveThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                        if (isServerClose(socketTcp)) { // 如果服务器断开
                            isClosed = true;
                        }
                        if (isClosed) {
                            synchronized (syncObj) {
                                syncObj.notifyAll();
                            }
                            break;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } // 每10秒发送心跳包
            }
        }});
        liveThread.start();
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
        MsgPackage result = sentData2(msgPackage);
        System.out.println("responsse :" + result);
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
        MsgPackage result = sentData2(msgPackage);
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
        MsgPackage result = sentData2(msgPackage);
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
    
    /**
     * {@link #sentData2(MsgPackage)}
     * @param data
     * @return
     */
    @Deprecated
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
    
    public MsgPackage sentData2(MsgPackage data) {
        synchronized (syncObj) {
            try {
                OutputStream outputStream = socketTcp.getOutputStream();
                outputStream.write(String.format("%04d",data.getLength()).getBytes());
                outputStream.write(String.format("%04d",data.getType()).getBytes());
                if (data.getMsg() != null) {
                    outputStream.write(data.getMsg().getBytes());
                }
                outputStream.flush();
                temp = null;
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
    
    /**
     * Use either the {@link #doReceiveData2()}  
     */
    @Deprecated
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
    
    private void doReceiveData2() {
        while (socketTcp != null && socketTcp.isConnected() && !socketTcp.isClosed()) {
            try {
                InputStream inputStream = socketTcp.getInputStream();
//                BufferedInputStream bin = new BufferedInputStream(inputStream);
                InputStreamReader reader = new InputStreamReader(inputStream);
                char[] lenChars = new char[4];
                if (reader.read(lenChars) == -1) {
                    continue;
                }
                int len = Integer.parseInt(new String(lenChars));
                if (len > 0) {
                    char[] typeChars = new char[4];
                    if (reader.read(typeChars) == -1) {
                        continue;
                    }
                    int type = Integer.parseInt(new String(typeChars));
                    if (MsgPackage.TYPE_CLOSE == type) {
                        doNotifyClose();
                        continue;
                    }
                    MsgPackage msg = new MsgPackage();
                    msg.setLength(len);
                    msg.setType(type);
                    char[] b = new char[len];
                    reader.read(b);
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
        System.out.println("socketTcp.isClosed: " + socketTcp.isClosed());
        synchronized (syncObj) {
            syncObj.notify();
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

    public Boolean isServerClose(Socket socket){ 
        try{ 
         socket.sendUrgentData(0xFF);//发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信 
         return false; 
        }catch(Exception se){ 
         return true; 
        }
     }
}
