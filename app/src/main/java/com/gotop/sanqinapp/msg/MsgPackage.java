package com.gotop.sanqinapp.msg;

/**
 * 调度报文
 * @author liquanzhan
 *
 */
public class MsgPackage {
    public static final int TYPE_LOGIN = 0;
    public static final int TYPE_LOGOUT = 1;
    public static final int TYPE_REQUEST = 10;
    public static final int TYPE_FORWARD = 11;
    public static final int TYPE_GET_RUL = 20;
    public static final int TYPE_SENT_RUL = 21;
    public static final int TYPE_CLOSE = 30;
    public static final int TYPE_CALLBACK = 31;
    public static final int TYPE_LIST = 41;
    
    
    private int length;
    private int type;
    private String msg;
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    @Override
    public String toString() {
        return length + "" + type + msg;
    }
}
