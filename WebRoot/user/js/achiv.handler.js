App.controller('teamspage', function(page) {
			var $template = $(page).find('.bmanitems').remove();
			
			$.getJSON('teams', function(data) {
				for ( var i = 0; i < data.length; i++) {
					$template = $template.clone(false);
					$template.find(".bname").text(data[i].name);
					$template.find(".phone").text(data[i].phone);
					$template.find(".wname").text(data[i].wechat_name);
					$template.find(".ctime").text(data[i].create_date);			
					$template.find(".dcount").text(data[i].count);			
					var args = data[i];
					
					(function(args) {
						$template.find("#fendian").on("click", function() {
							App.load("teamsinner", args, function() {
							});
						});
					})(args);

					$(page).find(".teams").append($template);
				}

			});

		});

		App.controller('teamsinner', function(page, args) {
			$(page).find('.uppername').text(args.name);
			var $template = $(page).find('.bmanitems').remove();

			$.getJSON('bottomteams?bman_id=' + args.id, function(data) {
				for ( var i = 0; i < data.length; i++) {
					$template = $template.clone(false);
					$template.find(".bname").text(data[i].name);
					$template.find(".phone").text(data[i].phone);
					$template.find(".wname").text(data[i].wechat_name);
					$template.find(".ctime").text(data[i].create_date);
					var args = data[i];
					(function(args) {
					})(args);
					$(page).find(".teams").append($template);
				}

			});
		});
