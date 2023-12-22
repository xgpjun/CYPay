package cn.xgpjun.cypay.qrcode;

import cn.xgpjun.cypay.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SignUtil {

    public static String generateSignature(Map<String, String> parameters) {
        List<String> keys = new ArrayList<>(parameters.keySet());
        Collections.sort(keys);

        StringBuilder signStr = new StringBuilder();
        for (String k : keys) {
            String v = parameters.get(k);
            if (!"sign".equals(k) && !"sign_type".equals(k) && v != null && !v.isEmpty()) {
                signStr.append(k).append('=').append(v).append('&');
            }
        }

        signStr.deleteCharAt(signStr.length() - 1);
//        signStr.append(Config.getKey());
        signStr.append("9uLuECaVZuHdK2RWztVr8j0gxUvDVGFS");

        return md5(signStr.toString()).toLowerCase();
    }

    private static String md5(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
