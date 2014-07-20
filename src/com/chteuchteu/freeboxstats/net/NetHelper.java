package com.chteuchteu.freeboxstats.net;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Enums.Db;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;


public class NetHelper {
	/**
	 * Get HttpParams with timeout and all
	 */
	private static HttpParams getHttpParams() {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
		return httpParameters;
	}
	
	/**
	 * Check if a Freebox is discoverable on this network
	 */
	public static Freebox checkFreebox() {
		String apiCallUri = Freebox.ApiUri + "/api_version";
		HttpClient httpclient = new DefaultHttpClient(getHttpParams());
		String responseBody = "";
		try {
			HttpGet httpget = new HttpGet(apiCallUri);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		
		if (responseBody.equals(""))
			return null;
		
		// Parse JSON
		try {
			JSONObject obj = new JSONObject(responseBody);
			return new Freebox(obj.getString("uid"), obj.getString("device_name"), obj.getString("api_version"),
					obj.getString("api_base_url"), obj.getString("device_type"));
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static NetResponse authorize(Freebox freebox, String appInfo) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		InputStream inStream = null;
		NetResponse netResponse = null;
		
		try {
			httpClient = new DefaultHttpClient(getHttpParams());
			String uri = freebox.getApiCallUrl() + "login/authorize/";
			FooBox.log("Polling uri " + uri);
			httpPost = new HttpPost(uri);
			HttpEntity postEntity = new ByteArrayEntity(appInfo.getBytes("UTF-8"));
			httpPost.setEntity(postEntity);
			
			// Execute and get the response
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				inStream = entity.getContent();
				try {
					String serverResponse = Util.Streams.convertStreamtoString(inStream);
					FooBox.log("serverResponse", serverResponse);
					// Check server's response
					JSONObject obj = new JSONObject(serverResponse);
					netResponse = new NetResponse(obj);
				} finally {
					inStream.close();
				}
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (JSONException exception) {
			exception.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return netResponse;
	}
	
	public static AuthorizeStatus getAuthorizeStatus(Freebox freebox, int trackId) {
		AuthorizeStatus authorizeStatus = null;
		
		String apiCallUri = freebox.getApiCallUrl() + "login/authorize/" + trackId;
		HttpClient httpclient = new DefaultHttpClient(getHttpParams());
		String responseBody = "";
		try {
			HttpGet httpget = new HttpGet(apiCallUri);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		
		if (responseBody.equals(""))
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
		// Get challenge
		NetResponse response = getChallenge(freebox);
		
		String challenge = "";
		if (response != null && response.hasSucceeded()) {
			try {
				challenge = response.getJsonObject().getString("challenge");
				FooBox.getInstance().getSession().setChallenge(challenge);
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		} else return false;
		
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
		} else return false;
	}
	
	private static NetResponse getChallenge(Freebox freebox) {
		NetResponse netResponse = null;
		String apiCallUri = freebox.getApiCallUrl() + "login/";
		HttpClient httpclient = new DefaultHttpClient(getHttpParams());
		String responseBody = "";
		try {
			HttpGet httpget = new HttpGet(apiCallUri);
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		
		if (responseBody.equals(""))
			return null;
		
		// Check server's response
		try {
			JSONObject obj = new JSONObject(responseBody);
			netResponse = new NetResponse(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return netResponse;
	}
	
	private static NetResponse beginSession(Freebox freebox, String challenge) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		InputStream inStream = null;
		NetResponse netResponse = null;
		
		try {
			httpClient = new DefaultHttpClient(getHttpParams());
			String uri = freebox.getApiCallUrl() + "login/session/";
			FooBox.log("Polling uri " + uri);
			// We have to provide app_id and password
			JSONObject obj = new JSONObject();
			obj.put("app_id", FooBox.APP_ID);
			obj.put("password", Util.encodeAppToken(freebox.getAppToken(), challenge));
			httpPost = new HttpPost(uri);
			HttpEntity postEntity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
			httpPost.setEntity(postEntity);
			
			// Execute and get the response
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				inStream = entity.getContent();
				try {
					String serverResponse = Util.Streams.convertStreamtoString(inStream);
					FooBox.log("serverResponse", serverResponse);
					// Check server's response
					JSONObject obj2 = new JSONObject(serverResponse);
					netResponse = new NetResponse(obj2);
				} finally {
					inStream.close();
				}
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (JSONException exception) {
			exception.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return netResponse;
	}
	
	public static NetResponse loadGraph(Freebox freebox, Period period, ArrayList<Field> fFields) {
		if (fFields.size() == 0)
			return null;
		
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		InputStream inStream = null;
		NetResponse netResponse = null;
		
		try {
			httpClient = new DefaultHttpClient(getHttpParams());
			String uri = freebox.getApiCallUrl() + "rrd/";
			FooBox.log("Polling uri " + uri);
			JSONObject obj = new JSONObject();
			Db db = GraphHelper.getDbFromField(fFields.get(0));
			obj.put("db", db.getSerializedValue());
			JSONArray fields = new JSONArray();
			for (Field f : fFields)
				fields.put(f.getSerializedValue());
			obj.put("fields", fields);
			obj.put("date_start", Util.Times.getFrom(period));
			if (db == Db.TEMP)
				obj.put("precision", 10);
			httpPost = new HttpPost(uri);
			HttpEntity postEntity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
			httpPost.setEntity(postEntity);
			httpPost.setHeader("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken());
			httpPost.addHeader("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken());
			
			// Execute and get the response
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				inStream = entity.getContent();
				try {
					String serverResponse = Util.Streams.convertStreamtoString(inStream);
					FooBox.log("serverResponse", serverResponse);
					// Check server's response
					JSONObject obj2 = new JSONObject(serverResponse);
					netResponse = new NetResponse(obj2);
				} finally {
					inStream.close();
				}
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (JSONException exception) {
			exception.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return netResponse;
	}
	
	public static String getPublicIP(Freebox freebox) {
		String apiCallUri = freebox.getApiCallUrl() + "connection/config";
		FooBox.log("Polling URI " + apiCallUri);
		HttpClient httpclient = new DefaultHttpClient(getHttpParams());
		String responseBody = "";
		try {
			HttpGet httpget = new HttpGet(apiCallUri);
			httpget.setHeader("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken());
			httpget.addHeader("X-Fbx-App-Auth", FooBox.getInstance().getSession().getSessionToken());
			
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		
		if (responseBody.equals(""))
			return null;
		
		// Parse JSON
		try {
			JSONObject obj = new JSONObject(responseBody);
			JSONObject result = obj.getJSONObject("result");
			String ip = result.getString("remote_access_ip");
			int port = result.getInt("remote_access_port");
			return ip + ":" + port;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}