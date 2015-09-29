$(document).ready(function() {
	$('.pagination').jqPagination({
		link_string	: '?from={page_number}',
		max_page	: 40,
		paged		: function(page) {
			window.location.href="?from="+page;
		}
	});
});