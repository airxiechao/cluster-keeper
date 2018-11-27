package com.airxiechao.clusterkeeper.util;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 安全相关辅助类
 */
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    /**
     * 节点token在请求头部的名称
     */
    public static final String NODE_TOKEN_HEADER = "Node-Token";

    /**
     * 节点token最大有效时间
     */
    private static final int NODE_TOKEN_MAX_SECOND = 60;

    private static final String AES_CIPHER_NAME = "AES";

    private static final String AES_KEY = "jaFXb8OXpMM79fbx";

    /**
     * 加密
     * @param data
     * @return
     */
    public static String encrypt(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(AES_CIPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes());

            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            logger.error("AES加密发生错误", e);
        }

        return null;
    }

    /**
     * 解密
     * @param data
     * @return
     */
    public static String decrypt(String data){
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance(AES_CIPHER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] original = cipher.doFinal(Base64.decodeBase64(data));

            return new String(original);
        } catch (Exception e) {
            logger.error("AES解密发生错误", e);
        }

        return null;
    }

    /**
     * 产生加密的节点token
     */
    public static String encryptNodeToken(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String token = formatter.format(new Date());
        return encrypt(token);
    }

    /**
     * 解密加密的节点token
     * @param token
     * @return
     */
    public static Date decryptNodeToken(String token){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        token = decrypt(token);
        Date date = null;
        try {
            date = formatter.parse(token);
        } catch (Exception e) {

        }

        return date;
    }

    /**
     * 验证加密的节点token是否有效
     * @param token
     * @return
     */
    public static boolean validateNodeToken(String token){
        Date date = decryptNodeToken(token);
        Date now = new Date();
        if(null != date){
            long diff = (now.getTime() - date.getTime()) / 1000;
            if(diff >= 0 && diff <= NODE_TOKEN_MAX_SECOND){
                return true;
            }
        }

        return false;
    }
}
