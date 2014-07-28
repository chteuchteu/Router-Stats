package com.chteuchteu.freeboxstats.hlpr;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidplot.xy.XValueMarker;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;

public class Util {
	public static String getPrefString(Context c, String key) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getString(key, "");
	}
	
	public static boolean getPrefBoolean(Context c, String key, boolean defaultValue) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getBoolean(key, defaultValue);
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
	
	public static void setPref(Context c, String key, boolean value) {
		SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
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
		return netInfo != null && netInfo.isConnectedOrConnecting();
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
	
	public static boolean isScreenOn(Context context) {
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
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
		
		public static int getDayBeginFromTimestamp(int timestamp) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp*1000);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return (int) cal.getTimeInMillis() / 1000;
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
							&& (pos == 0 || pos > 0 && !series.get(pos).equals(series.get(pos-1)))) {
						int hours = Integer.parseInt(serie.split(":")[0]);
						if (hours == 24) // Avoid 24:30, display 00:30 instead
							return "00:" + minutes;
						else
							return serie;
					} else
						return "";
					
				case DAY :
					if (serie.equals("") || !serie.contains(":"))
						return "";
					// Only display 14:00, 16:00, 18:00, ...
					int hours2 = Integer.parseInt(serie.split(":")[0]);
					int minutes2 = Integer.parseInt(serie.split(":")[1]);
					
					if (hours2 % 4 == 0 && minutes2 == 0
							&& (pos == 0 || pos > 0 && !series.get(pos).equals(series.get(pos-1)))) {
						if (hours2 == 24) // Avoid 24:30, display 00:30 instead
							return "00:" + minutes2;
						else
							return serie;
					} else
						return "";
					
				case WEEK:
					if (serie.equals(""))
						return "";
					
					// Only display 01/02, 02/02, 03/02, ... at midday (12:00)
					String dateLabel = serie.split(":")[0];
					int hours3 = Integer.parseInt(serie.split(":")[1]);
					if (hours3 == 12 && (pos == 0 || pos > 0 && !series.get(pos).equals(series.get(pos-1))))
						return dateLabel;
					else
						return "";
					
				case MONTH:
					if (serie.equals(""))
						return "";
					
					// Only display one day out of X
					int eachXdays = 5;
					int dayTimestamp = Util.Times.getDayBeginFromTimestamp(
							Integer.parseInt(serie));
					int previousIndex = pos-1;
					if (previousIndex < 0)
						previousIndex = 0;
					int previousTimestamp = Util.Times.getDayBeginFromTimestamp(Integer.parseInt(series.get(previousIndex)));
					
					if (previousTimestamp == dayTimestamp)
						return "";
					
					int latestTimestamp = Util.Times.getDayBeginFromTimestamp(
							Integer.parseInt(series.get(series.size()-1)));
					
					int mod = (latestTimestamp - dayTimestamp) % (eachXdays*24*60*60);
					
					if (mod == 0 && previousTimestamp != dayTimestamp)
						return GraphHelper.getDateLabelFromTimestamp(dayTimestamp, null);
					else
						return "";
				default: return "";
			}
		}
		
		public static ArrayList<XValueMarker> getMarkers(Period period, ArrayList<String> series) {
			ArrayList<XValueMarker> markers = new ArrayList<XValueMarker>();
			
			String lastText = "";
			int pos = 0;
			for (String serie : series) {
				boolean display = false;
				String currentText = "";
				
				switch (period) {
					case HOUR:
						if (serie.equals("") || !serie.contains(":"))
							display = false;
						else {
							// Only display 11:00, 11:10, 11:20, ...
							int minutes = Integer.parseInt(serie.split(":")[1]);
							
							if (minutes % 10 == 0) {
								display = true;
								currentText = serie;
							}
						}
						break;
					case DAY:
						if (serie.equals("") || !serie.contains(":"))
							display = false;
						else {
							// Only display 14:00, 16:00, ...
							int hours = Integer.parseInt(serie.split(":")[0]);
							int minutes = Integer.parseInt(serie.split(":")[1]);
							
							if (hours % 4 == 0 && minutes == 0) {
								display = true;
								currentText = serie;
							}
						}
						break;
					case WEEK:
						if (serie.equals(""))
							display = false;
						else {
							int hours3 = Integer.parseInt(serie.split(":")[1]);
							if (hours3 == 24 && (currentText.equals("") || !serie.equals(currentText))) {
								display = true;
								currentText = serie;
							}
						}
						break;
					case MONTH:
						// Only display one day out of X
						int eachXdays = 5;
						int dayTimestamp = Util.Times.getDayBeginFromTimestamp(
								Integer.parseInt(serie));
						int previousIndex = pos-1;
						if (previousIndex < 0)
							previousIndex = 0;
						int previousTimestamp = Util.Times.getDayBeginFromTimestamp(Integer.parseInt(series.get(previousIndex)));
						
						if (previousTimestamp == dayTimestamp) {
							display = false;
						} else {
							int latestTimestamp = Util.Times.getDayBeginFromTimestamp(
									Integer.parseInt(series.get(series.size()-1)));
							
							int mod = (latestTimestamp - dayTimestamp) % (eachXdays*24*60*60);
							
							display = (mod == 0 && previousTimestamp != dayTimestamp);
							currentText = serie;
						}
						break;
					default:
						break;
				}
				
				if (display && !currentText.equals(lastText)) {
					XValueMarker marker = new XValueMarker(pos, "");
					markers.add(marker);
					lastText = currentText;
				}
				
				pos++;
			}
			
			return markers;
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
	
	public static final class Fonts {
		/* ENUM Custom Fonts */
		public enum CustomFont {
			RobotoCondensed_Light("RobotoCondensed-Light.ttf"), RobotoCondensed_Regular("RobotoCondensed-Regular.ttf"), Roboto_Thin("Roboto-Thin.ttf");
			final String file;
			private CustomFont(String fileName) { this.file = fileName; }
			public String getValue() { return this.file; }
		}
		
		/* Fonts */
		public static void setFont(Context c, ViewGroup g, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			setFont(g, mFont);
		}
		
		public static void setFont(Context c, TextView t, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			t.setTypeface(mFont);
		}
		
		public static void setFont(Context c, Button t, CustomFont font) {
			Typeface mFont = Typeface.createFromAsset(c.getAssets(), font.getValue());
			t.setTypeface(mFont);
		}
		
		private static void setFont(ViewGroup group, Typeface font) {
			int count = group.getChildCount();
			View v;
			for (int i = 0; i < count; i++) {
				v = group.getChildAt(i);
				if (v instanceof TextView || v instanceof EditText || v instanceof Button) {
					((TextView) v).setTypeface(font);
				} else if (v instanceof ViewGroup)
					setFont((ViewGroup) v, font);
			}
		}
		
		public static Typeface getTypeFace(Context c, CustomFont name) {
			return Typeface.createFromAsset(c.getAssets(), name.getValue());
		}
	}
}