package com.hogense.util;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;

import atg.taglib.json.util.JSONObject;

public class MD5Util {
    public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public final static String MD5(String s,String charset) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes(charset);
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static class data
    {
    	int app_id=507;
    	String card_no="13579693299";
    	int fee=1000;
    	int goods_id=1393;
    	String order_no="T0test1234567";
    	String others="";
    	int pay_mode=1;
    	int pay_type=1;
    	String req_time="";
    	String version="1.3.0";
    	String wx_body="";
    	String sign;
    	public void sign()
    	{
    		sign=MD5(app_id+card_no+fee+goods_id+order_no+others+pay_mode+pay_type+req_time+version+wx_body+"9b7cc0328f1053e4f7de172f6029db6d");
    	}
    };
    public static void main(String[] args) {
    
    	System.out.println(MD5("suying1688").toLowerCase());
	}
}
