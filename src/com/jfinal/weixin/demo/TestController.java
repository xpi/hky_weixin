package com.jfinal.weixin.demo;

import com.hukouyi.weixin.model.Bman;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.api.WebAccessToken;

public class TestController extends Controller {
	public WebAccessToken getWebAccessToken() {
		WebAccessToken wat = (WebAccessToken) getSession().getAttribute(
				"access_token");
		if (wat != null && wat.isAvailable()) {
			return wat;
		} else {
			WebAccessToken newwat = AccessTokenApi
					.getJsAccessToken(getPara("code"));
			setSessionAttr("access_token", newwat);
			return newwat;

		}
	}

	public void index() {
	
			renderText(Bman.bindWeixin("18825180000", getWebAccessToken()
				.getOpenid()) == true ? "绑定完成" : "绑定失败");
	}

	public void test() {
		ApiConfig ac = ApiConfigKit.getApiConfig();

		String timestamp = System.currentTimeMillis() + "";
		String nonceStr = "asnxiasxapfaso";
		setAttr("appId", ac.getAppId());
		setAttr("timestamp", timestamp);
		setAttr("nonceStr", nonceStr);
		setAttr("signature", AccessTokenApi.getSignature(nonceStr, timestamp,
				"http://www.hukouyi.com/weixin/hello/test"));
		render("weixin.html");
	}

}
