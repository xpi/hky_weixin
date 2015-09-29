package com.hukouyi.weixin.model;

import java.util.List;

import com.hukouyi.weixin.controller.AdminPage;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class Bill {
	//个人签单
	
	public  long getPersonalBill(long bman_id){
		long bcount = Db.findFirst("select count(*) bcount from mt_record where sbman_id = ? ",bman_id).getLong("bcount");
		return bcount;
	}
	
	public  long getBottomBill(long bman_id){
		long bcount = Db.findFirst("select count(*) bcount from mt_record where sbman_id != ? and bman_id = ? ",bman_id,bman_id).getLong("bcount");
		return bcount;
	}
	
	public double computepercen(AdminPage ap ,int bman_id,double count) {
		long rank = ap.getRank(bman_id).getLong("rank");
		String sql = " select * from mt_rank  where rank >=? order by rank asc limit 0,1";
		Record percen = Db.findFirst(sql, rank);
		double lt_rate = Double.valueOf(ap.getconf("lt2"));
		double bottom = Double.valueOf(ap.getconf("bottom"));
		double top = Double.valueOf(ap.getconf("top"));
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
		return percen.get("percen");
	}
	
}
