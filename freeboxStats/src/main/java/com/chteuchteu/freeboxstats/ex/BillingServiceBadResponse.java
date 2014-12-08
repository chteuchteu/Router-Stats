package com.chteuchteu.freeboxstats.ex;

@SuppressWarnings("serial")
public class BillingServiceBadResponse extends Exception {
	public BillingServiceBadResponse() { }
	
	public BillingServiceBadResponse(String msg) {
		super(msg);
	}
	
	public BillingServiceBadResponse(Throwable cause) {
		super(cause);
	}
	
	public BillingServiceBadResponse(String msg, Throwable cause) {
		super(msg, cause);
	}
}