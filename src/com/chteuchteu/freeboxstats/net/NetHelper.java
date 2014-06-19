package com.chteuchteu.freeboxstats.net;

import java.io.IOException;
import java.io.InputStream;

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
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.chteuchteu.freeboxstats.SingleBox;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;


public class NetHelper {
	/**
	 * Check if a Freebox is discoverable on this network
	 */
	public static Freebox checkFreebox() {
		String apiCallUri = Freebox.ApiUri + "/api_version";
		HttpClient httpclient = new DefaultHttpClient();
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
			httpClient = new DefaultHttpClient();
			String uri = freebox.getApiCallUrl() + "login/authorize/";
			Log.v("", "Polling uri " + uri);
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
					Log.v("serverResponse", serverResponse);
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
		HttpClient httpclient = new DefaultHttpClient();
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
				SingleBox.getInstance().getSession().setChallenge(challenge);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return authorizeStatus;
	}
}