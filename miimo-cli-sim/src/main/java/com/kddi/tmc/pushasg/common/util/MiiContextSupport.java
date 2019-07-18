package com.kddi.tmc.pushasg.common.util;

import com.kddi.tmc.pushasg.common.vo.MiiContext;

public interface MiiContextSupport {

    void setMiiContext(MiiContext context);
    
    MiiContext getMiiContext();
    
    public static void setMiiContext(Object obj, MiiContext context) {
        if (obj instanceof MiiContextSupport) {
            ((MiiContextSupport) obj).setMiiContext(context);
        }
    }
    
}
