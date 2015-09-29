package com.hukouyi.weixin.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ChartsPage extends Controller{
	public void index(){
	
		
		String y_sql = "select t1.tmonth,sum(t1.count) sumcount from "
				+ "(select DATE_FORMAT(sign_date,'%m') tmonth ,id,count,DATE_FORMAT(sign_date,'%Y') tyear "
				+ "from mt_bill where DATE_FORMAT(sign_date,'%Y') = DATE_FORMAT(CURRENT_TIMESTAMP,'%Y') group by sign_date)t1 "
				+ "group by t1.tmonth ";
		List<Record> tyear = Db.find(y_sql);

		double[] month_count = new double[12];

		for (Record r : tyear) {
			Integer mth = Integer.valueOf(r.getStr("tmonth")) - 1;
			month_count[mth] = r.getDouble("sumcount");
		}
		String mon = "[";

		for (int i = 0; i < month_count.length; i++) {
			mon += month_count[i] + ",";
		}
		mon = mon.substring(0, mon.length() - 1);

		mon += "]";
		setAttr("tyear", mon);	
		setAttr("cyear", new Date().getYear());	
	
	}	
	public void personpercen(){
		String bman_id = getPara("bman_id");
		Record r = Db.findFirst("select * from mt_bman where id = ?",bman_id);
		setAttr("bman_name", r.get("name"));
		String sql ="select tmonth,if(unpaypercen is null,0,unpaypercen) unpaypercen,\n" +
				"if(paypercen is null,0,paypercen) paypercen\n" +
				"from(\n" +
				"select t1.tmonth,t1.unpaypercen,t2.paypercen from\n" +
				"(select DATE_FORMAT(sign_date,'%m') as tmonth ,\n" +
				"id,sum(percen) as unpaypercen,DATE_FORMAT(sign_date,'%Y') as  tyear \n" +
				"from mt_record where bman_id = ? and ispay=0 group by tmonth)t1 \n" +
				"left join\n" +
				"(select DATE_FORMAT(sign_date,'%m') as tmonth ,\n" +
				"id,sum(percen) as paypercen,DATE_FORMAT(sign_date,'%Y') as  tyear \n" +
				"from mt_record where bman_id = ? and ispay=1 group by tmonth)t2\n" +
				"on t2.tmonth=t1.tmonth\n" +
				")t3";
		List<Record> data = Db.find(sql,bman_id,bman_id);
		
		setAttr("data", JsonKit.toJson(data));
		
		
	}
	
	public void allpercen(){
	
		String sql = "select tmonth,if(unpaypercen is null,0,unpaypercen) unpaypercen,\n" +
				"if(paypercen is null,0,paypercen) paypercen\n" +
				"from(\n" +
				"select t1.tmonth,t1.unpaypercen,t2.paypercen from\n" +
				"(select DATE_FORMAT(sign_date,'%m') as tmonth ,\n" +
				"id,sum(percen) as unpaypercen,DATE_FORMAT(sign_date,'%Y') as  tyear \n" +
				"from mt_record where ispay=0 group by tmonth)t1 \n" +
				"left join\n" +
				"(select DATE_FORMAT(sign_date,'%m') as tmonth ,\n" +
				"id,sum(percen) as paypercen,DATE_FORMAT(sign_date,'%Y') as  tyear \n" +
				"from mt_record where ispay=1 group by tmonth)t2\n" +
				"on t2.tmonth=t1.tmonth\n" +
				")t3";
		List<Record> data = Db.find(sql);
		
		setAttr("data", JsonKit.toJson(data));
		
		//总提成饼图
		Double unpay = Db.findFirst(
				"select sum(percen) unpay from mt_record where ispay = 0 ")
				.getDouble("unpay");
		unpay = unpay == null ? 0 : unpay;
		
		Double ispay = Db.findFirst(
				"select sum(percen) ispay from mt_record where ispay = 1 ")
				.getDouble("ispay");
		

		double[] month_count = new double[12];

		setAttr("cyear", new SimpleDateFormat("yyyy").format(new Date()));	
		setAttr("unpay", unpay);
		setAttr("ispay", ispay);
		
	}
	
	public void personbill(){
		
	}
	
	public void department(){
		Integer bman_id = getParaToInt("bman_id");
		ArrayList<Object> bottoms = new ArrayList<Object>();
		bottoms.add(bman_id);
		
		ArrayList<Integer> bottom_ids = new ArrayList<Integer>();
		getbottom(bottoms.toArray(),bottom_ids);
		String bottom_idsjson=JsonKit.toJson(bottom_ids).replace("[", "(").replace("]", ")");
		System.out.println(bottom_idsjson);
		List<Record> percens1= Db.find("select tmonth,if(unpaypercen is null,0,unpaypercen) unpaypercen,\n" +
				"if(paypercen is null,0,paypercen) paypercen\n" +
				"from(\n" +
				"select t1.tmonth,t1.unpaypercen,t2.paypercen from\n" +
				"(select DATE_FORMAT(sign_date,'%m') as tmonth ,\n" +
				"id,sum(percen) as unpaypercen,DATE_FORMAT(sign_date,'%Y') as  tyear \n" +
				"from mt_record where bman_id in "+bottom_idsjson+" and  ispay=0 group by tmonth)t1 \n" +
				"left join\n" +
				"(select DATE_FORMAT(sign_date,'%m') as tmonth ,\n" +
				"id,sum(percen) as paypercen,DATE_FORMAT(sign_date,'%Y') as  tyear \n" +
				"from mt_record where bman_id in "+bottom_idsjson+" and   ispay=1 group by tmonth)t2\n" +
				"on t2.tmonth=t1.tmonth\n" +
				")t3");
		setAttr("depart", Db.find("select name from mt_bman where id = ?",bman_id).get(0).get("name"));
		setAttr("cyear", new SimpleDateFormat("yyyy").format(new Date()));	
		setAttr("data",JsonKit.toJson(percens1));
	}
	
	
	
	public Object[] getbottom(Object[] bman_ids,ArrayList<Integer> bottom_ids){
		if(bman_ids==null){
			return null;
		}
		for (int i = 0; i < bman_ids.length; i++) {
			Integer id = (Integer)bman_ids[i];
			bottom_ids.add(id);
			List<Record> rs= Db.find("select name,id,upper from mt_bman where upper = ?",id);
			ArrayList<Object> bottoms = new ArrayList<Object>();
			for (Record r : rs) {
				bottoms.add(r.getInt("id"));
			}
			getbottom(bottoms.toArray(),bottom_ids);
		}
		
		return null;
	}
	
}
