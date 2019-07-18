package com.kddi.tmc.pushasg.common.util;

import com.kddi.tmc.pushasg.common.vo.SimContext;

public interface SimContextSupport {

    void setSimContext(SimContext context);
    
    SimContext getSimContext();
    
    public static void setSimContext(Object obj, SimContext context) {
        if (obj instanceof SimContextSupport) {
            ((SimContextSupport) obj).setSimContext(context);
        }
    }
    
}
