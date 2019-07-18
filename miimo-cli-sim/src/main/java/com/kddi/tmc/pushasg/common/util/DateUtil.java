package com.kddi.tmc.pushasg.common.util;

import java.math.BigDecimal;

public final class DateUtil {
    
    public static Float getNow() {
        return System.currentTimeMillis() / 1000f;
    }

    public static BigDecimal getUnixNowBigDecimal() {
    	BigDecimal millBigDecimal= new BigDecimal(Double.toString(System.currentTimeMillis()/1000d));
    	millBigDecimal.setScale(3, BigDecimal.ROUND_HALF_UP);
    	return millBigDecimal;
    }

}
