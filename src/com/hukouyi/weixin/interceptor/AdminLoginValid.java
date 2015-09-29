package com.hukouyi.weixin.interceptor;

import com.hukouyi.weixin.model.Bman;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.validate.Validator;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.WebAccessToken;

public class AdminLoginValid implements Interceptor {

	public void intercept(ActionInvocation ai) {
		// TODO Auto-generated method stub
		Controller c = ai.getController();
		if (c.getSession().getAttribute("user") != null) {
			ai.invoke();
		} else {
			String cokuser = c.getCookie("username");
			String coklogtime = c.getCookie("login_cookie");
			if (cokuser != null && coklogtime != null) {
				Record r = Db
						.findFirst(
								"select * from mt_admin where admin_user = ? and login_cookie=?",
								cokuser, coklogtime);
				if (null != r) {
					c.setSessionAttr("user", r);
					ai.invoke();
				}

			}
			c.redirect("/admin/login");
		}

	}
}
