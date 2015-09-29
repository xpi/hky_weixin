package com.hukouyi.weixin.interceptor;

import com.hukouyi.weixin.model.Bman;
import com.hukouyi.weixin.model.Message;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.validate.Validator;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.WebAccessToken;
import com.jfinal.weixin.sdk.kit.PropertyKit;

public class LoginValid implements Interceptor {

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

	public void intercept(ActionInvocation ai) {
		Controller c = ai.getController();
		String openid = "";
		boolean dev = Boolean.valueOf(PropertyKit.getConfig("devUrl",
				PropertyKit.class.getClassLoader().getResource("").getPath()
						+ "a_little_config.txt"));
		System.out.println(dev);
		if (dev) {
			openid = "openid"; // test
		} else {
			openid = getWebAccessToken(c).getOpenid();
		}

		Record r = isValidUser(openid);

		if (r == null) {
			c.redirect("/user");
		} else {
			int bid = r.getInt("id");
			int prank = r.getInt("prank");
			long trank = Bman.getRank(bid).getLong("rank");
			if (prank != trank) {
				Db.update("update mt_bman set prank = ? where id = ?", trank,
						bid);
				new Message().sentMsg(
						"等级变更提示",
						"根据您部门前三个月的总签单数变动，您的等级由#oldrank#级变为#newrank#级。"
								.replace("#oldrank#", prank + "").replace(
										"#newrank#", trank + ""), r
								.getStr("phone"));
			}
			ai.getController().setAttr("bman_id", bid);
			ai.getController().setAttr("bman_name", r.get("name"));
			ai.getController().setAttr("phone", r.get("phone"));
			ai.getController().setAttr("wechat_name", r.get("wechat_name"));
			Record msgr = Db
					.findFirst(
							"select title,sent_to,date,count(*) as newmsg_count from\n"
									+ "(select m1.title,m1.sent_to,m1.date,m2.`name` from mt_news m1 \n"
									+ "inner join mt_bman m2 on m2.phone =m1.sent_to  where isread=0)t1\n"
									+ "GROUP BY sent_to\n"
									+ "having sent_to = ?", r.get("phone"));
			long unread = msgr == null ? 0 : msgr.getLong("newmsg_count");
			System.out.println(unread);
			ai.getController().setAttr("unread", unread);
			ai.getController().getSession()
					.setAttribute("bman_id", r.get("id"));
			ai.getController().setAttr("openid", openid);
			ai.invoke();
		}
	}

	public Record isValidUser(String openid) {
		Record bman = Db.findFirst("select * from mt_bman where open_id = ?",
				openid);
		return bman;

	}
}
