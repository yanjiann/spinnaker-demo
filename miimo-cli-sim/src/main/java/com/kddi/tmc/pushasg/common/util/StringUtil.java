package com.kddi.tmc.pushasg.common.util;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StringUtil {
    private static final Logger logger = LoggerFactory
            .getLogger(StringUtil.class);

    public static final String ENC = "UTF-8";
    
    public static String toString(byte[] contents) {
        try {
            return new String(contents, ENC);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
    
    /**
     * byte配列をStringに変換し、空白や改行を除去したものを返す
     * 
     * @param byte_array
     *            バイト配列
     * @return 変換後の文字列
     */
    public static String encodeToString(byte[] byte_array) {
        try {
            String rawString = new String(byte_array, "UTF-8");
            String compactString = rawString.replaceAll("\r\n", "").replaceAll("\n", "");
            return compactString;
        } catch (UnsupportedEncodingException e) {
            logger.debug("Failed to encode byte array into character string.");
            logger.debug(e.getMessage(), e);
            return null;
        }
    }
    
}
