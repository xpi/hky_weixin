/**
 * Copyright (c) 2011-2014, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.jfinal.weixin.demo;

import java.util.Properties;

import com.hukouyi.weixin.controller.AdminPage;
import com.hukouyi.weixin.controller.BindToWeixin;
import com.hukouyi.weixin.controller.ChartsPage;
import com.hukouyi.weixin.controller.UserPage;
import com.hukouyi.weixin.interceptor.InitApiConfig;
import com.hukouyi.weixin.model.Bman;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.render.ViewType;
import com.jfinal.weixin.sdk.api.ApiConfig;
import com.jfinal.weixin.sdk.api.ApiConfigKit;

public class WeixinConfig extends JFinalConfig {

	public Properties loadProp(String pro, String dev) {
		try {
			return loadPropertyFile(pro);
		} catch (Exception e) {
			return loadPropertyFile(dev);
		}
	}

	public void configConstant(Constants me) {
		// 如果生产环境配置文件存在，则优先加载该配置，否则加载开发环境配置文件
		loadProp("a_little_config_pro.txt", "a_little_config.txt");
		me.setDevMode(getPropertyToBoolean("devMode", false));
		// ApiConfigKit 设为开发模式可以在开发阶段输出请求交互的 xml 与 json 数据
		ApiConfigKit.setDevMode(me.getDevMode());
		me.setViewType(ViewType.FREE_MARKER);
		
		
	}

	public void configRoute(Routes me) {
		me.add("/user", BindToWeixin.class);
		me.add("/user/bman", UserPage.class);
		me.add("/admin", AdminPage.class);
		me.add("/admin/charts", ChartsPage.class);
		me.add("/msg", WeixinMsgController.class);
		me.add("/api", WeixinApiController.class, "/api");
	}

	public void configPlugin(Plugins me) {
		boolean isdevurl = getPropertyToBoolean("devUrl");
		String test = isdevurl?"test":"";
		System.out.println(isdevurl);
		C3p0Plugin cp = new C3p0Plugin(getProperty(test+"jdbcUrl"),
				getProperty(test+"user"), getProperty(test+"password").trim());
		me.add(cp);
		ActiveRecordPlugin arp = new ActiveRecordPlugin(cp);
		me.add(arp);
		arp.addMapping("mt_bman", Bman.class);
		EhCachePlugin ecp = new EhCachePlugin();
		me.add(ecp);
		
	}

	public void configInterceptor(Interceptors me) {
		//me.add(new InitApiConfig());
	}

	public void configHandler(Handlers me) {
		
	}

	public static void main(String[] args) {
		JFinal.start("webapp", 8080, "/", 5);
	}
}
