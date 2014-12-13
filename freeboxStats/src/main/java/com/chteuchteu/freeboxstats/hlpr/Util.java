package com.chteuchteu.freeboxstats.hlpr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.xy.XValueMarker;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Util {
	public static String getPrefString(Context c, String key) {
		return getPrefString(c, key, "");
	}
	
	public static String getPrefString(Context c, String key, String defaultValue) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getString(key, defaultValue);
	}
	
	public static boolean getPrefBoolean(Context c, String key, boolean defaultValue) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).getBoolean(key, defaultValue);
	}
	
	public static boolean hasPref(Context c, String key) {
		return c.getSharedPreferences("user_pref", Context.MODE_PRIVATE).contains(key);
	}
	
	public static void setPref(Context c, String key, String value) {
		if (value.equals(""))
			removePref(c, key);
		else {
			SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(key, value);
			editor.apply();
		}
	}
	
	public static void setPref(Context c, String key, boolean value) {
		SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}
	
	public static void removePref(Context c, String key) {
		SharedPreferences prefs = c.getSharedPreferences("user_pref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key);
		editor.apply();
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
		return Crypto.hmacSha1(challenge, app_token);
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
		
		public static long getFrom_stack(Period period) {
			Calendar cal = Calendar.getInstance();
			switch (period) {
				case HOUR:
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					break;
				case DAY:
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					break;
				case WEEK:
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
					break;
				case MONTH:
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					cal.set(Calendar.DAY_OF_MONTH, 1);
					break;
				default:
					break;
			}
			return cal.getTime().getTime() / 1000;
		}
		
		public static long getDayBeginFromTimestamp(long timestamp) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp*1000);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTimeInMillis() / 1000;
		}
		
		public static String getDate_oneMonthAgo() {
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MONTH, -1);
			SimpleDateFormat formater = new SimpleDateFormat("dd/MM");
			return formater.format(calendar.getTime());
		}
		
		public static String getDate_today() {
			Date date = new Date();
			SimpleDateFormat formater = new SimpleDateFormat("dd/MM");
			return formater.format(date);
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
					
				case DAY:
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
					long dayTimestamp = Util.Times.getDayBeginFromTimestamp(
							Integer.parseInt(serie));
					int previousIndex = pos-1;
					if (previousIndex < 0)
						previousIndex = 0;
					long previousTimestamp = Util.Times.getDayBeginFromTimestamp(Long.parseLong(series.get(previousIndex)));
					
					if (previousTimestamp == dayTimestamp)
						return "";
					
					long latestTimestamp = Util.Times.getDayBeginFromTimestamp(
							Long.parseLong(series.get(series.size()-1)));
					
					int mod = (int) ((latestTimestamp - dayTimestamp) % (eachXdays*24*60*60));
					
					if (mod == 0)
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
						long dayTimestamp = Util.Times.getDayBeginFromTimestamp(Long.parseLong(serie));
						int previousIndex = pos-1;
						if (previousIndex < 0)
							previousIndex = 0;
						long previousTimestamp = Util.Times.getDayBeginFromTimestamp(Long.parseLong(series.get(previousIndex)));
						
						if (previousTimestamp == dayTimestamp) {
							display = false;
						} else {
							long latestTimestamp = Util.Times.getDayBeginFromTimestamp(
									Long.parseLong(series.get(series.size()-1)));
							
							int mod = (int) ((latestTimestamp - dayTimestamp) % (eachXdays*24*60*60));
							
							display = (mod == 0);
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
			RobotoCondensed_Light("RobotoCondensed-Light.ttf"),
			RobotoCondensed_Regular("RobotoCondensed-Regular.ttf"),
			Roboto_Thin("Roboto-Thin.ttf"),
			Roboto_Regular("Roboto-Regular.ttf");
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
	}
	
	/**
	 * Compares two version strings. 
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical 
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1 a string of ordinal numbers separated by decimal points. 
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2. 
	 *         The result is a positive integer if str1 is _numerically_ greater than str2. 
	 *         The result is zero if the strings are _numerically_ equal.
	 */
	public static Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}
	
	public static void restartApp(Context context) {
		Intent i = context.getPackageManager()
				.getLaunchIntentForPackage(context.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}

	/**
	 * Prepares a Gmail-style progressbar on the actionBar
	 * Should be called in onCreate
	 */
	public static ProgressBar prepareGmailStyleProgressBar(final Activity activity, final ActionBar actionBar, final View paddingComponent2) {
		// create new ProgressBar and style it
		final ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
		progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24));
		progressBar.setProgress(0);
		progressBar.setVisibility(View.GONE);

		// retrieve the top view of our application
		final FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
		decorView.addView(progressBar);

		// Here we try to position the ProgressBar to the correct position by looking
		// at the position where content area starts. But during creating time, sizes
		// of the components are not set yet, so we have to wait until the components
		// has been laid out
		// Also note that doing progressBar.setY(136) will not work, because of different
		// screen densities and different sizes of actionBar
		ViewTreeObserver observer = progressBar.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				View contentView = decorView.findViewById(android.R.id.content);
				int actionBarHeight = actionBar != null ? actionBar.getHeight() : Util.getActionBarHeight(activity);
				int paddingComponent2Height = paddingComponent2 != null ? paddingComponent2.getHeight() : 0;
				int y = Util.getStatusBarHeight(activity) + actionBarHeight;

				progressBar.setY(y + contentView.getY() - 10 + paddingComponent2Height);
				progressBar.setProgressDrawable(activity.getResources().getDrawable(
						R.drawable.progress_horizontal_holo_no_background_light));

				ViewTreeObserver observer = progressBar.getViewTreeObserver();
				observer.removeGlobalOnLayoutListener(this);
			}
		});

		return progressBar;
	}

	public static int getActionBarHeight(Context c) {
		final TypedArray styledAttributes = c.getTheme().obtainStyledAttributes(
				new int[] { android.R.attr.actionBarSize });
		int height = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();
		return height;
	}

	public static int getStatusBarHeight(Context c) {
		int result = 0;
		int resourceId = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0)
			result = c.getResources().getDimensionPixelSize(resourceId);
		return result;
	}
}