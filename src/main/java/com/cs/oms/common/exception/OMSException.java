package com.cs.oms.common.exception;

public class OMSException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6132704835325197077L;

	public OMSException(String message) {
		super(message);
	}

	public OMSException(Throwable cause) {
		super(cause);
	}

}
