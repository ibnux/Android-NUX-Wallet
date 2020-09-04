package com.ibnux.nuxwallet.utils;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ibnux.nuxwallet.Aplikasi;
import com.ibnux.nuxwallet.kripto.Curve25519;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

public class Utils {
    private static final char[] hexChars = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };


    public static String signBytes(byte[] messageBytes, String secret){
        Log.d("sign",messageBytes.toString());
        byte[] secretbytes = toBytes(secret);
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(secret.getBytes());
            digest = md.digest();
        }catch (Exception e){
            return null;
        }

        byte[] s = Curve25519.keygen(digest);
        byte[] m = parseHexString(SHA256(toHexString(messageBytes)));
        byte[] x = parseHexString(SHA256(toHexString(array_merge(m, s))));
        Map<String, Object> key = Curve25519.keygens(x);
        x = (byte[])key.get("k");
        byte[] y = (byte[])key.get("P");
        byte[] h = parseHexString(SHA256(toHexString(array_merge(m, y))));
        byte[] v = Curve25519.sign(h,x,s);
        return toHexString(array_merge(v, h));
    }


    public static <T> T array_merge(T a, T b) {
        if (!a.getClass().isArray() || !b.getClass().isArray()) {
            throw new IllegalArgumentException();
        }

        Class<?> resCompType;
        Class<?> aCompType = a.getClass().getComponentType();
        Class<?> bCompType = b.getClass().getComponentType();

        if (aCompType.isAssignableFrom(bCompType)) {
            resCompType = aCompType;
        } else if (bCompType.isAssignableFrom(aCompType)) {
            resCompType = bCompType;
        } else {
            throw new IllegalArgumentException();
        }

        int aLen = Array.getLength(a);
        int bLen = Array.getLength(b);

        @SuppressWarnings("unchecked")
        T result = (T) Array.newInstance(resCompType, aLen + bLen);
        System.arraycopy(a, 0, result, 0, aLen);
        System.arraycopy(b, 0, result, aLen, bLen);

        return result;
    }

    public static byte[] toBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static byte[] toBytes(String s, boolean isText) {
        return isText ? toBytes(s) : parseHexString(s);
    }

    public static String toString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static String toString(byte[] bytes, boolean isText) {
        return isText ? toString(bytes) : toHexString(bytes);
    }

    public static byte[] toBytes(long n) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte)(n >> (8 * i));
        }
        return bytes;
    }

    public static byte[] parseHexString(String hex) {
        if (hex == null) {
            return null;
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int char1 = hex.charAt(i * 2);
            char1 = char1 > 0x60 ? char1 - 0x57 : char1 - 0x30;
            int char2 = hex.charAt(i * 2 + 1);
            char2 = char2 > 0x60 ? char2 - 0x57 : char2 - 0x30;
            if (char1 < 0 || char2 < 0 || char1 > 15 || char2 > 15) {
                throw new NumberFormatException("Invalid hex number: " + hex);
            }
            bytes[i] = (byte)((char1 << 4) + char2);
        }
        return bytes;
    }


    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            chars[i * 2] = hexChars[((bytes[i] >> 4) & 0xF)];
            chars[i * 2 + 1] = hexChars[(bytes[i] & 0xF)];
        }
        return String.valueOf(chars);
    }
    public static String SHA256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(text.getBytes());
            byte[] digest = md.digest();

            return encodeHexString(digest);
        }catch (Exception e){
            return null;
        }
    }

    public static byte[] hex2bin(String hex) throws NumberFormatException {
        if (hex.length() % 2 > 0) {
            throw new NumberFormatException("Hexadecimal input string must have an even length.");
        }
        byte[] r = new byte[hex.length() / 2];
        for (int i = hex.length(); i > 0;) {
            r[i / 2 - 1] = (byte) (digit(hex.charAt(--i)) | (digit(hex.charAt(--i)) << 4));
        }
        return r;
    }

    private static int digit(char ch) {
        //TODO Optimize this
        int r = Character.digit(ch, 16);
        if (r < 0) {
            throw new NumberFormatException("Invalid hexadecimal string: " + ch);
        }
        return r;
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();

    private static String encodeHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bin2hex(byte[] in) {
        StringBuilder sb = new StringBuilder(in.length * 2);
        for (byte b : in) {
            sb.append(
                    forDigit((b & 0xF0) >> 4)
            ).append(
                    forDigit(b & 0xF)
            );
        }
        return sb.toString();
    }

    public static char forDigit(int digit) {
        if (digit < 10) {
            return (char) ('0' + digit);
        }
        return (char) ('A' - 10 + digit);
    }

    public static void log(Object txt){
        Log.d("NUX","-----------------------");
        Log.d("NUX",txt+"");
        Log.d("NUX","-----------------------");
    }

    public static String nuxFormat(long myNumber){
        DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
        NumberFormat formatter = new DecimalFormat("#,###");
        String strNumber = String.valueOf(myNumber);
        String result = "";
        if(strNumber.length()>8) {
            String desimal = strNumber.substring(strNumber.length() - 8);
            strNumber = strNumber.substring(0, strNumber.length() - 8);
            result = formatter.format(Double.parseDouble(strNumber));
            if(!desimal.equals("00000000"))
                result += dfs.getDecimalSeparator() +desimal;
        }else{
            result = formatter.format(myNumber);
        }
        return result;
    }

    // d m y h i s full
    public static String toDate(long milliSeconds, String format)
    {
        log("toDate "+milliSeconds+" "+format);
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTimeInMillis(milliSeconds);
        cal.setTimeZone(TimeZone.getDefault());
        String hasil = "";
        switch (format){
            case "d" : hasil = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)); break;
            case "m" : hasil = String.valueOf(cal.get(Calendar.MONTH)+1); break;
            case "y" : hasil = String.valueOf(cal.get(Calendar.YEAR)).substring(2,4); break;
            case "Y" : hasil = String.valueOf(cal.get(Calendar.YEAR)); break;
            case "h" : hasil = String.valueOf(cal.get(Calendar.HOUR)); break;
            case "H" : hasil = String.valueOf(cal.get(Calendar.HOUR_OF_DAY)); break;
            case "i" : hasil = String.valueOf(cal.get(Calendar.MINUTE)); break;
            default: hasil = cal.get(Calendar.DAY_OF_MONTH)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.YEAR)+" "
                    +cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE);
        }
        if(hasil.length()==1)
            return "0"+hasil;
        else
            return hasil;
    }

    public static void showToast(String pesan, Context cx){
        Toast t =Toast.makeText(cx,pesan,Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER,0,0);
        t.show();
    }

    public static void vibrate(){
        Vibrator v = (Vibrator) Aplikasi.app.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(500);
        }
    }

    public static AlertDialog.Builder progressDialog(String title, Context cx ){
        AlertDialog.Builder builder = new AlertDialog.Builder(cx);
        builder.setTitle(title);
        final ProgressBar progressBar = new ProgressBar(cx);
        progressBar.setIndeterminate(true);
        builder.setView(progressBar);
        builder.setNegativeButton("Cancel",null);
        builder.setCancelable(false);
        return builder;
    }

}
