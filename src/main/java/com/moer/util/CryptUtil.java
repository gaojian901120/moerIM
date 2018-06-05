package com.moer.util;

import java.awt.*;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Created by gaoxuejian on 2018/5/24.
 */
public class CryptUtil
{
    public enum DiscuzAuthcodeMode {
        Encode, Decode
    }

    ;
    /**
     * @param str
     * @return
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
        }
        return sb.toString().trim();
    }

    /**
     * @param hexStr
     * @return
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }


    public static String md5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes("UTF-8"));
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String CutString(String str, int startIndex, int length) {
        if (startIndex >= 0) {
            if (length < 0) {
                length = length * -1;
                if (startIndex - length < 0) {
                    length = startIndex;
                    startIndex = 0;
                } else {
                    startIndex = startIndex - length;
                }
            }

            if (startIndex > str.length()) {
                return "";
            }

        } else {
            if (length < 0) {
                return "";
            } else {
                if (length + startIndex > 0) {
                    length = length + startIndex;
                    startIndex = 0;
                } else {
                    return "";
                }
            }
        }

        if (str.length() - startIndex < length) {

            length = str.length() - startIndex;
        }

        return str.substring(startIndex, startIndex + length);
    }

    public static String RandomString(int lens) {
        char[] CharArray = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k',
                'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
                'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        int clens = CharArray.length;
        String sCode = "";
        Random random = new Random();
        for (int i = 0; i < lens; i++) {
            sCode += CharArray[Math.abs(random.nextInt(clens))];
        }
        return sCode;
    }

    // / <summary>
    // / 用于 RC4 处理密码
    // / </summary>
    // / <param name="pass">密码字串</param>
    // / <param name="kLen">密钥长度，一般为 256</param>
    // / <returns></returns>
    static private byte[] GetKey(byte[] pass, int kLen) {
        byte[] mBox = new byte[kLen];

        for (int i = 0; i < kLen; i++) {
            mBox[i] = (byte) i;
        }

        int j = 0;
        for (int i = 0; i < kLen; i++) {

            j = (j + (int) ((mBox[i] + 256) % 256) + pass[i % pass.length])
                    % kLen;

            byte temp = mBox[i];
            mBox[i] = mBox[j];
            mBox[j] = temp;
        }

        return mBox;
    }

    public static int toInt(byte b) {
        return (int) ((b + 256) % 256);
    }

    // / <summary>
    // / RC4 原始算法
    // / </summary>
    // / <param name="input">原始字串数组</param>
    // / <param name="pass">密钥</param>
    // / <returns>处理后的字串数组</returns>
    private static byte[] RC4(byte[] input, String pass) {
        if (input == null || pass == null)
            return null;

        byte[] output = new byte[input.length];
        byte[] mBox = GetKey(pass.getBytes(), 256);

        // 加密
        int i = 0;
        int j = 0;

        for (int offset = 0; offset < input.length; offset++) {
            i = (i + 1) % mBox.length;
            j = (j + (int) ((mBox[i] + 256) % 256)) % mBox.length;

            byte temp = mBox[i];
            mBox[i] = mBox[j];
            mBox[j] = temp;
            byte a = input[offset];

            // byte b = mBox[(mBox[i] + mBox[j] % mBox.Length) % mBox.Length];
            // mBox[j] 一定比 mBox.Length 小，不需要在取模
            byte b = mBox[(toInt(mBox[i]) + toInt(mBox[j])) % mBox.length];

            output[offset] = (byte) ((int) a ^ (int) toInt(b));
        }

        return output;
    }

    public static String CutString(String str, int startIndex) {
        return CutString(str, startIndex, str.length());
    }

    /// <summary>
    /// 使用 变形的 rc4 编码方法对字符串进行加密或者解密
    /// </summary>
    /// <param name="source">原始字符串</param>
    /// <param name="key">密钥</param>
    /// <param name="operation">操作 加密还是解密</param>
    /// <param name="expiry">加密字串过期时间</param>
    /// <returns>加密或者解密后的字符串</returns>
    public static String authcode(String source, String key,
                                  DiscuzAuthcodeMode operation, int expiry) {
        try {
            if (source == null || key == null) {
                return "";
            }

            int ckey_length = 4;
            String keya, keyb, keyc, cryptkey, result;


            key = md5(key);

            keya = md5(CutString(key, 0, 16));

            keyb = md5(CutString(key, 16, 16));

            keyc = ckey_length > 0 ? (operation == DiscuzAuthcodeMode.Decode ? CutString(
                    source, 0, ckey_length)
                    : RandomString(ckey_length))
                    : "";


            cryptkey = keya + md5(keya + keyc);

            if (operation == DiscuzAuthcodeMode.Decode) {
                byte[] temp;

                temp = Base64Util.decode(CutString(source, ckey_length));
                result = new String(RC4(temp, cryptkey));
                if (CutString(result, 10, 16).equals(CutString(md5(CutString(result, 26) + keyb), 0, 16))) {
                    return CutString(result, 26);
                } else {
                    temp = Base64Util.decode(CutString(source + "=", ckey_length));
                    result = new String(RC4(temp, cryptkey));
                    if (CutString(result, 10, 16).equals(CutString(md5(CutString(result, 26) + keyb), 0, 16))) {
                        return CutString(result, 26);
                    } else {
                        temp = Base64Util.decode(CutString(source + "==", ckey_length));
                        result = new String(RC4(temp, cryptkey));
                        if (CutString(result, 10, 16).equals(CutString(md5(CutString(result, 26) + keyb), 0, 16))) {
                            return CutString(result, 26);
                        } else {
                            return "2";
                        }
                    }
                }
            } else {
                source = "0000000000" + CutString(md5(source + keyb), 0, 16)
                        + source;

                byte[] temp = RC4(source.getBytes("GBK"), cryptkey);

                return keyc + Base64Util.encodeBytes(temp);

            }
        } catch (Exception e) {
            return "";
        }

    }
}
