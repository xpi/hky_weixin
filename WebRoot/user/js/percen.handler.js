var selecttype = function(page,year_month,type){
			$(page).find(".app-content").showLoading();
			$.ajax(
			{
			dataType:"json",
			url:"bman/listpercen",
			data:{year_month:year_month,type:type,bman_id:"%{bman_id}"},
			success:function(data){	
				$(".appendli").remove();
				for(var k in data)
				{	
					 var obj = data[k];
					  		
					if(data[k].ispay=="未付款"){
						$(page).find(".c"+data[k].crange).before('<li class="appendli unpaycolor">顾客姓名：'+obj["client_name"]+'</li>');	
					}else{
						$(page).find(".c"+data[k].crange).before('<li class="appendli paycolor">顾客姓名：'+obj["client_name"]+'</li>');	
					}
					$(page).find(".c"+data[k].crange).before('<li class="appendli">提成金额：'+obj["percen"]+'元</li>');	
					$(page).find(".c"+data[k].crange).before('<li class="appendli">签单日期：'+obj["sign_date"]+'</li>');	
					$(page).find(".c"+data[k].crange).before('<li class="appendli">支付日期：'+obj["pay_date"]+'</li>');	
					$(page).find(".c"+data[k].crange).before('<li class="appendli">支付状态：'+obj["ispay"]+'</li>');	
					
				}
				$(page).find('.app-content').hideLoading();
			 },
			error:function(err){
				$(page).find('.app-content').hideLoading();
			},
				timeout:10000
			})
		}
		App.controller('percen-page',function(page){
			var cbtype="sbman_id";
			var year_month = $(page).find('#from-date').val();
			selecttype(page,year_month,"sbman_id");
			$(page).find("#mypercen").click(function(){
				year_month = $(page).find('#from-date').val();
				
				selecttype(page,year_month,"sbman_id");
				cbtype="sbman_id";
			})
			$(page).find("#bpercen").click(function(){
			    year_month = $(page).find('#from-date').val();
				selecttype(page,year_month,"bman_id");
				
				var cbtype="bman_id";
			})
			$(page).find("#bbpercen").click(function(){
			    year_month = $(page).find('#from-date').val();
				selecttype(page,year_month,"bman__id");
				var cbtype="bman_id";
				
			})
			
			var i =$(page).find('#from-date').mdater({
				minDate : new Date(2015, 2, 10),
				outercontainer:$(page).find(".app-content"),okcallback:function(){
					year_month = $(page).find('#from-date').val();
					selecttype(page,year_month,cbtype);
				
				}
				
				});
			
		});