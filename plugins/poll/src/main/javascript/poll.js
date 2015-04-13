(function($) {
    $(function () {
        $(".element-poll").each(function () {
            var poll = $(this);
            var pollId = poll.attr("data-pollId");
            var getCookie = function (cname) {
                var name = cname + "=";
                var ca = document.cookie.split(';');
                for(var i=0; i<ca.length; i++) {
                    var c = ca[i];
                    while (c.charAt(0)==' ') c = c.substring(1);
                    if (c.indexOf(name) != -1) return c.substring(name.length,c.length);
                }
                return "";
            }
            if(getCookie("fd_" + pollId)) {
                $('.poll-link', poll).hide();
                $('.poll-container', poll).toggle();
            }
            poll.on('click', '.poll-link', function (event) {
                event.preventDefault();
                $('.poll-container', poll).toggle();
            });
            poll.on('change','form input:radio', function (event) {
                event.preventDefault();
                poll.find('form').submit();
            });
            poll.on('submit', 'form', function (event) {
                event.preventDefault();
                var form = $(this);
                var url = form.attr("action");
                var formData = form.serialize();
                $.post(url, formData, null, 'json')
                    .done(function (data) {
                        $.each(data.results, function (answerId, answerResult) {
                            var elem = $("li[data-answerId='" + answerId + "']", poll);
                            $('.poll-result-data', elem).text(answerResult.percent + '%' + ' ('+answerResult.count+')');
                            $('progress', elem).val(answerResult.percent);
                        });
                        $('.poll-total-vote-count', poll).text(data.totalVoteCount);
                        $('.poll-link', poll).hide();
                        $('.poll-vote-success', poll).show();
                        $('.poll-container', poll).toggle();
                    })
                    .fail(function (data) {
                        if (data.status === 403) {
                            $('.poll-container', poll).toggle();
                        }
                        $('.poll-vote-success', poll).hide();
                        $('form', poll).trigger('reset');
                        $('.text-danger', poll).show();
                    });
            });
        });
    });
})(jQuery);
