/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jfinal.weixin.sdk.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfinal.kit.FileKit;
import com.jfinal.kit.HttpKit;
import com.jfinal.weixin.sdk.kit.EncoderHandler;
import com.jfinal.weixin.sdk.kit.ParaMap;

/**
 * 认证并获取 access_token API
 * http://mp.weixin.qq.com/wiki/index.php?title=%E8%8E%B7%E5%8F%96access_token
 */
public class AccessTokenApi {

	// "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	private static String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
	private static String ticUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi";

	private static AccessToken accessToken;
	private static JsApiTicket ticket;

	public static AccessToken getAccessToken() {
		if (accessToken != null && accessToken.isAvailable())
			return accessToken;

		refreshAccessToken();
		return accessToken;
	}

	public static JsApiTicket getTicket() {
		if (ticket != null && ticket.isAvailable())
			return ticket;
		refreshTick();
		return ticket;
	}

	public static void refreshAccessToken() {
		accessToken = requestAccessToken();
	}

	public static void refreshTick() {
		ticket = requestTicket();
	}

	private static synchronized WebAccessToken refreshWebAccessToken(
			WebAccessToken webAccessToken) {

		ApiConfig ac = ApiConfigKit.getApiConfig();
		String json = HttpKit
				.get("https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
						+ ac.getAppId()
						+ "&grant_type=refresh_token&refresh_token="
						+ webAccessToken.getRefreshToken());
		try {
			WebAccessToken newwat = new WebAccessToken(json);
			return newwat;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private static synchronized AccessToken requestAccessToken() {
		AccessToken result = null;
		ApiConfig ac = ApiConfigKit.getApiConfig();
		for (int i = 0; i < 3; i++) {
			String appId = ac.getAppId();
			String appSecret = ac.getAppSecret();
			Map<String, String> queryParas = ParaMap.create("appid", appId)
					.put("secret", appSecret).getData();
			String json = HttpKit.get(url, queryParas);
			result = new AccessToken(json);

			if (result.isAvailable())
				break;
		}
		return result;
	}

	private static synchronized JsApiTicket requestTicket() {
		Map<String, String> queryParas = ParaMap.create("access_token",
				getAccessToken().getAccessToken()).getData();
		String json = HttpKit.get(ticUrl, queryParas);
		JsApiTicket jticket = new JsApiTicket(json);
		return jticket;
	}

	public static WebAccessToken getJsAccessToken(String code) {
		ApiConfig ac = ApiConfigKit.getApiConfig();
		String json = HttpKit
				.get("https://api.weixin.qq.com/sns/oauth2/access_token?appid="
						+ ac.getAppId() + "&secret=" + ac.getAppSecret()
						+ "&code=" + code + "&grant_type=authorization_code");
		return new WebAccessToken(json);
	}

	public static String getSignature(String noncestr, String timestamp,
			String url) {
		String str = "jsapi_ticket=" + getTicket().getTicket() + "&noncestr="
				+ noncestr + "&timestamp=" + timestamp + "&url=" + url;
		return EncoderHandler.encode("sha1", str);
	}
	 public static String readFile(String fileName) {
		    String output = ""; 
		    File file = new File(fileName);
		    if(file.exists()){
		      if(file.isFile()){
		        try{
		          BufferedReader input = new BufferedReader (new FileReader(file));
		          StringBuffer buffer = new StringBuffer();
		          String text;
		          while((text = input.readLine()) != null)
		            buffer.append(text);
		          output = buffer.toString();          
		        }
		        catch(IOException ioException){
		          System.err.println("File Error!");
		        }
		      }
		      else if(file.isDirectory()){
		        String[] dir = file.list();
		        output += "Directory contents:/n";
		         
		        for(int i=0; i<dir.length; i++){
		          output += dir[i] +"/n";
		        }
		      }
		    }
		    else{
		      System.err.println("Does not exist!");
		    }
		    return output;
		   }
	public static void main(String[] args) throws JsonParseException,
			JsonMappingException, IOException {
		ApiConfig ac = new ApiConfig();
		ac.setAppId("wx385e1fe5663a7814");
		ac.setAppSecret("507f2dd8699bdcecd8cd8b1bb0553ff6");
		ApiConfigKit.setThreadLocalApiConfig(ac);
		String mjson = readFile(Class.class.getClass().getResource("/menu.json").getPath() );
		System.out.println(mjson);
		ApiResult returns = MenuApi.createMenu(mjson);
		
		System.out.println(returns.getJson());
	}
	
}
