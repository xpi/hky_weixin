package com.hukouyi.weixin.interceptor;

import com.hukouyi.weixin.model.Bman;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.validate.Validator;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.api.WebAccessToken;
import com.jfinal.weixin.sdk.kit.PropertyKit;

public class ValidUser extends Validator {

	@Override
	protected void handleError(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void validate(Controller controller) {
		String openid = "";
		
		boolean dev = Boolean.valueOf(PropertyKit.getConfig("devUrl",
				PropertyKit.class.getClassLoader().getResource("").getPath()
						+ "a_little_config.txt"));
		System.out.println(dev);

		if (dev) {
			openid = "openid"; // test
			System.out.println(dev);

		} else {
			openid = getWebAccessToken(controller).getOpenid();
		}
		Record testexist = Db.findFirst("select * from mt_bman where open_id = ?",openid);
		if (testexist!=null) {
			controller.redirect("/user/home");
			
			return;
		}
		controller.setAttr("openid", openid);

		String wechatName = UserApi.getUserInfo(openid).get("nickname");
		controller.getSession().setAttribute("wechatName", wechatName);
		controller.getSession().setAttribute("openid", openid);

	}

	private WebAccessToken getWebAccessToken(Controller controller) {
		WebAccessToken wat = (WebAccessToken) controller.getSession()
				.getAttribute("access_token");
		if (wat != null && wat.isAvailable()) {
			return wat;
		} else {
			wat = AccessTokenApi.getJsAccessToken(controller.getPara("code"));
			controller.setSessionAttr("access_token", wat);
			return wat;
		}
	}
}
