package com.chteuchteu.freeboxstats.hlpr;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;

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
		Log.v("Generated res", res);
		return res;
	}
	
	public static final class Times {
		/**
		 * Gets "from" timestamp when retrieving data from Freebox
		 * @param period
		 * @return
		 */
		public static long getFrom(Period period) {
			Calendar cal = Calendar.getInstance();
			switch (period) {
				case HOUR:
					cal.add(Calendar.HOUR, -1);
					break;
				case DAY:
					cal.add(Calendar.HOUR, -24);
					break;
				case WEEK:
					cal.add(Calendar.DAY_OF_MONTH, -7);
					break;
				case MONTH:
					cal.add(Calendar.DAY_OF_MONTH, -30);
					break;
				default:
					break;
			}
			return cal.getTime().getTime() / 1000;
		}
		
		/**
		 * Returns "to" timestamp when retrieving data from Freebox
		 * @return
		 */
		public static long getTo() {
			Calendar cal = Calendar.getInstance();
			return new Timestamp(cal.getTime().getTime()).getTime() / 1000;
		}
		
		/**
		 * Avoid printing a label for each drawn point :
		 * returns an empty string or the label depending on the label value
		 */
		public static String getLabel(Period period, String serie, int pos, ArrayList<String> series) {
			switch (period) {
				case HOUR:
					if (serie.equals("") || !serie.contains(":"))
						return "";
					// Only display 11:00, 11:10, 11:20, ...
					int minutes = Integer.parseInt(serie.split(":")[1]);
					
					if (minutes % 10 == 0
							&& (pos == 0 || pos > 0 && !series.get(pos).equals(series.get(pos-1))))
						return serie;
					else
						return "";
					
				case DAY :
					if (serie.equals("") || !serie.contains(":"))
						return "";
					// Only display 14:00, 16:00, 18:00, ...
					int hours = Integer.parseInt(serie.split(":")[0]);
					int minutes2 = Integer.parseInt(serie.split(":")[1]);
					
					if (hours % 2 == 0 && minutes2 == 0
							&& (pos == 0 || pos > 0 && !series.get(pos).equals(series.get(pos-1))))
						return serie;
					else
						return "";
					
				case WEEK:
					return "";
					
				case MONTH:
					return "";
				default: return "";
			}
		}
	}
	
	public static Number convertUnit(Unit from, Unit to, double value) {
		int fromIndex = from.getIndex();
		int toIndex = to.getIndex();
		
		if (fromIndex == toIndex)
			return value;
		else if (fromIndex < toIndex)
			return value / Math.pow(1024, toIndex - fromIndex);
		else // if (fromIndex > toIndex)
			return value * Math.pow(1024, fromIndex - toIndex);
	}
}