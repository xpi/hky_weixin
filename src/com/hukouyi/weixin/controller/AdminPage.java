package com.hukouyi.weixin.controller;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;

import com.hukouyi.weixin.interceptor.AdminLoginValid;
import com.hukouyi.weixin.interceptor.BaseInfoAppend;
import com.hukouyi.weixin.model.Bill;
import com.hukouyi.weixin.model.Bman;
import com.hukouyi.weixin.model.Message;
import com.jfinal.aop.Before;
import com.jfinal.aop.ClearInterceptor;
import com.jfinal.aop.ClearLayer;
import com.jfinal.core.Controller;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.weixin.sdk.kit.EncoderHandler;
import com.jfinal.weixin.sdk.kit.MyDateKit;
import com.sun.org.apache.regexp.internal.RE;

@Before({ AdminLoginValid.class, BaseInfoAppend.class })
public class AdminPage extends Controller {
	@ClearInterceptor
	public void login() {

		if (getSession().getAttribute("user") != null) {
			redirect("/admin/");
			return;
		}
		String admin_name = getPara("admin_name");
		if (admin_name == null || admin_name.equals("")) {
			render("login.html");
			return;
		}

		String admin_pwd = getPara("admin_pwd");
		String remember = getPara("remember");
		Record r = Db
				.findFirst(
						"select * from mt_admin where admin_user = ? and admin_pwd = password(?)",
						admin_name, admin_pwd);

		if (r != null) {
			getSession().setAttribute("user", r);
			if (remember != null) {
				setCookie("username", admin_name, 2147483644);
				String logintime = System.currentTimeMillis() + "";
				Db.update(
						"update mt_admin set login_cookie = ? where admin_user = ?",
						logintime, admin_name);

				setCookie("login_cookie", logintime, 2147483644);
			}
			redirect("/admin/");
		} else {
			String error_msg = "用户名密码不正确";
			setAttr("error_msg", error_msg);
			render("login.html");
		}
	}

	@ClearInterceptor
	public void logout() {
		getSession().removeAttribute("user");
		Db.update(
				"update mt_admin set login_cookie = null where admin_user = ?",
				getCookie("username"));
		removeCookie("username");
		removeCookie("login_cookie");
		redirect("/admin/login");

	}

	public void index() {

		setAttr("charttype", "index");
		render("index.html");
	}

	public void main() {
		render("main.html");
	}

	public void getMutiPage(Controller c, String sql, String attrname,
			Object... sqlparam) {
		int from = (c.getParaToInt("from") == null ? 0 : c.getParaToInt("from"));
		int pre = from - 1;
		int next = from + 1;
		ArrayList<Object> sqlparams = new ArrayList<Object>();
		for (Object object : sqlparam) {
			sqlparams.add(object);
		}
		sqlparams.add(from * 15);
		sqlparams.add(15);
		List<Record> mbs = Db.find(sql + " limit ? , ?", sqlparams.toArray());
		c.setAttr("pre", pre < 0 ? 0 : pre);
		c.setAttr("next", mbs.size() == 0 ? from : next);
		String json = JsonKit.toJson(mbs);
		System.out.println(json);
		c.setAttr(attrname, json);
		c.setAttr("currentpage", next);
	}

	public void memberstree() {
		String bman_id = getPara("bman_id");
		String sql = null;
		ArrayList<Record> roots = new ArrayList<Record>();
		if (bman_id == null) {
			sql = "select * from mt_bman where upper is null";
			List<Record> rlist = Db.find(sql);
			for (Record r : rlist) {
				roots.add(getRank(r.getInt("id")));
			}
			renderJson(roots);

		} else {

			sql = "select * from mt_bman where upper = ?";
			List<Record> rlist = Db.find(sql, bman_id);
			for (Record r : rlist) {
				roots.add(getRank(r.getInt("id")));
			}
			renderJson(roots);
		}
	}

	public void mtreeview() {

	}

	public void members() {
		getMutiPage(this, "select * from mt_bman where upper is null",
				"members");

		// int from = (getParaToInt("from") == null ? 0 : getParaToInt("from"));
		// int pre = from - 1;
		// int next = from + 1;
		//
		// List<Record> mbs = Db.find(
		// "select * from mt_bman where upper is null limit ? , ?", from*10,2);
		//
		// setAttr("pre", pre < 0 ? 0 : pre);
		// setAttr("next", mbs.size() == 0 ? from : next);
		//
		//
		// String json = JsonKit.toJson(mbs);
		// setAttr("members", json);
	}

	public void getMembersById() {

		getMutiPage(this,
				"select * from mt_bman where upper = ? order by create_date",
				"members", getParaToInt("bmanid"));

		render("membersbyid.html");
	}

	public void membersbyname() {
		String namekey = getPara("namekey") == null ? "" : getPara("namekey");
		setAttr("namekey", namekey);
		namekey = "%" + namekey + "%";

		getMutiPage(this,
				"select * from mt_bman where name like ? order by create_date",
				"members", namekey);

		render("membersbyname.html");
	}

	public void bills() {

		String keyw = getPara("keyw") == null ? "" : getPara("keyw");
		
		System.out.println(keyw);
		if (keyw != null) {
			setAttr("keyw", keyw + "");
			keyw = "%" + keyw + "%";
			getMutiPage(
					this,
					"select m1.id,m1.client_name,m1.count,m2.name as bman_name, "
							+ "m1.apply_type,m1.sign_date,m1.ispay from mt_bill m1 "
							+ "left join mt_bman m2 on m2.id=m1.bman_id "
							+ "where m2.name like ? or m1.client_name like ? order by m1.sign_date desc",
					"members", keyw, keyw);
		} else {
			getMutiPage(
					this,
					"select m1.id,m1.client_name,m1.count,m2.name as bman_name, "
							+ "m1.apply_type,m1.sign_date,m1.ispay from mt_bill m1 "
							+ "left join mt_bman m2 on m2.id=m1.bman_id "
							+ " order by m1.sign_date desc",
					"members");
		}
	}

	public void addbill() {

	}

	public void findbman() {
		String namekey = "%" + getPara("namekey") + "%";
		List<Record> bmans = Db.find("select * from mt_bman where name like ?",
				namekey);
		renderJson(bmans);
	}

	public void membersbyrank() {

		int rankoption = getParaToInt("rank");

		setAttr("rankoption", rankoption);
		String sql = "SELECT * from (select t6.*,t7.phone as sphone,t7.name sbman_name from(select * from (select t4.*,count(t5.name) mcount from \n"
				+ "(select t1.*, t2.count as bcount,ifnull(max(t3.rank),1) rank from mt_bman t1 \n"
				+ "left join (select bman_id,count(*) count from mt_record m1 GROUP BY bman_id) t2 on t2.bman_id=t1.id \n"
				+ "left join mt_rank t3 on t2.count >= t3.bill_count \n"
				+ "group by t1.id ) t4 \n"
				+ "left join mt_bman t5 on t5.upper = t4.id  \n"
				+ "group by t4.id) t6) t6\n"
				+ "left join mt_bman t7 on t7.id = t6.upper)t8 where rank=? ";

		getMutiPage(this, sql, "members", getParaToInt("rank"));
	}

	public void deleteMemberById() {
		Integer delbman_id = getParaToInt("bman_id");
		String fsql = "select * from mt_bman where id = ?";
		Record todelupper = Db.findFirst(fsql, delbman_id);
		if (todelupper != null) {
			String bottoms = "select * from mt_bman where upper = ?";
			List<Record> todelbottoms = Db.find(bottoms, delbman_id);
			System.out.println(todelupper);
			for (Record record : todelbottoms) {
				Db.update("update mt_bman set upper = ? where id = ?",
						todelupper.getInt("upper"), record.getInt("id"));
			}
		}
		String sql = "delete  from mt_bman where id = ?";
		Db.update(sql, delbman_id);
		redirect("/admin/members");
	}

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

	public void test() {

		renderJson(getRank(9));
	}

	public double getconf(String varname) {
		return Double.valueOf(Db.findFirst(
				"select* from mt_conf where var_name = ?", varname).getStr(
				"value"));
	}

	// 添加签单以及所有的提成记录
	public void addSignBill() {
		Integer bman_id = getParaToInt("bman_id");
		String bill_id = System.currentTimeMillis() + "";
		double count = Double.valueOf(getPara("count"));
		int result = Db
				.update("insert into mt_bill(id,client_name,count,client_phone,bman_id,apply_type,comments) values(?,?,?,?,?,?,?)",
						bill_id, getPara("client_name"), count,
						getPara("client_phone"), bman_id,
						getPara("apply_type"), getPara("comments"));
		if (result > 0) {
			new Message().sentMsg("订单添加", "您的签单(顾客:" + getPara("client_name")
					+ ")录入", getPara("bman_phone"));
		}
		if (getPara("percenbman_ids") == null) {
			renderHtml("奖金链不能为空<a href='bills'>返回</a>");
			return;
		}
		String[] percenbman_ids = getPara("percenbman_ids").split(",");
		for (String b_id : percenbman_ids) {
			int ispay = 0;
			String[] ptypes = getParaValues("percentype_" + b_id);
			String[] uppercens = getParaValues("uppercen_" + b_id);
			if (ptypes.length > 1) {
				for (int j = 0; j < ptypes.length; j++) {
					Db.update(
							"insert into mt_record(bman_id,percen,bill_id,sbman_id,ispay,percen_type) values(?,?,?,?,?,?)",
							b_id, uppercens[j], bill_id, bman_id, ispay,
							ptypes[j]);
				}
				continue;
			}
			Db.update(
					"insert into mt_record(bman_id,percen,bill_id,sbman_id,ispay,percen_type) values(?,?,?,?,?,?)",
					b_id, getPara("uppercen_" + b_id), bill_id, bman_id, ispay,
					getPara("percentype_" + b_id));
		}

		redirect("/admin/billpercen?bill_id=" + bill_id);
	}

	public void delbill() {
		int result = Db.update("delete from mt_bill where id = ?",
				getPara("bill_id"));
		redirect("/admin/bills");

	}

	public void billpercen() {
		String filterStr = "";
		ArrayList<Object> sqlparams = new ArrayList<Object>();
		Integer bman_id = getParaToInt("bman_id");
		Integer sbman_id = getParaToInt("sbman_id");
		String bill_id = getPara("bill_id");
		Record rbill = Db
				.findFirst(
						"select m2.name from mt_bill m1 left join mt_bman m2 on m1.bman_id=m2.id where m1.id = ?",
						bill_id);
		if (rbill != null) {
			String sbn = rbill.getStr("name");
			setAttr("sbman_name", sbn);
			setAttr("bill_id", bill_id);

		}

		String bman_name = getPara("bman_name");
		String sbman_namek = getPara("sbman_name");
		Integer ispay = getParaToInt("ispay");
		Integer cstart = getParaToInt("cstart") == null ? 0
				: getParaToInt("cstart");
		Integer cend = getParaToInt("cend") == null ? 0 : getParaToInt("cend");

		setAttr("bman_name", bman_name);

		setAttr("ispay", ispay);
		setAttr("cstart", cstart);
		setAttr("cend", cend);

		if (cstart == 0 && cend != 0) {
			sqlparams.add(cend);
			filterStr += " and m4.count <= ? ";
		}

		if (cend == 0 && cstart != 0) {
			sqlparams.add(cstart);
			filterStr += " and m4.count >= ? ";
		}

		if (cend != 0 && cstart != 0) {
			sqlparams.add(cstart);
			sqlparams.add(cend);
			filterStr += " and m4.count >= ? and m4.count <= ? ";
		}

		if (bman_id != null) {
			sqlparams.add(bman_id);
			filterStr += "and m2.id=? ";
		}
		if (sbman_id != null) {

			sqlparams.add(sbman_id);

			filterStr += "and m1.sbman_id=? ";
		}

		if (sbman_namek != null) {

			sqlparams.add("%" + sbman_namek + "%");

			filterStr += "and m5.name like ? ";
		}
		if (bman_name != null) {

			sqlparams.add("%" + bman_name + "%");

			filterStr += "and m2.name like ? ";
		}
		if (bill_id != null) {

			sqlparams.add(bill_id);

			filterStr += "and m1.bill_id=? ";
		}
		if (ispay != null) {

			sqlparams.add(ispay);

			filterStr += "and m1.ispay=? ";
		}
		String sql = "select m1.id,m5.name as sbman_name,m1.percen,m1.sbman_id,m1.bill_id,m1.percen_type,m2.name as bman_name,m2.id as bman_id,m1.pay_date,"
				+ " m1.ispay ,m4.count,m4.sign_date from mt_record m1 "
				+ "left join mt_bman m2 on m2.id=m1.bman_id "
				+ "left join mt_bill m4 on m4.id = m1.bill_id "
				+ " left join mt_bman m5 on m5.id = m1.sbman_id "
				+ "where 1=1 " + filterStr + "order by m4.sign_date desc ";
		System.out.println(sql);

		getMutiPage(this, sql, "members", sqlparams.toArray());
		render("billpercen.html");
	}

	public void confirmpercen() {

		String record_comment = getPara("record_comment");
		String bill_id = getPara("bill_id");
		String record_id = getPara("record_id");
		Record bman_info = Db
				.findFirst(
						"select m1.sign_date,m1.account_type,m1.account_num,m1.percen_type,m2.name,m2.phone from mt_record m1 left join mt_bman m2 on m2.id=m1.bman_id where m1.id = ?",
						record_id);
		int result = Db
				.update("update mt_record set percen = ? , account_type = ? , account_num = ? , ispay = 1,pay_date=CURRENT_TIMESTAMP,record_comment=? where id = ?",
						getParaToInt("percen"), getPara("account_type"),
						getPara("account_num"), record_comment, record_id);
		if (result > 0) {
			new Message().sentMsg("提成发放通知", "您在签单(" + bill_id + ")中的"
					+ bman_info.getStr("percen_type") + "已发放："
					+ getParaToInt("percen") + "元", bman_info.getStr("phone"));
		}
		Record r = Db.findFirst(
				"select * from mt_record where bill_id = ? and ispay = 0",
				bill_id);
		if (r == null) {
			Db.update("update mt_bill set ispay=1 where id = ?", bill_id);
		}
		redirect("/admin/billpercen?bill_id=" + bill_id);
	}

	public void paypercen() {
		// Db.update("update mt_record set ispay = 1 where id = ?",
		// getParaToInt("record_id"));
		Record record = Db
				.findFirst(
						"select m1.bill_id,m1.percen_type,m1.sbman_id,m1.bman_id, m2.name sbman_name,"
								+ "m3.name as bman_name,percen,sign_date,pay_date,m1.id,m3.account_type,"
								+ "m3.account_num from mt_record m1 left join mt_bman m2 on m2.id=m1.sbman_id "
								+ "left join mt_bman m3 on m3.id=m1.bman_id where m1.id = ? ",
						getParaToInt("record_id"));
		String[] cnames = record.getColumnNames();
		setAttr("bill_id", record.get("bill_id"));
		for (String name : cnames) {
			setAttr(name, record.get(name));
		}

		setAttr("relation", record.get("percen_type"));
		render("paypercenpage.html");
	}

	public void updatepercen() {
		int result = Db.update("update mt_record set percen = ? where id = ?",
				getPara("percen"), getParaToInt("id"));
		renderJson(result);
	}

	public void computepercen() {
		long rank = getRank(getParaToInt("bman_id")).getLong("rank");
		String sql = " select * from mt_rank  where rank >=? order by rank asc limit 0,1";
		Record percen = Db.findFirst(sql, rank);
		double lt_rate = Double.valueOf(getconf("lt2"));
		double bottom = Double.valueOf(getconf("bottom"));
		double top = Double.valueOf(getconf("top"));
		int count = getParaToInt("count");
		System.out.println(percen.getDouble("percen"));
		if (count < bottom) {
			percen.set("percen", percen.getDouble("percen") * lt_rate);
		} else if (count >= top) {
			List<Record> list = Db
					.find("select * from mt_pscope order by count asc");
			// 超出基准的分段比率
			for (int i = 0; i < list.size(); i++) {
				if (count <= list.get(i).getDouble("count")) {
					percen.set(
							"percen",
							percen.getDouble("percen")
									* list.get(i).getDouble("rate"));
					break;
				}
			}
		}
		renderJson(percen);
	}

	public void bmaninfo() {
		int bman_id = getParaToInt("bman_id");
		String sql = "select * from mt_bman where id = ?";
		Record bman = Db.findFirst(sql, bman_id);
		Record rank = getRank(bman_id);
		// System.out.println(bman.toJson());
		setAttr("id", bman.get("id"));
		setAttr("upper", bman.get("upper"));
		setAttr("name", bman.get("name"));
		setAttr("phone", bman.get("phone"));
		setAttr("create_date", bman.get("create_date"));
		setAttr("account_type", bman.get("account_type"));
		setAttr("account_num", bman.get("account_num"));
		setAttr("upper", bman.get("upper"));
		setAttr("sbman_name", rank.get("sbman_name"));
		setAttr("sphone", rank.get("sphone"));
		setAttr("openid", bman.get("open_id"));
		setAttr("wechat_name", bman.get("wechat_name"));
		setAttr("comments", bman.get("comments"));
		setAttr("enter_code", bman.get("enter_code"));
		setAttr("personbill", new Bill().getPersonalBill(bman_id));
		setAttr("bottombill", new Bill().getBottomBill(bman_id));
		setAttr("mcount", rank.get("mcount") == null ? 0 : rank.get("mcount"));
		// setAttr("rank", bman.get("rank") == null ? 0 : bman.get("rank"));
		System.out.println(getRank(bman_id).toJson());
		setAttr("rank", rank.get("rank"));
		setAttr("bcount", rank.get("bcount") == null ? 0 : rank.get("bcount"));
	}

	public void bmaninfoupdate() {
		if (getPara("upper_id") != null) {
			Integer upperid = getParaToInt("upper_id");
			Db.update(
					"update mt_bman set phone=?,account_num=?,account_type=?,upper=?,comments=? where id = ? ",
					getPara("phone"), getPara("account_num"),
					getPara("account_type"), upperid, getPara("comments"),
					getPara("bman_id"));
		} else {
			Db.update(
					"update mt_bman set phone=?,account_num=?,account_type=?,comments=? where id = ? ",
					getPara("phone"), getPara("account_num"),
					getPara("account_type"), getPara("comments"),
					getPara("bman_id"));
		}
		if(getPara("uptoupper")!=null){
			Db.update("update mt_bman set upper = null where id = ?",getPara("bman_id"));
		}

		redirect("/admin/bmaninfo?bman_id=" + getPara("bman_id"));

	}

	public void bmanmanage() {
		String namekey = getPara("namekey") == null ? "" : getPara("namekey");
		setAttr("namekey", namekey);
		namekey = "%" + namekey + "%";

		getMutiPage(
				this,
				"select * from mt_bman where name like ? order by create_date desc",
				"members", namekey);

		render("bmanmanage.html");
	}

	// 检验手机号码正确性
	public void checkphone() {
		String phone = getPara("phone") == null ? "" : getPara("phone");
		Record r = Db.findFirst(
				"select id,name,phone from mt_bman where phone = ? ", phone);
		HashMap<String, String> error_result = new HashMap<String, String>();
		error_result.put("errorMsg", "不存在的手机号码");
		if (r == null) {
			renderJson(error_result);

		} else {
			renderJson(r);
		}
	}

	@SuppressWarnings("unused")
	public void getuppercenbyphone() {
		String phone = getPara("phone") == null ? "" : getPara("phone");
		Record r = Db.findFirst(
				"select id,name,phone,upper from mt_bman where phone = ? ",
				phone);
		double relpercen = 0;
		if (r == null) {

			HashMap<String, String> error_result = new HashMap<String, String>();
			error_result.put("errorMsg", "不存在的手机号码");
			renderJson(error_result);
			return;
		} else {
			relpercen = new Bill().computepercen(this, r.getInt("id"),
					Double.valueOf(getPara("count")));

			Integer upper = r.get("upper");
			Record rd = getRank(r.getInt("id"));
			boolean dirpercen = true;
			double bman_percen = rd.getDouble("percen");
			List<Record> uppers = new ArrayList<Record>();
			Record pinfo = new Record();
			pinfo.set("name", r.getStr("name"));
			pinfo.set("rank", rd.get("rank"));
			pinfo.set("percen", relpercen);
			pinfo.set("bman_id", r.getInt("id"));
			pinfo.set("percentype", "个人签单提成");

			uppers.add(pinfo);

			// percentype_
			if (Double.valueOf(getPara("overcount")) != 0) {
				Record opinfo = new Record();
				opinfo.set("name", r.getStr("name"));
				opinfo.set("rank", rd.get("rank"));
				opinfo.set("percen", Double.valueOf(getPara("overcount"))
						* getconf("overcount"));
				opinfo.set("bman_id", r.getInt("id"));
				opinfo.set("percentype", "个人加价提成");
				uppers.add(opinfo);
			}
			double notdirectpercen = 0;
			while (upper != null) {
				Record upr = Db.findFirst("select * from mt_bman where id=?",
						upper);
				Record old_rd = rd;
				rd = getRank(upper);
				Record upinfo = new Record();

				if (old_rd.getLong("rank") >= rd.getLong("rank") && dirpercen) {
					rd.set("percen", getconf("eq"));
					notdirectpercen = getconf("eq") / 2;
					upinfo.set("percentype", "分部签单提成");
					dirpercen = false;
				} else {
					if (dirpercen) {
						notdirectpercen = (rd.getDouble("percen") - bman_percen) / 2;
						rd.set("percen", notdirectpercen * 2);
						upinfo.set("percentype", "分部签单提成");
						dirpercen = false;
					} else {
						rd.set("percen", notdirectpercen);
						upinfo.set("percentype", "分部拓展提成");

					}
				}
				long rank = rd.get("rank");
				upinfo.set("name", upr.getStr("name"));
				upinfo.set("rank", rank);
				upinfo.set("percen", rd.get("percen"));
				upinfo.set("bman_id", upper);
				uppers.add(upinfo);
				upper = upr.get("upper");
			}
			r.set("uppers", uppers);

			renderJson(r);
		}
	}

	public void addbman() {
		String rc = Bman.getrandomcode(10);
		while (true) {
			Record rcis = Db.findFirst(
					"select enter_code from mt_bman where enter_code =?", rc);
			if (null == rcis) {
				break;
			} else {
				rc = Bman.getrandomcode(10);
			}
		}
		if (getParaToInt("upper_id") != null) {
			Db.update(
					"insert into mt_bman(name,phone,upper,account_type,account_num,enter_code) values(?,?,?,?,?,?)",
					getPara("name"), getPara("phone"), getPara("upper_id"),
					getPara("account_type"), getPara("account_num"), rc);
		} else {
			Db.update(
					"insert into mt_bman(name,phone,account_type,account_num,enter_code) values(?,?,?,?,?)",
					getPara("name"), getPara("phone"), getPara("account_type"),
					getPara("account_num"), rc);
		}
		Record insert_id = Db.findFirst(
				"select id from mt_bman where enter_code=?", rc);
		redirect("/admin/bmaninfo?bman_id=" + insert_id.get("id"));
	}

	public void adminmanage() {
		getMutiPage(this, "select * from mt_admin", "members");
		render("adminmanage.html");
	}

	public void addadmin() {
		int t1 = 0;
		try {
			t1 = Db.update(
					"insert into mt_admin(admin_user,admin_pwd) values(?,password(?)) ",
					getPara("admin_user"), getPara("admin_pwd"));
		} catch (Exception e) {
			setAttr("errorMsg", "帐号已存在");

		}
		if (t1 == 1) {
			adminmanage();
			return;
		} else {
			setAttr("errorMsg", "帐号已存在");
			adminmanage();
		}
	}

	public void changeadminpwd() {
		Record r = (Record) getSession().getAttribute("user");
		int result = Db
				.update("update mt_admin set admin_pwd = password(?) where admin_user = ? and admin_pwd = password(?)",
						getPara("newpwd"), r.getStr("admin_user"),
						getPara("oldpwd"));
		if (result == 1) {
			setAttr("successMsg", "修改成功");
			adminmanage();
		} else {
			setAttr("errorMsg", "密码错误");
			adminmanage();
		}
	}

	public void deladmin() {
		Record r = Db.findFirst("select count(*) cadmin from mt_admin ");
		if (r != null && r.getLong("cadmin") == 1) {
			setAttr("errorMsg", "账户不能少于一个");
			adminmanage();

		} else {
			Db.update("delete from mt_admin where admin_user = ? ",
					getPara("admin_user"));
		}
		adminmanage();

	}

	public void changepercenrule() {
		if (getPara("bcount") != null) {
			Db.update("delete from mt_rank");
			Db.update("delete from mt_pscope");
			Db.update("delete from mt_conf");
			String[] bcounts = getPara("bcount").split(",");
			String[] ranks = getPara("rank").split(",");
			String[] percens = getPara("percen").split(",");
			String[] upcount = getPara("upcount").split(",");
			String[] uprate = getPara("uprate").split(",");
			String lt2 = getPara("lt2");
			String eq = getPara("eq");
			String bottom = getPara("bottom");
			String top = getPara("top");
			Db.update("insert into mt_conf(var_name,value) values(?,?)", "lt2",
					lt2);
			Db.update("insert into mt_conf(var_name,value) values(?,?)", "eq",
					eq);
			Db.update("insert into mt_conf(var_name,value) values(?,?)",
					"bottom", bottom);
			Db.update("insert into mt_conf(var_name,value) values(?,?)", "top",
					top);

			for (int i = 0; i < percens.length; i++) {
				Db.update(
						"insert into mt_rank(bill_count,rank,percen) values(?,?,?)",
						bcounts[i], ranks[i], percens[i]);
			}
			for (int i = 0; i < upcount.length; i++) {
				Db.update("insert into mt_pscope(count,rate) values(?,?)",
						upcount[i], uprate[i]);
			}
			redirect("/admin/changepercenrule");
		}
		// 基准规则
		List<Record> rs = Db.find("select * from mt_rank order by rank desc");
		String bcount = "";
		String rank = "";
		String percen = "";
		for (Record r : rs) {
			bcount += r.get("bill_count") + ",";
			rank += r.get("rank") + ",";
			percen += r.get("percen") + ",";
		}
		bcount = bcount.substring(0, bcount.length() - 1);
		rank = rank.substring(0, rank.length() - 1);
		percen = percen.substring(0, percen.length() - 1);
		setAttr("bcount", bcount);
		setAttr("rank", rank);
		setAttr("percen", percen);

		// 大于基准的规则
		List<Record> rsover = Db
				.find("select * from mt_pscope order by count  asc");
		String upcount = "";
		String uprate = "";

		for (Record r : rsover) {
			upcount += r.get("count") + ",";
			uprate += r.get("rate") + ",";
		}

		upcount = upcount.substring(0, upcount.length() - 1);
		uprate = uprate.substring(0, uprate.length() - 1);
		setAttr("upcount", upcount);
		setAttr("uprate", uprate);

		List<Record> confs = Db.find("select * from mt_conf");
		for (Record r : confs) {
			setAttr(r.getStr("var_name"), r.getStr("value"));
		}
	}

	public void payallpercen() {
		Db.update("update mt_record set ispay=1 where bill_id = ? and ispay=0",
				getPara("bill_id"));
		Db.update("update mt_bill set ispay=1 where id = ? and ispay=0",
				getPara("bill_id"));
		redirect("/admin/bills");
	}

	public void addnews() {
		String news_id = getPara("news_id");
		String content = getPara("content");
		String title = getPara("title");
		String createid = System.currentTimeMillis() + "";

		if (news_id == null) {
			Db.update("insert into mt_news(id,title,content) values(?,?,?)",
					createid, title, content);
		} else {
			Db.update(
					"update mt_news set title = ? ,content = ? where id = ? ",
					title, content, news_id);
		}

		renderHtml("<html><head></head><body><script>window.parent.location.href='/weixin/admin/newsmanage'</script></body></html>");
	}

	public void getnews() {

		renderJson(Db.findFirst("select * from mt_news where id = ? ",
				getPara("news_id")));
	}

	public void delnews() {
		renderJson(Db.update("delete from mt_news where id = ? ",
				getPara("news_id")));
		redirect("/admin/newsmanage");
	}

	public void newsmanage() {
		String title = getPara("title") == null ? "" : getPara("title");
		getMutiPage(this,
				"select id,title,date from mt_news where sent_to is  null and title like '%"
						+ title + "%' order by date desc ", "members");
		render("newsmanage.html");

	}

	public void news() {
		Record r = Db.findFirst("select * from mt_news where id = ? ",
				getPara("news_id"));
		setAttr("title", r.getStr("title"));
		setAttr("content", r.getStr("content"));
		render("news.html");
	}

	public void addnewspage() {
	}

	public void updatenewspage() {
		setAttr("news_id", getPara("news_id"));
	}

	public void showrecorddetails() {
		String table_name = getPara("table_name");
		String id = getPara("id");
		String sql = "select * from " + table_name + " where id = ?";
		Record r = Db.findFirst(sql, id);
		setAttr("details", r.toJson());
		setAttr("id", id);
		setAttr("page", getPara("page"));
		setAttr("page_name", getPara("page_name"));

		render("showrecorddetails.html");
	}
}
