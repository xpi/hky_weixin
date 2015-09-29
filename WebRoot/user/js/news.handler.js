var next=1;
		var morenews = function(obj,listtype){
			$('.app-content').showLoading();
			$.ajax({
				dataType:"json",
				url:"bman/"+listtype,
				data:{next:next},
				success:function(data){
					if(data.length==0){
					$("#morenews").remove();
					
					}
					for(var i in data){
						$(obj).before("<li><a class='app-button' onclick='newsdetails("+data[i].id+")'>"+data[i].title+"</a></li>");
					}
					
					next = parseInt($("#morenews").attr("next"));
					$("#morenews").attr("next",next+1);
					$('.app-content').hideLoading();
				}
			});
		};
		
		var newsdetails = function(news_id){
		   $('.app-content').showLoading();
		   $.ajax({
				dataType:"json",
				url:"bman/getnews",
				data:{news_id:news_id},
				success:function(data){
					App.load("newsdetails-page",data)
					$('.app-content').hideLoading();
				}
			});
		}
		
		var initnews = function(page,next,listtype){
			$(page).find('.app-content').showLoading();
			$.ajax({
				dataType:"json",
				url:"bman/"+listtype,
				data:{next:next},
				success:function(data){
					for(var i in data){
						$(page).find("#morenews").before("<li><a class='app-button' onclick='newsdetails("+data[i].id+")'>"+data[i].title+"</a></li>");
					}
					next = parseInt($(page).find("#morenews").attr("next"));
					$(page).find("#morenews").attr("next",next+1);
					$(page).find('.app-content').hideLoading();
				}
			});
		}
		
		App.controller('news-page',function(page){
			next=1;	
			initnews(page,0,"listnews");
			
		})		
		App.controller('message-page',function(page){
			next=1;
			initnews(page,0,"listmessage");
			
		})
		
		
		App.controller('newsdetails-page',function(page,args){
			$(page).find(".newsdate").before(args.title);
			$(page).find(".newsdate").append("<br>"+args.date);
			$(page).find(".content").append(args.content);
			
		});
		
		App.controller('newsdetailsfixed-page',function(page,args){
		   $(page).find('.app-content').showLoading();
		   $.ajax({
				dataType:"json",
				url:"bman/getnews",
				data:{news_id:'1438578783579'},
				success:function(data){
					$(page).find(".newsdate").before(data.title);
					$(page).find(".newsdate").append("<br>"+data.date);
					$(page).find(".content").append(data.content);
					$(page).find('.app-content').hideLoading();
				}
			
			});
		});