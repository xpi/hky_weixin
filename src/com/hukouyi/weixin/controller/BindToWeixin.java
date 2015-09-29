package com.hukouyi.weixin.controller;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.hukouyi.weixin.interceptor.InitApiConfig;
import com.hukouyi.weixin.interceptor.LoginValid;
import com.hukouyi.weixin.interceptor.ValidUser;
import com.hukouyi.weixin.model.Bman;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.weixin.sdk.api.AccessTokenApi;
import com.jfinal.weixin.sdk.api.UserApi;
import com.jfinal.weixin.sdk.api.WebAccessToken;

@Before(InitApiConfig.class)
public class BindToWeixin extends Controller {
	// 微信进入
	@Before(ValidUser.class)
	public void index() {
		setAttr("title", "激活");
		Record isExist = Bman.isValidUser((String) getSession().getAttribute(
				"openid"));
		
	}

	public void bind() {
		if (getSession().getAttribute("openid") == null) {
			redirect("/user/");
		}
		String enter_code = getPara("enter_code");
		System.out.println(enter_code);
		int isbind = Db
				.update("update mt_bman set open_id = ?,wechat_name=? where enter_code = ? ",
						(String) getSession().getAttribute("openid"),
						(String) getSession().getAttribute("wechatName"),
						enter_code);

		System.out.println(isbind);

		HashMap<String, String> jsonmap = new HashMap<String, String>();
		if (isbind == 1) {
			Db.update("update mt_bman set enter_code = ? where enter_code = ?",
					enter_code, Bman.getrandomcode(10));
			jsonmap.put("resultMsg", "绑定成功");
			jsonmap.put("next", "home");
		} else {
			jsonmap.put("resultMsg", "该号码不存在，请重新输入");
		}
		renderJson(jsonmap);
	}

	// 微信进入
	@Before(LoginValid.class)
	public void home() {
		setAttr("page", getPara("page"));
		setAttr("rank", Bman.getRank(getAttrForInt("bman_id")));
		setAttr("current_ym",
				new SimpleDateFormat("yyyy-MM").format(new Date()));
		setAttr("current_y", new SimpleDateFormat("yyyy").format(new Date()));
		render("home.html");
	}

	@Before(LoginValid.class)
	public void teams() {
		List<Record> downers = Db
				.find("select m2.id,m2.wechat_name,m2.phone,m2.create_date,m2.name,count(m3.name) as count from mt_bman m1 "
						+ "left join mt_bman m2 on m2.upper = m1.id "
						+ "left join mt_bman m3 on m3.upper = m2.id "
						+ "where m1.id = ? " + "group by m2.name",
						(Integer) getSession().getAttribute("bman_id"));

		String json = JsonKit.toJson(downers);

		renderJson(json);
	}

	@Before(LoginValid.class)
	public void bottomteams() {
		List<Record> downers = Db
				.find("select m2.id,ifnull(m2.name,'无') name,m2.wechat_name,m2.phone,m2.create_date from mt_bman m1 "
						+ "left join mt_bman m2 on m2.upper=m1.id "
						+ "where m1.id = ?", getPara("bman_id"));
		String json = JsonKit.toJson(downers);
		renderJson(json);
	}

	public void testapi() {
		String resp = "";
		resp += getPara("code");
		WebAccessToken wat = AccessTokenApi.getJsAccessToken(getPara("code"));
		resp += wat.getAccessToken();
		String json = UserApi.getUserInfo(wat.getOpenid()).getJson();
		renderText(resp + json);
	}

	public void route() {
		String pagefor = getPara("page") == null ? "home" : getPara("page");
		String appid = "wx385e1fe5663a7814";

		String uri = URLEncoder
				.encode("http://localhost:8080/weixin/user/home?page="
						+ pagefor);
		String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
				+ appid
				+ "&redirect_uri="
				+ uri
				+ "&response_type=code&scope=snsapi_userinfo&state=1&connect_redirect=1#wechat_redirect";

		redirect(url);

	}

	public void news() {
		setAttr("page", getPara("page"));
		setAttr("news_id", getPara("news_id"));
	}
	
	
	public void tops() {
		setAttr("current_y", new SimpleDateFormat("yyyy").format(new Date()));
	}
	public void test(){
	//	renderText(Bman.listallpercencount(3));
	}
	public static void main(String[] args) {
	
		String[] pages = { "teams-page", "achiv-page", "percen-page",
				"tops-page", "home", "news-page", "newsdetailsfixed-page" };
		for (String pagefor : pages) {
			String appid = "wx385e1fe5663a7814";

			String uri = "http://www.hukouyi.com/weixin/user/home?page="
					+ pagefor;
			String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
					+ appid
					+ "&redirect_uri="
					+ uri
					+ "&response_type=code&scope=snsapi_userinfo&state=1&connect_redirect=1#wechat_redirect";
			System.out.println(url);

		}
	}

}
