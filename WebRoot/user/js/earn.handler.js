	var listdetails = function(bman_id,year_month,rangetype){
		$('.app-content').showLoading();
		$.ajax({
			dataType:"json",
			url:"bman/earnnames",
			data:{bman_id:bman_id,year_month:year_month,rangetype:rangetype},
			success:function(data){
				$("#nameslist li").remove();
				for(i in data){
					$("#nameslist").append("<li class='app-button'>"+data[i].name+"</li>");
				}
				$('.app-content').hideLoading();
			},
			error:function(err){
				$('.app-content').hideLoading();
				
			}
			
		});
		
		
	}

	var changeDate = function(target,tit_temp,row_temp,page,year_month,type,tit){
		$(page).find(".app-content").showLoading();
					$.ajax({
						dataType:"json",
						url:"bman/teamearn",
						data:{year_month:year_month,bman_id:${bman_id},type:type},
						success: function(result) {
					       tit_temp=tit_temp.clone();
					       row_temp=row_temp.clone();
					      	$(tit_temp).text(tit)
					      	for(k in result){
					        	$(row_temp).find(".r"+result[k].crange).html(result[k].bill_count+"单");
					        	console.log(result[k].crange);
					        	if(type=="sbman_id"&&parseInt(result[k].bill_count)!=0){
					        		$(row_temp).find(".t"+result[k].crange).append("<a class='app-button' onclick=\"listdetails(${bman_id},'"+year_month+"',"+result[k].crange+")\" >查看分店<a>")
		
						      	}
					      	}
					        $(tit_temp).text(tit)
					        $(page).find(target).append(tit_temp);
					        $(page).find(target).append(row_temp);
					        $(page).find('.app-content').hideLoading();
					    },
					    error: function(httpException) {
							 $('.app-content').hideLoading();
							 showerror(showerror);
					    },
					    timeout: 10000
					})
					
		}
		
		
		App.controller('achiv-page', function(page) {
			var year_month = $(page).find('#from-date').val();
			var tit_temp = $($(page).find(".tit")[0]).clone();
	        var row_temp = $($(page).find(".box")[0]).clone(); 
	        $(page).find(".tit").remove();
	        $(page).find(".box").remove();
	        $(page).find(".splilt").html("");
	        console.log( $(page).find(".splilt"));
			changeDate("#all_bill",tit_temp,row_temp,page,year_month,"bman_id","总业绩");
			changeDate("#my_bill",tit_temp,row_temp,page,year_month,null,"个人业绩");
			changeDate("#other_bill",tit_temp,row_temp,page,year_month,"sbman_id","分部业绩");
			// 日期选择事件
			var i =$(page).find('#from-date').mdater({
				minDate : new Date(2015, 2, 10),
				outercontainer:$(page).find(".app-content"),
				okcallback:function(){
					var year_month = $(page).find('#from-date').val();
					var tit_temp = $($(page).find(".tit")[0]).clone();
			        var row_temp = $($(page).find(".box")[0]).clone(); 
			        $(page).find(".tit").remove();
			        $(page).find(".box").remove();
			        $(page).find(".splilt").html("");
			        console.log( $(page).find(".splilt"));
					changeDate("#all_bill",tit_temp,row_temp,page,year_month,"bman_id","总业绩");
					changeDate("#my_bill",tit_temp,row_temp,page,year_month,null,"个人业绩");
					changeDate("#other_bill",tit_temp,row_temp,page,year_month,"sbman_id","分部业绩");
				}
			});
			// getbillcount
			
			$(page).find('#getbillcount').on("click",function(){
				
			})
			
		});