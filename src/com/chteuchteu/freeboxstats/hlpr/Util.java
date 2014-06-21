package com.chteuchteu.freeboxstats.hlpr;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Util {
	public static String getPref(Context c, String key) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getString(key, "");
	}
	
	public static void setPref(Context c, String key, String value) {
		if (value.equals(""))
			removePref(c, key);
		else {
			SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(key, value);
			editor.commit();
		}
	}
	
	public static void removePref(Context c, String key) {
		SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.commit();
	}
	
	public static boolean isOnline(Context c) {
		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
			return true;
		return false;
	}
	
	public static final class Streams {
		public static String convertStreamtoString(InputStream is) {
			Scanner s = new Scanner(is);
			s.useDelimiter("\\A");
			String ret = s.hasNext() ? s.next() : "";
			s.close();
			return ret;
		}
	}
	
	public static final class Crypto {
		public static String hmacSha1(String value, String key)
				throws UnsupportedEncodingException, NoSuchAlgorithmException,
				InvalidKeyException {
			String type = "HmacSHA1";
			SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
			Mac mac = Mac.getInstance(type);
			mac.init(secret);
			byte[] bytes = mac.doFinal(value.getBytes());
			return bytesToHex(bytes);
		}
		
		private final static char[] hexArray = "0123456789abcdef".toCharArray();
		
		private static String bytesToHex(byte[] bytes) {
			char[] hexChars = new char[bytes.length * 2];
			int v;
			for (int j = 0; j < bytes.length; j++) {
				v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}
	}
	
	/**
	 * Returns the password asked by the API from the app_token and challenge
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	public static String encodeAppToken(String app_token, String challenge) throws 
	InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, SignatureException {
		String res = Crypto.hmacSha1(challenge, app_token);
		//String res = calculateRFC2104HMAC(challenge, app_token);
		Log.v("Generated res", res);
		return res;
	}
}