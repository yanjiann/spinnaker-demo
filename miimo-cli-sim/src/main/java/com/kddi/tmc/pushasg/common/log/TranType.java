package com.kddi.tmc.pushasg.common.log;

public enum TranType {
    SEND("SEND"), RECV("RECV");
    
    private final String fieldDescription;

    private TranType(String value) {
        fieldDescription = value;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }
}
