package com.kddi.tmc.pushasg.common.log;

import org.slf4j.LoggerFactory;

import com.kddi.tmc.pushasg.common.util.MessageUtil;

/**
 * ログを出力するクラスです。
 */
public final class Logger {

    /** ログレベル DEBUG */
    public static final String LOG_LEVEL_DEBUG = "D";
    /** ログレベル INFO */
    public static final String LOG_LEVEL_INFO = "I";
    /** ログレベル ERROR */
    public static final String LOG_LEVEL_ERROR = "E";
    /** ログレベル WARN */
    public static final String LOG_LEVEL_WARN = "W";
    /** ログレベル CRITICAL */
    public static final String LOG_LEVEL_CRITICAL = "C";
    /** ログレベル TRACE */
    public static final String LOG_LEVEL_TRACE = "T";

    /** ロガー名 アプリケーションログ */
    private static final String APPLICATION_LOG = "ApplicationLog";

    /** ロガー名 システムログ */
    private static final String TRAN_LOG = "TranLog";

    /** デリミタ */
    private static final String BYTE_SPACE = " ";
    
    private static final String BYTE_TAB = "\t";

    private static org.slf4j.Logger appLog = LoggerFactory
            .getLogger(APPLICATION_LOG);
    
    private static org.slf4j.Logger tranLog = LoggerFactory
            .getLogger(TRAN_LOG);
    
    private Class<?> clazz;
    
    private static ThreadLocal<String> sessionIdLocal = new ThreadLocal<String>();
    
    public Logger(Class<?> clazz) {
        this.clazz = clazz;
    }

    public void writeAppLog(String msgId,
            Object... args) {
        LogLevel loglevel = getLevel(msgId);

        StringBuilder sb = new StringBuilder();
        sb.append(getSessionId());
        sb.append(BYTE_TAB);
        sb.append(getLogMsgId(msgId));       
        sb.append(BYTE_TAB);
        sb.append(getClassName(clazz));
        sb.append(BYTE_TAB);
        sb.append(MessageUtil.getMessage(msgId, args));
        switch (loglevel) {
        case DEBUG:
            appLog.debug(sb.toString());            
            break;
        case INFO:
            appLog.info(sb.toString());
            break;
        case ERROR:
            appLog.error(sb.toString());
            break;
        case WARN:
            appLog.warn(sb.toString());
            break;
        case CRITICAL:
            appLog.error(sb.toString());
            break;
        case TRACE:
            appLog.trace(sb.toString());
            break;
        }
    }
    
//    public void writeAppLog(String msgId,
//            Object... args) {
//        writeAppLog("-", msgId, args);
//    }
    
    public void writeTranLog(TranType type, String cmdType ,String sessionId, String payload) {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());
        sb.append(BYTE_TAB);
        sb.append(sessionId);      
        sb.append(BYTE_TAB);
        sb.append(cmdType);
        sb.append(BYTE_TAB);
        sb.append(payload);

        tranLog.trace(sb.toString());      
    }

    private static LogLevel getLevel(String msgId) {

        LogLevel result = null;
        if (msgId.endsWith(LOG_LEVEL_DEBUG)) {
            result = LogLevel.DEBUG;
        } else if (msgId.endsWith(LOG_LEVEL_INFO)) {
            result = LogLevel.INFO;
        } else if (msgId.endsWith(LOG_LEVEL_ERROR)) {
            result = LogLevel.ERROR;
        } else if (msgId.endsWith(LOG_LEVEL_WARN)) {
            result = LogLevel.WARN;
        } else if (msgId.endsWith(LOG_LEVEL_CRITICAL)) {
            result = LogLevel.CRITICAL;
        } else if (msgId.endsWith(LOG_LEVEL_TRACE)) {
            result = LogLevel.TRACE;
        } else {
            result = LogLevel.DEBUG;
        }

        return result;
    }
    
    private static String getLogMsgId(String msgId) {
        return msgId.substring(0, msgId.length() - 1);
    }

    private static String getClassName(Class<?> clazz) {
        return clazz.getSimpleName();
    }
    
    public static void setSessionId(String sessionId) {
        sessionIdLocal.set(sessionId);
    }
    
    public static String getSessionId() {
        String res = sessionIdLocal.get();
        return res == null ? "-" : res;
    }

}
