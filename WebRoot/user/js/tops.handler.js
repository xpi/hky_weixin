var selecttop =function(page,type,year){
		$(page).find('.app-content').showLoading();
			$(".apl").remove();
			$.ajax({
					dataType:"json",
					url:"bman/"+type,
					data:{year:year},
					success:function(data){
						console.log(data);
						var toporder = {};
						for(var k in data){
							var ms = "m"+data[k].mon;
							if(toporder[ms]){
								toporder[ms]+=1;
							}else{
								toporder[ms]=1;
							}
							$(page).find(".m"+data[k].mon).before("<li class='apl'>Top-"+(toporder[ms])+":"+data[k].name+"("+data[k].citem+")</li>");
						}
						$(page).find('.app-content').hideLoading();
					},
					error:function(err){
						console.log(err);
						$(page).find('.app-content').hideLoading();
					}
					
				});
		}
		
		App.controller('tops-page',function(page){
			for(var i = 1 ;i<=12;i++){
				$(page).find("#toplist").append("<label>"+i+"月</label>");
				$(page).find("#toplist").append("<li class='m"+i+"' hidden></li>");
			}
			var curyear = '${current_y}';
			selecttop(page,"percentop",curyear);
			$(page).find("#percentop").on("click",function(){
				selecttop(page,"percentop",curyear);
			})
			$(page).find("#billtop").on("click",function(){
				selecttop(page,"billtop",curyear);
			})
		
		});