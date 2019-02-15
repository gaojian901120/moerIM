package com.moer.util;

import java.security.MessageDigest;
import java.util.Random;

import static jdk.nashorn.internal.objects.NativeString.substr;

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
    public static String authcode(String string, String key,
                                  DiscuzAuthcodeMode operation, int expiry) throws Exception{
//        try {
//            if (source == null || key == null) {
//                return "";
//            }
//
//            int ckey_length = 4;
//            String keya, keyb, keyc, cryptkey, result;
//
//
//            key = md5(key);
//
//            keya = md5(CutString(key, 0, 16));
//
//            keyb = md5(CutString(key, 16, 16));
//
//            keyc = ckey_length > 0 ? (operation == DiscuzAuthcodeMode.Decode ? CutString(
//                    source, 0, ckey_length)
//                    : RandomString(ckey_length))
//                    : "";
//
//
//            cryptkey = keya + md5(keya + keyc);
//
//            if (operation == DiscuzAuthcodeMode.Decode) {
//                byte[] temp;
//
//                temp = Base64Util.decode(CutString(source, ckey_length));
//                result = new String(RC4(temp, cryptkey));
//                if (CutString(result, 10, 16).equals(CutString(md5(CutString(result, 26) + keyb), 0, 16))) {
//                    return CutString(result, 26);
//                } else {
//                    temp = Base64Util.decode(CutString(source + "=", ckey_length));
//                    result = new String(RC4(temp, cryptkey));
//                    if (CutString(result, 10, 16).equals(CutString(md5(CutString(result, 26) + keyb), 0, 16))) {
//                        return CutString(result, 26);
//                    } else {
//                        temp = Base64Util.decode(CutString(source + "==", ckey_length));
//                        result = new String(RC4(temp, cryptkey));
//                        if (CutString(result, 10, 16).equals(CutString(md5(CutString(result, 26) + keyb), 0, 16))) {
//                            return CutString(result, 26);
//                        } else {
//                            return "2";
//                        }
//                    }
//                }
//            } else {
//                source = "0000000000" + CutString(md5(source + keyb), 0, 16)
//                        + source;
//
//                byte[] temp = RC4(source.getBytes("GBK"), cryptkey);
//
//                return keyc + Base64Util.encodeBytes(temp);
//
//            }
//        } catch (Exception e) {
//            return "";
//        }
// 动态密匙长度，相同的明文会生成不同密文就是依靠动态密匙
        // 加入随机密钥，可以令密文无任何规律，即便是原文和密钥完全相同，加密结果也会每次不同，增大破解难度。
        // 取值越大，密文变动规律越大，密文变化 = 16 的 $ckey_length 次方
        // 当此值为 0 时，则不产生随机密钥
        int ckey_length = 0;
        // 密匙
        // $GLOBALS['discuz_auth_key'] 这里可以根据自己的需要修改
        key = md5(key);
        // 密匙a会参与加解密
        String keya = md5(substr(key, 0, 16));
        // 密匙b会用来做数据完整性验证
        String keyb = md5(substr(key, 16, 16));
        // 密匙c用于变化生成的密文
        String keyc = "";
        // 参与运算的密匙
        String cryptkey = keya + md5(keya + keyc);
        int key_length = cryptkey.length();
        // 明文，前10位用来保存时间戳，解密时验证数据有效性，10到26位用来保存$keyb(密匙b)，解密时会通过这个密匙验证数据完整性
        // 如果是解码的话，会从第$ckey_length位开始，因为密文前$ckey_length位保存 动态密匙，以保证解密正确
        String s = string.substring(ckey_length);
        int string_length = 0;
        byte [] stringbyte = null;
        if(operation == DiscuzAuthcodeMode.Decode){
            stringbyte =  Base64Util.decode(s);
            string_length = stringbyte.length;
        }else {
            string = String.format("%010d", expiry!=0   ? expiry + System.currentTimeMillis()/1000 : 0) + md5(string + keyb).substring( 0, 16) + string;
            string_length = string.length();
        }

        String result = "";
        int [] box = new int[256];
        for (int i=0;i<256;i++){
            box[i] = i;
        }
        int [] rndkey = new int[256];
        // 产生密匙簿
        for(int i = 0; i <= 255; i++) {
            rndkey[i] = cryptkey.charAt(i % key_length);
        }
        // 用固定的算法，打乱密匙簿，增加随机性，好像很复杂，实际上并不会增加密文的强度
        for(int j=0,i = 0; i < 256; i++) {
            j = (j + box[i] + rndkey[i]) % 256;
            int tmp = box[i];
            box[i] = box[j];
            box[j] = tmp;
        }
        // 核心加解密部分
        for(int a=0,j=0,i=0; i < string_length; i++) {
            a = (a + 1) % 256;
            j = (j + box[a]) % 256;
            int tmp = box[a];
            box[a] = box[j];
            box[j] = tmp;
            // 从密匙簿得出密匙进行异或，再转成字符
            int tc = stringbyte[i] ^ (box[(box[a] + box[j]) % 256]);
            tc = (tc+256) % 256;
            result += (char)tc;
        }
        if(operation == DiscuzAuthcodeMode.Decode) {
            // substr($result, 0, 10) == 0 验证数据有效性
            // substr($result, 0, 10) - time() > 0 验证数据有效性
            // substr($result, 10, 16) == substr(md5(substr($result, 26).$keyb), 0, 16) 验证数据完整性
            // 验证数据有效性，请看未加密明文的格式
            int time = Integer.valueOf(Long.valueOf(System.currentTimeMillis()/1000).toString());
            System.out.println(" result.substring(10,16):" +  result.substring(10,26));
            System.out.println("md5(result.substring(26) + keyb).substring(0, 16)):" + md5(result.substring(26) + keyb).substring(0, 16));
            if((result.substring(0,10).equals("0000000000")|| (Long.valueOf(result.substring(0,10)) - time)> 0L )&& result.substring(10,26).equals(md5(result.substring(26) + keyb).substring(0, 16))) {
                return result.substring(26);
            } else {
                return "";
            }
        } else {
            // 把动态密匙保存在密文里，这也是为什么同样的明文，生产不同密文后能解密的原因
            // 因为加密后的密文可能是一些特殊字符，复制过程可能会丢失，所以用base64编码
            return keyc + Base64Util.encodeBytes(result.getBytes()).replace("=","");
        }
    }
}
