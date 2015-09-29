package com.hukouyi.weixin.model;

import com.jfinal.plugin.activerecord.Db;

public class Message {
	public int sentMsg(String title, String content, String sent_to) {
		String createid = System.currentTimeMillis() + "";
		return Db
				.update("insert into mt_news(id,title,content,sent_to) values(?,?,?,?)",
						createid, title, content, sent_to);
	}

	public int notreadcount(String sent_to) {
		return Db
				.findFirst(
						"select m1.title,m1.sent_to,m1.date,m2.`name`,count(*) as newmsg_count from mt_news m1 inner join mt_bman m2 on m2.phone =m1.sent_to group by m1.sent_to having m1.sent_to = ? ",sent_to)
				.getInt("newmsg_count");
	}

}
