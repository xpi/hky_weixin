package com.hukouyi.weixin.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hukouyi.weixin.interceptor.LoginValid;
import com.hukouyi.weixin.model.Bman;
import com.hukouyi.weixin.model.Message;
import com.jfinal.aop.Before;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@Before(LoginValid.class)
public class UserPage extends Controller {
	public Record getRank(int bman_id) {
		Record r = Db.findFirst("select percen from mt_rank where rank = 1");
		String sql = "select t6.*,t7.phone as sphone,t7.name sbman_name from(select * from (select t4.*, count(t5.name)  mcount from "
				+ "(select t1.id,t1.upper,t1.phone,t1.name, t2.count as bcount,ifnull(max(t3.rank),1) rank,ifnull(t3.percen,"
				+ r.get("percen")
				+ ") percen from mt_bman t1 "
				+ "left join (select bman_id, IF(count(*)=0,1,count(*)) count from mt_record m1 GROUP BY bman_id) t2 on t2.bman_id=t1.id "
				+ "left join mt_rank t3 on t2.count >= t3.bill_count "
				+ "group by t1.id ) t4 "
				+ "left join mt_bman t5 on t5.upper = t4.id "
				+ "group by t4.id) t6 where id = ?) t6"
				+ " left join mt_bman t7 on t7.id = t6.upper";
		return Db.findFirst(sql, bman_id);
	}

	public void earnnames() {
		String sql = "";
		String[] year_month = getPara("year_month").replace("-0", "-").split(
				"-");
		if (getParaToInt("rangetype") == 1) {
			sql = "select '1' as crange,t3.name ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "left join mt_bman t3 on t3.id=t2.bman_id\n"
					+ "where t1.bman_id = ? and t1.sbman_id != ? and count <= 20000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1] + "'\n";
		} else if (getParaToInt("rangetype") == 2) {

			sql = "select '2' as crange,t3.name   ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "left join mt_bman t3 on t3.id=t2.bman_id\n"
					+ "where  t1.bman_id = ? and t1.sbman_id != ? and count > 20000 and count <=50000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1] + "'\n";

		} else if (getParaToInt("rangetype") == 3) {
			sql = "select '3' as crange,t3.name   ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "left join mt_bman t3 on t3.id=t2.bman_id\n"
					+ "where  t1.bman_id = ? and t1.sbman_id != ? and count >50000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1] + "'  order by sign_date desc ;";
		}
		renderJson(Db.find(sql, getParaToInt("bman_id"),
				getParaToInt("bman_id")));

	}

	public void teamearn() {
		String type = getPara("type");
		String sql = "";
		String bman_id = getPara("bman_id");
		String[] year_month = getPara("year_month").replace("-0", "-").split(
				"-");
		if (type == null || type.equals("null")) {
			type = "bman_id = " + bman_id + " and sbman_id =" + bman_id;
			sql = "select '1' as crange,count(t2.id) as  bill_count,t2.sign_date   from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " and count <= 20000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "'\n"
					+ "UNION\n"
					+ "select '2' as crange,count(t2.id) as  bill_count,t2.sign_date   from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " and count > 20000 and count <=50000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "'\n"
					+ "UNION\n"
					+ "select '3' as crange,count(t2.id) as  bill_count ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " and count >50000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1] + "'  order by sign_date desc ;";

		} else if (type.equals("sbman_id")) {
			type = "bman_id = " + bman_id + " and sbman_id != " + bman_id;
			sql = "select '1' as crange,count(t2.id) as  bill_count,t2.sign_date   from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " and count <= 20000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "'\n"
					+ "UNION\n"
					+ "select '2' as crange,count(t2.id) as  bill_count,t2.sign_date   from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " and count > 20000 and count <=50000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "'\n"
					+ "UNION\n"
					+ "select '3' as crange,count(t2.id) as  bill_count ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " and count >50000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1] + "'  order by sign_date desc ;";
		} else {

			sql = "select '1' as crange,count(t2.id) as  bill_count ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " = "
					+ bman_id
					+ " and count <= 20000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "'\n"
					+ "UNION\n"
					+ "select '2' as crange,count(t2.id) as  bill_count ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " = "
					+ bman_id
					+ " and count > 20000 and count <=50000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "'\n"
					+ "UNION\n"
					+ "select '3' as crange,count(t2.id) as  bill_count ,t2.sign_date  from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t1."
					+ type
					+ " = "
					+ bman_id
					+ " and count >50000 and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1] + "'  order by sign_date desc ;";
		}
		List<Record> rs = Db.find(sql);
		renderJson(rs);
	}

	public void listpercen() {
		String[] year_month = getPara("year_month").replace("-0", "-").split(
				"-");
		Integer bman_id = (Integer) getSession().getAttribute("bman_id");

		String type = getPara("type");
		if (type.equals("sbman_id")) {
			type = " and t1.bman_id = " + bman_id + " and sbman_id = "
					+ bman_id;
		} else if (type.equals("bman_id")) {
			type = " and t1.bman_id = " + bman_id + " and sbman_id != "
					+ bman_id;
			String sql = "select '1' as crange,t2.sign_date,t1.bman_id,t1.sbman_id, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t2.count <=20000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "' "
					+ type
					+ " \n"
					+ "union\n"
					+ "select '2' as crange,t2.sign_date,t1.bman_id,t1.sbman_id, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t2.count >20000 and t2.count <=50000   and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "' "
					+ type
					+ " \n"
					+ "union\n"
					+ "select '3' as crange,t2.sign_date,t1.bman_id,t1.sbman_id,  t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t2.count >=50000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "' "
					+ type
					+ "   order by sign_date desc ";

			List<Record> percens = Db.find(sql);
			List<Record> newpercens = new ArrayList<Record>();
			for (int i = 0; i < percens.size(); i++) {
				Record r = percens.get(i);
				String sql1 = "select * from mt_bman where id = ?";
				Record r1 = Db.findFirst(sql1, r.getInt("sbman_id"));
				int upper_id = r1.getInt("upper");
				if (upper_id == bman_id) {
					newpercens.add(r);
				}
			}
			renderJson(newpercens);
			return;
		} else {
			type = " and t1.bman_id = " + bman_id + " and sbman_id != "
					+ bman_id;
			String sql = "select '1' as crange,t2.sign_date,t1.bman_id,t1.sbman_id, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t2.count <=20000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "' "
					+ type
					+ " \n"
					+ "union\n"
					+ "select '2' as crange,t2.sign_date,t1.bman_id,t1.sbman_id, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t2.count >20000 and t2.count <=50000   and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "' "
					+ type
					+ " \n"
					+ "union\n"
					+ "select '3' as crange,t2.sign_date,t1.bman_id,t1.sbman_id,  t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
					+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
					+ "where t2.count >=50000  and year(t1.sign_date)='"
					+ year_month[0]
					+ "' and month(t1.sign_date)='"
					+ year_month[1]
					+ "' "
					+ type
					+ "   order by sign_date desc ";
			List<Record> percens = Db.find(sql);
			List<Record> newpercens = new ArrayList<Record>();
			for (int i = 0; i < percens.size(); i++) {
				Record r = percens.get(i);
				String sql1 = "select * from mt_bman where id = ?";
				Record r1 = Db.findFirst(sql1, r.getInt("sbman_id"));
				int upper_id = r1.getInt("upper");
				if (upper_id != bman_id) {
					newpercens.add(r);
				}
			}
			renderJson(newpercens);
			return;

		}
		String sql = "select '1' as crange,t2.sign_date, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
				+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
				+ "where t2.count <=20000  and year(t1.sign_date)='"
				+ year_month[0]
				+ "' and month(t1.sign_date)='"
				+ year_month[1]
				+ "' "
				+ type
				+ " \n"
				+ "union\n"
				+ "select '2' as crange,t2.sign_date, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
				+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
				+ "where t2.count >20000 and t2.count <=50000   and year(t1.sign_date)='"
				+ year_month[0]
				+ "' and month(t1.sign_date)='"
				+ year_month[1]
				+ "' "
				+ type
				+ " \n"
				+ "union\n"
				+ "select '3' as crange,t2.sign_date, t2.count,t1.percen,REPLACE(REPLACE(t1.ispay,1,'已付款'),0,'未付款') ispay,t2.client_name,ifnull(t1.pay_date,'') pay_date from mt_record t1 \n"
				+ "left join mt_bill t2 on t2.id = t1.bill_id  \n"
				+ "where t2.count >=50000  and year(t1.sign_date)='"
				+ year_month[0]
				+ "' and month(t1.sign_date)='"
				+ year_month[1]
				+ "' " + type + "   order by sign_date desc ";
		List<Record> percens = Db.find(sql);
		renderJson(percens);
	}

	@ClearInterceptor
	public void percentop() {
		String sql = "select bman_id,sum(percen) citem,name , mon \n"
				+ "from (select name,percen,bman_id,month(t1.sign_date) mon  \n"
				+ "from mt_record t1 \n"
				+ "LEFT JOIN mt_bman t2 on t2.id=t1.bman_id \n"
				+ "where year(t1.sign_date)=?)t3\n" + "GROUP BY mon,bman_id\n"
				+ "order BY mon,citem desc ";
		List<Record> ptop = Db.find(sql, getPara("year"));
		renderJson(ptop);
	}

	@ClearInterceptor
	public void billtop() {
		String sql = "select bman_id,count(percen) citem,name , mon \n"
				+ "from (select name,percen,bman_id,month(t1.sign_date) mon  \n"
				+ "from mt_record t1 \n"
				+ "LEFT JOIN mt_bman t2 on t2.id=t1.bman_id \n"
				+ "where year(t1.sign_date)=?)t3\n" + "GROUP BY mon,bman_id\n"
				+ "order BY mon,citem desc ";

		List<Record> ptop = Db.find(sql, getPara("year"));
		renderJson(ptop);
	}

	@ClearInterceptor
	public void listnews() {
		int next = getParaToInt("next");
		int pagesize = 20;
		next = (int) (next * pagesize);
		List<Record> newslist = Db
				.find("select id,title from mt_news where sent_to is  null order by date desc limit ? , ?",
						next, pagesize);
		renderJson(newslist);
	}

	public void listmessage() {
		int next = getParaToInt("next");
		int pagesize = 20;
		next = (int) (next * pagesize);
		System.out.println(getAttr("phone"));
		List<Record> newslist = Db
				.find("select id,title from mt_news where sent_to = ?  order by date desc limit ? , ?",
						getAttr("phone"), next, pagesize);
		renderJson(newslist);
	}
	
	@ClearInterceptor
	public void getnews() {
		Record r = Db.findFirst("select *  from mt_news where id =?",
				getPara("news_id"));

		if (r.getStr("sent_to") != null
				&& r.getStr("sent_to").equals(getAttr("phone"))) {
			int rs = Db.update("update mt_news set isread = 1 where id = ?",
					getPara("news_id"));
			System.out.println(rs);
		}
		renderJson(r);
	}

	@ClearInterceptor
	public void getpublicnews() {
		Record r = Db.findFirst(
				"select *  from mt_news where id =? and sent_to is null",
				getPara("news_id"));
		renderJson(r);
	}

	public void unread() {
		renderJson(new Message().notreadcount(getAttrForStr("phone")));
	}

	public void details() {
		Record r = Db
				.findFirst("select *  from mt_news where title like '%方案信息%' ");
		renderJson(r);
	}

	public void mypercen() {
		int bman_id = getAttrForInt("bman_id");
		ArrayList<Integer> bman_ids = new ArrayList<Integer>();
		ArrayList<Integer> bottom_ids = new ArrayList<Integer>();
		bman_ids.add(bman_id);
		Bman.getallbottom(bman_ids, bottom_ids);
		Record table = new Record();
		table.set("allpercencount", Bman.getallpercencount(bman_id));
		table.set("allpercensum", Bman.getallpercensum(bman_id));
		table.set("personalpercencount", Bman.getpersonalpercencount(bman_id));
		table.set("personalpercensum", Bman.getpersonalpercensum(bman_id));
		table.set("dirpercencount", Bman.getdirpercencount(bman_id));
		table.set("dirpercensum", Bman.getdirpercensum(bman_id));
		table.set("extpercencount", Bman.getextpercencount(bman_id));
		table.set("extpercensum", Bman.getextpercensum(bman_id));
		table.set("overpercencount", Bman.getoverpercencount(bman_id));
		table.set("overpercensum", Bman.getoverpercensum(bman_id));
		System.out.println(table.toJson());
		renderJson(table);
	}

	public void getallpercens() {
		int bman_id = getAttrForInt("bman_id");
		String year_month = getPara("year_month");

		renderJson(Bman.listallpercencount(bman_id, year_month));

	}

	public void getselfpercens() {
		int bman_id = getAttrForInt("bman_id");
		String year_month = getPara("year_month");

		renderJson(Bman.listpersonalpercencount(bman_id, year_month));

	}

	public void getdirpercens() {
		int bman_id = getAttrForInt("bman_id");
		String year_month = getPara("year_month");

		renderJson(Bman.listdirpercencount(bman_id, year_month));
	}

	public void getextpercens() {
		int bman_id = getAttrForInt("bman_id");
		String year_month = getPara("year_month");
		renderJson(Bman.listextpercencount(bman_id, year_month));
	}
	public void getoverpercens() {
		int bman_id = getAttrForInt("bman_id");
		String year_month = getPara("year_month");
		renderJson(Bman.listoverpercencount(bman_id, year_month));
	}
	//查看单据视图
	public void getbillview(){
		Record r = Db.findFirst("select * from mt_bill_view where id = ?",getPara("bill_id"));
		renderJson(r);
	}
}
