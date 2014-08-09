package com.chteuchteu.freeboxstats.ex;

import com.applovin.sdk.AppLovinErrorCodes;

@SuppressWarnings("serial")
public class AdsLoadingFail extends Exception {
	public AdsLoadingFail() { }
	
	public AdsLoadingFail(int errorCode) {
		super(getMessage(errorCode));
	}
	
	private static String getMessage(int applovinErrorCode) {
		switch (applovinErrorCode) {
			case AppLovinErrorCodes.NO_FILL:
				return "failedToReceiveAd - NO_FILL";
			case AppLovinErrorCodes.UNABLE_TO_RENDER_AD:
				return "failedToReceiveAd - UNABLE_TO_RENDER_AD";
			case AppLovinErrorCodes.UNSPECIFIED_ERROR:
				return "failedToReceiveAd - UNSPECIFIED_ERROR";
			case AppLovinErrorCodes.NO_INCENTIVIZED_AD_PRELOADED:
				return "failedToReceiveAd - NO_INCENTIVIZED_AD_PRELOADED";
			case AppLovinErrorCodes.UNKNOWN_SERVER_ERROR:
				return "failedToReceiveAd - UNKNOWN_SERVER_ERROR";
			case AppLovinErrorCodes.SERVER_TIMEOUT:
				return "failedToReceiveAd - SERVER_TIMEOUT";
			case AppLovinErrorCodes.USER_CLOSED_VIDEO:
				return "failedToReceiveAd - USER_CLOSED_VIDEO";
		}
		return "failedToReceiveAd - unknown";
	}
	
	public AdsLoadingFail(String msg) {
		super(msg);
	}
	
	public AdsLoadingFail(Throwable cause) {
		super(cause);
	}
	
	public AdsLoadingFail(String msg, Throwable cause) {
		super(msg, cause);
	}
}