package com.hukouyi.weixin.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class BaseInfoAppend implements Interceptor {

	public void intercept(ActionInvocation ai) {
		Controller c = ai.getController();

		c.setAttr("unpayPercenCount", getUnPayPercenCount());
		c.setAttr("unpayBillCount", getUnPayBillCount());
		
		ai.invoke();
	}
	
	public long  getUnPayPercenCount(){
		Record r = Db.findFirst("select count(*) count from mt_record where ispay=0");
		
		
		return r.getLong("count");
	}
	public long getUnPayBillCount(){
		Record r = Db.findFirst("select count(*) count from mt_bill where ispay=0");
		return r.getLong("count");
	}
	
}
