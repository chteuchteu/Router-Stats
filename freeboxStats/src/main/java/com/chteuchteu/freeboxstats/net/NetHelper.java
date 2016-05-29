package com.chteuchteu.freeboxstats.net;

import android.util.Pair;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Enums.Db;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;


public class NetHelper {
	private static final int CONNECTION_TIMEOUT = 20000;
	private static final int READ_TIMEOUT = 30000;
	private static final String USER_AGENT = "FreeboxStats";
	private enum RequestMethod { GET, POST }

	private static String fetch(String uri, RequestMethod requestMethod) { return fetch(uri, requestMethod, null, null); }
	private static String fetch(String uri, RequestMethod requestMethod, String postParams) { return fetch(uri, requestMethod, postParams, null); }
	private static String fetch(String uri, RequestMethod requestMethod, String postParams, List<Pair<String, String>> headers) {
		URL url;
		try {
			url = new URL(uri);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		FooBox.log("Polling URL " + uri);

		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod.name());
			connection.setConnectTimeout(CONNECTION_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);
			connection.setRequestProperty("User-Agent", USER_AGENT);
			connection.setRequestProperty("Accept", "*/*");

			if (requestMethod == RequestMethod.POST && postParams != null) {
				connection.setDoInput(true);
				connection.setDoOutput(true);

				// Write POST
				OutputStream os = connection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
				writer.write(postParams);
				writer.flush();
				writer.close();
			}

			if (headers != null) {
				for (Pair<String, String> header : headers)
					connection.setRequestProperty(header.first, header.second);
			}


			connection.connect();

			int responseCode = connection.getResponseCode();
			//String responseMessage = connection.getResponseMessage();

			// Get response
			InputStream in = responseCode == 200 ? connection.getInputStream() : connection.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder html = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
				html.append(line);

			in.close();
			reader.close();

			return html.toString();
		} catch (SocketTimeoutException | ConnectException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}

		return null;
	}
	
	/**
	 * Check if a Freebox is discoverable on this network
	 */
	public static Freebox checkFreebox() {
		String apiCallUri = Freebox.ApiUri + "/api_version";

		String responseBody = fetch(apiCallUri, RequestMethod.GET);
		
		// Parse JSON
		try {
			JSONObject obj = new JSONObject(responseBody);
			return new Freebox(obj.getString("uid"), obj.getString("device_name"), obj.getString("api_version"),
					obj.getString("api_base_url"), obj.getString("device_type"));
		} catch (JSONException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
			return null;
		}
	}
	
	public static NetResponse authorize(Freebox freebox, String appInfo) {
		try {
			String uri = freebox.getApiCallUrl() + "login/authorize/";
			String responseBody = fetch(uri, RequestMethod.POST, appInfo);

			if (responseBody == null)
				return null;

			return new NetResponse(new JSONObject(responseBody));
		} catch (JSONException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static AuthorizeStatus getAuthorizeStatus(Freebox freebox, int trackId) {
		AuthorizeStatus authorizeStatus = null;
		
		String apiCallUri = freebox.getApiCallUrl() + "login/authorize/" + trackId;
		String responseBody = fetch(apiCallUri, RequestMethod.GET);

		if (responseBody == null)
			return null;
		
		// Check server's response
		try {
			JSONObject obj = new JSONObject(responseBody);
			
			NetResponse netResponse = new NetResponse(obj);
			
			if (netResponse.hasSucceeded()) {
				authorizeStatus = AuthorizeStatus.get(netResponse.getJsonObject().getString("status"));
				String challenge = netResponse.getJsonObject().getString("challenge");
				FooBox.getInstance().getSession().setChallenge(challenge);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return authorizeStatus;
	}
	
	public static boolean openSession(Freebox freebox) {
		if (freebox == null)
			return false;
		
		// Get challenge
		NetResponse response = getChallenge(freebox);
		
		String challenge;
		if (response != null && response.hasSucceeded()) {
			try {
				challenge = response.getJsonObject().getString("challenge");
				FooBox.getInstance().getSession().setChallenge(challenge);
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			ErrorsLogger.log(response);
			return false;
		}
		
		// Begin session
		NetResponse response2 = beginSession(freebox, challenge);
		if (response2 != null && response2.hasSucceeded()) {
			try {
				String session_token = response2.getJsonObject().getString("session_token");
				FooBox.getInstance().getSession().setSessionToken(session_token);
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			ErrorsLogger.log(response2);
			return false;
		}
	}
	
	private static NetResponse getChallenge(Freebox freebox) {
		if (freebox == null)
			return null;

		String apiCallUri = freebox.getApiCallUrl() + "login/";
		String responseBody = fetch(apiCallUri, RequestMethod.GET);

		if (responseBody == null)
			return null;
		
		// Check server's response
		try {
			JSONObject obj = new JSONObject(responseBody);
			return new NetResponse(obj);
		} catch (JSONException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
			return null;
		}
	}
	
	private static NetResponse beginSession(Freebox freebox, String challenge) {
		try {
			String uri = freebox.getApiCallUrl() + "login/session/";

			// We have to provide app_id and password
			JSONObject obj = new JSONObject();
			obj.put("app_id", FooBox.APP_ID);
			obj.put("password", Util.encodeAppToken(freebox.getAppToken(), challenge));

			String responseBody = fetch(uri, RequestMethod.POST, obj.toString());

			if (responseBody == null)
				return null;

			return new NetResponse(new JSONObject(responseBody));
		} catch (IOException | JSONException | InvalidKeyException | NoSuchAlgorithmException | SignatureException | IllegalArgumentException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static NetResponse loadGraph(Freebox freebox, Period period, ArrayList<Field> fFields) {
        if (freebox == null) {
            ErrorsLogger.log("Freebox loading fail");
            return null;
        }
        if (fFields.isEmpty()) {
            ErrorsLogger.log("Empty field list");
            return null;
        }
		try {
			String uri = freebox.getApiCallUrl() + "rrd/";

			Db db = GraphHelper.getDbFromField(fFields.get(0));

			if (db == null)
				return null;

			JSONArray fields = new JSONArray();
			for (Field f : fFields)
				fields.put(f.getSerializedValue());

			List<Pair<String, String>> pairs = new ArrayList<>();
			pairs.add(new Pair<>("db", db.getSerializedValue()));
			pairs.add(new Pair<>("fields", fields.toString()));
			if (period != null)
				pairs.add(new Pair<>("date_start", String.valueOf(Util.Times.getFrom(period))));

			if (db == Db.TEMP)
				pairs.add(new Pair<>("precision", "10"));

			uri = uri + "?" + getQuery(pairs);

			// Header params
			List<Pair<String, String>> headers = new ArrayList<>();
			headers.add(new Pair<>("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken()));


			String responseBody = fetch(uri, RequestMethod.GET, null, headers);

			if (responseBody == null)
				return null;

			return new NetResponse(new JSONObject(responseBody));
		} catch (JSONException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
			return null;
		}
	}
	
	public static boolean getPublicIP(Freebox freebox) {
		String apiCallUri = freebox.getApiCallUrl() + "connection/config";

		// Header params
		List<Pair<String, String>> headers = new ArrayList<>();
		headers.add(new Pair<>("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken()));

		String responseBody = fetch(apiCallUri, RequestMethod.GET, null, headers);
		
		if (responseBody == null)
			return false;
		
		// Parse JSON
		try {
			JSONObject obj = new JSONObject(responseBody);
			JSONObject result = obj.getJSONObject("result");
			String ip = result.getString("remote_access_ip");
			int port = result.getInt("remote_access_port");

			freebox.setIp(ip + ":" + port);

			boolean remote_access = result.getBoolean("remote_access");
			boolean api_remote_access = result.getBoolean("api_remote_access");

			freebox.setApiRemoteAccess(remote_access && api_remote_access ? Enums.SpecialBool.TRUE : Enums.SpecialBool.FALSE);
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static NetResponse getSwitchStatus(Freebox freebox) {
		if (freebox == null)
			return null;

		String apiCallUri = freebox.getApiCallUrl() + "switch/status/";

		// Header params
		List<Pair<String, String>> headers = new ArrayList<>();
		headers.add(new Pair<>("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken()));

		String responseBody = fetch(apiCallUri, RequestMethod.GET, null, headers);

		if (responseBody == null)
			return null;

		// Check server's response
		try {
			return new NetResponse(new JSONObject(responseBody));
		} catch (JSONException e) {
			e.printStackTrace();
			Crashlytics.logException(e);
			return null;
		}
	}

	private static String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		boolean first = true;

		for (Pair<String, String> param : params) {
			if (first)
				first = false;
			else
				builder.append("&");

			builder.append(URLEncoder.encode(param.first, "UTF-8"));
			builder.append("=");
			builder.append(URLEncoder.encode(param.second, "UTF-8"));
		}

		return builder.toString();
	}
}
