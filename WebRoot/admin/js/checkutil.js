function checkPhoneNotExist(txtinput,success) {
	$("#"+txtinput).on("blur", function() {
		var phone = $(this).val();
		if (phone == "") {
			$("#submitbtn").attr("disabled", true);
			return;
		}
		$.get("checkphone", {
			phone : phone
		}, function(data) {
			if (data.errorMsg) {
				$("#submitbtn").attr("disabled", false);
			} else {
				alert("手机号码已经存在")

				$("#submitbtn").attr("disabled", true);
			}
			$("#submitbtn").attr("disabled", false);
		});
	})
}