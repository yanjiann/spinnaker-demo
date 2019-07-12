package com.hpe.iot.core.nip.adapter.mqtt.exception;

/**
 * @author chenjial
 *
 */
public class ApplicationException extends Exception {
	
	private String errorCode;
	private String errorMsg;
	
	public ApplicationException(String errorCode, String errorMsg){
		super(errorMsg);
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
	}
	
   
    public String getErrorCode() {
		return errorCode;
	}


	public String getErrorMsg() {
		return errorMsg;
	}


	public ApplicationException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public ApplicationException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public ApplicationException(String message) {
        super(message);
        this.errorCode = message;
        // TODO Auto-generated constructor stub
    }

    public ApplicationException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
