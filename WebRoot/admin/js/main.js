function bindJsonToDom(containerid, domid, jsonarray, func) {
	$.each(jsonarray, function(i, obj) {
		var row = $($(domid).clone()[0]); // 克隆模板，创建一个新数据行

		for (attribute in obj) {
			if (func) {
				func(attribute, obj,row);
			}
			row.find("." + attribute).html(obj[attribute]); // 循环json对象的属性，并赋值到数据行中对应的列，此处列的id就是相应的属性名称
		}
		
		row.appendTo($(containerid));
	});
	$($(domid)[0]).remove();
}
var reconfirm=function(){
	
	var isdel = confirm("确认删除");

	if (isdel) {
		return true;
	}else{
		return false;
	}
}
