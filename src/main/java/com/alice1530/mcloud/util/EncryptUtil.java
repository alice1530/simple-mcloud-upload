package com.alice1530.mcloud.util;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EncryptUtil {
    public static String getRandomSring(int bit) {
        String n = "";
        String t = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int a = 0; a < bit; a++) {
            int o = (int)Math.floor(Math.random() * t.length());
            n += t.substring(o, o + 1);
        }
        return n;
    }

    public static String getNewSign(String body,String ts,String ramdomstr) {
        String temstr = "";
        if (StringUtils.hasLength(body)) {
            Pattern pattern = Pattern.compile("\\s*"); //去掉空格符合换行符
            Matcher matcher = pattern.matcher(body);
            String result = matcher.replaceAll("");
            result = UrlEncodeUtil.encodeURIComponent(result);
            String[] strs = result.split("");
            Arrays.sort(strs);
            temstr = String.join("",strs);
        }
        temstr = Base64.getEncoder().encodeToString(temstr.getBytes());
        String res = DigestUtils.md5DigestAsHex((DigestUtils.md5DigestAsHex(temstr.getBytes())+DigestUtils.md5DigestAsHex((ts + ":" + ramdomstr).getBytes())).getBytes());
        return res.toUpperCase();
    }

    public static void main(String[] args) {
    }
}
