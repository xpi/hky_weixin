package com.hukouyi.weixin.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

public class Bman extends Model<Bman> {
	public static final Bman dao = new Bman();

	public static boolean bindWeixin(String phone, String openid) {
		Bman bman = Bman.dao.findFirst("select * from mt_bman where phone = ?",
				phone);
		if (bman == null) {
			return false;
		} else {
			return bman.set("open_id", openid).update();
		}
	}

	public static Record isValidUser(String openid) {
		Record bman = Db.findFirst("select * from mt_bman where open_id = ?",
				openid);
		return bman;

	}

	public static Record getBmanInfo(int bman_id) {
		String sql = "select t6.*,t7.phone as sphone,t7.name sbman_name from(select * from (select t4.*,count(t5.name) mcount from "
				+ "(select t1.*, t2.count as bcount,max(t3.rank) rank from mt_bman t1 "
				+ "left join (select bman_id,count(*) count from mt_record m1 GROUP BY bman_id) t2 on t2.bman_id=t1.id "
				+ "left join mt_rank t3 on t2.count >= t3.bill_count "
				+ "group by t1.id ) t4 "
				+ "left join mt_bman t5 on t5.upper = t4.id "
				+ "group by t4.id) t6 where id = ?) t6"
				+ " left join mt_bman t7 on t7.id = t6.upper";

		return Db.findFirst(sql, bman_id);
	}

	public static Record getRank(int bman_id) {
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

	public static String getrandomcode(int num) {
		String name = "";
		String[] al = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9"
				.split(",");

		for (int i = 0; i < num; i++) {
			int intValue = (int) (Math.random() * 36);
			name = name + al[intValue];
		}
		return name;
	}

	// 部门所有人的id
	public static ArrayList<Integer> getallbottom(ArrayList<Integer> bman_ids,
			ArrayList<Integer> bottom_ids) {
		if (bman_ids == null) {
			return null;
		}

		for (Integer id : bman_ids) {
			bottom_ids.add(id);
			List<Record> rs = Db.find(
					"select name,id,upper from mt_bman where upper = ?", id);
			ArrayList<Integer> bottoms = new ArrayList<Integer>();
			for (Record r : rs) {
				bottoms.add(r.getInt("id"));
			}
			getallbottom(bottoms, bottom_ids);
		}

		return bottom_ids;
	}

	// 拓展分部id
	public static ArrayList<Integer> getallnextbottom(
			ArrayList<Integer> bman_ids, ArrayList<Integer> bottom_ids) {
		ArrayList<Integer> exclude = (ArrayList<Integer>) bman_ids.clone();
		String uppers = JsonKit.toJson(bottom_ids).replace("[", "(")
				.replace("]", ")");
		List<Record> alldir = Db
				.find("select id from mt_record where upper in " + uppers);
		for (Record r : alldir) {
			exclude.add(r.getInt("id"));
		}

		getallbottom(bman_ids, bottom_ids);
		for (Integer id : exclude) {
			bottom_ids.remove(id);
		}
		return bottom_ids;
	}

	// 获取直接分部
	public static List<Record> dirbottoms(int upper) {
		List<Record> dbs = Db.find(
				"select name,id,upper from mt_bman where upper = ?", upper);
		return dbs;
	}

	// 获取直接分部id
	public static List<Integer> dirbottomsid(int upper) {
		List<Record> dbs = dirbottoms(upper);
		ArrayList<Integer> dirbottoms_id = new ArrayList<Integer>();
		for (Record dbsr : dbs) {
			dirbottoms_id.add(dbsr.getInt("id"));
		}
		return dirbottoms_id;
	}

	// 直接分部
	public static long getdirpercencount(int upper) {
		long count = 0;
		List<Integer> alldir = dirbottomsid(upper);

		for (Integer id : alldir) {
			Record countr = Db
					.findFirst(
							"select count(percen) as countpercen from mt_record where bman_id = ? and sbman_id = ?",
							upper, id);
			if (countr != null) {
				count += countr.getLong("countpercen");
			}
		}

		return count;
	}

	// 直接分部
	public static double getdirpercensum(int upper) {
		double count = 0;
		List<Integer> alldir = dirbottomsid(upper);
		for (Integer id : alldir) {
			Record countr = Db
					.findFirst(
							"select sum(percen) as countpercen from mt_record where bman_id = ? and sbman_id = ?",
							upper, id);
			if (countr != null) {
				count += countr.get("countpercen") == null ? 0 : countr
						.getDouble("countpercen");
			}
		}

		return count;
	}

	// 拓展
	public static double getextpercensum(int upper) {
		double count = 0;
		List<Integer> alldir = dirbottomsid(upper);

		String alldirids = JsonKit.toJson(alldir).replace("[", "(")
				.replace("]", ")");
		System.out.println(alldirids);
		Record r = Db
				.findFirst(
						"select sum(percen) as extpercensum from mt_record where bman_id = ? and sbman_id != ? and sbman_id not in "
								+ alldirids, upper, upper);
		count = r.get("extpercensum") == null ? 0 : r.getDouble("extpercensum");
		return count;
	}

	// 拓展
	public static long getextpercencount(int upper) {
		long count = 0;
		List<Integer> alldir = dirbottomsid(upper);

		String alldirids = JsonKit.toJson(alldir).replace("[", "(")
				.replace("]", ")");
		Record r = Db
				.findFirst(
						"select count(percen) as extpercencount from mt_record where bman_id = ? and sbman_id != ? and sbman_id not in "
								+ alldirids, upper, upper);

		count = r.get("extpercencount") == null ? 0 : r
				.getLong("extpercencount");
		return count;
	}

	// 个人所有提成总额
	public static double getallpercensum(int upper) {
		double count = 0;
		Record countr = Db
				.findFirst(
						"select sum(percen) as countpercen from mt_record where bman_id = ?  ",
						upper);
		if (countr != null) {
			count = countr.get("countpercen") == null ? 0 : countr
					.getDouble("countpercen");
		}
		return count;
	}

	// 个人所有提成总数
	public static long getallpercencount(int upper) {
		long count = 0;

		Record countr = Db
				.findFirst(
						"select count(percen) as countpercen from mt_record where bman_id = ? ",
						upper);
		if (countr != null) {
			count = countr.get("countpercen") == null ? 0 : countr
					.getLong("countpercen");
		}
		return count;
	}

	// 个人加价提成总额
	public static double getoverpercensum(int upper) {
		double count = 0;
		Record countr = Db
				.findFirst(
						"select sum(percen) as countpercen from mt_record where bman_id = ? and sbman_id = ? and percen_type like '%加价%' ",
						upper, upper);
		if (countr != null) {
			count = countr.get("countpercen") == null ? 0 : countr
					.getDouble("countpercen");
		}
		return count;
	}

	// 个人加价提成总数
	public static long getoverpercencount(int upper) {
		long count = 0;

		Record countr = Db
				.findFirst(
						"select count(percen) as countpercen from mt_record where bman_id = ? and sbman_id = ?  and percen_type  like '%加价%' ",
						upper, upper);
		if (countr != null) {
			count = countr.get("countpercen") == null ? 0 : countr
					.getLong("countpercen");
		}
		return count;
	}

	// 个人签单提成总数
	public static long getpersonalpercencount(int bman_id) {
		return Db
				.findFirst(
						"select count(*) percencount  from mt_record where bman_id = ? and sbman_id = ? and percen_type not like '%加价%'",
						bman_id, bman_id).getLong("percencount");
	}

	// 个人签单提成总额
	public static double getpersonalpercensum(int bman_id) {
		return Db
				.findFirst(
						"select sum(percen) percensum  from mt_record where bman_id = ? and sbman_id = ? and percen_type not like '%加价%'",
						bman_id, bman_id).getDouble("percensum");
	}

	// 个人签单提成列表
	public static String listpersonalpercencount(int upper, String year_month) {
		String[] yearmonth = year_month.replace("-0", "-").split("-");
		List<Record> countr = Db
				.find("select m1.bill_id,m1.id,m1.percen,m1.sign_date,m1.percen_type,m1.ispay,m2.name from mt_record m1 "
						+ "left join mt_bman m2 on m2.id = m1.sbman_id "
						+ "where m1.bman_id = ? and m1.sbman_id=? and m1.percen_type not like '%加价%' "
						+ "and year(m1.sign_date)=? and month(m1.sign_date)=? order by m1.sign_date desc",
						upper, upper, yearmonth[0], yearmonth[1]);
		return JsonKit.toJson(countr);
	}

	// 个人加价提成列表
	public static String listoverpercencount(int upper, String year_month) {
		String[] yearmonth = year_month.replace("-0", "-").split("-");
		List<Record> countr = Db
				.find("select m1.bill_id,m1.id,m1.percen,m1.sign_date,m1.percen_type,m1.ispay,m2.name from mt_record m1 "
						+ "left join mt_bman m2 on m2.id = m1.sbman_id "
						+ "where m1.bman_id = ? and m1.sbman_id=? and m1.percen_type  like '%加价%' "
						+ "and year(m1.sign_date)=? and month(m1.sign_date)=? order by m1.sign_date desc",
						upper, upper, yearmonth[0], yearmonth[1]);
		return JsonKit.toJson(countr);
	}

	// 部门+个人提成列表
	public static String listallpercencount(int upper, String year_month) {
		String[] yearmonth = year_month.replace("-0", "-").split("-");
		List<Record> countr = Db
				.find("select m1.bill_id,m1.id,m1.percen,m1.sign_date,m1.percen_type,m1.ispay,m2.name from mt_record m1 "
						+ "left join mt_bman m2 on m2.id = m1.sbman_id "
						+ "where m1.bman_id = ? "
						+ "and year(m1.sign_date)=? and month(m1.sign_date)=? order by m1.sign_date desc",
						upper, yearmonth[0], yearmonth[1]);
		return JsonKit.toJson(countr);
	}

	// 分部提成列表
	public static String listdirpercencount(int upper, String year_month) {
		String[] yearmonth = year_month.replace("-0", "-").split("-");
		List<Integer> alldir = dirbottomsid(upper);
		String alldirids = JsonKit.toJson(alldir).replace("[", "(")
				.replace("]", ")");
		List<Record> countr = Db
				.find("select m1.bill_id,m1.id,m1.percen,m1.sign_date,m1.percen_type,m1.ispay,m2.name from mt_record m1 "
						+ "left join mt_bman m2 on m2.id = m1.sbman_id "
						+ "where m1.bman_id = ? and m1.sbman_id in "
						+ alldirids
						+ "  and m1.percen_type not like '%加价%' "
						+ "and year(m1.sign_date)=? and month(m1.sign_date)=? order by m1.sign_date desc",
						upper, yearmonth[0], yearmonth[1]);
		return JsonKit.toJson(countr);
	}

	// 拓展提成列表
	public static String listextpercencount(int upper, String year_month) {
		String[] yearmonth = year_month.replace("-0", "-").split("-");
		List<Integer> alldir = dirbottomsid(upper);
		String alldirids = JsonKit.toJson(alldir).replace("[", "(")
				.replace("]", ")");
		List<Record> countr = Db
				.find("select m1.bill_id,m1.id,m1.percen,m1.sign_date,m1.percen_type,m1.ispay,m2.name from mt_record m1 "
						+ "left join mt_bman m2 on m2.id = m1.sbman_id "
						+ "where m1.bman_id = ? and sbman_id != ? and sbman_id not in"
						+ alldirids
						+ "  and m1.percen_type not like '%加价%' "
						+ "and year(m1.sign_date)=? and month(m1.sign_date)=? order by m1.sign_date desc",
						upper, upper, yearmonth[0], yearmonth[1]);
		return JsonKit.toJson(countr);
	}

	public static void main(String[] args) {
	}
}
