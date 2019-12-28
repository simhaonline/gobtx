var lastExpressTime = 1577165076;
$(document).ready(function () {
    $('.click_loading').click(function () {
        var lock = false;
        $('.click_loading').html('<i class="fa fa-spinner fa-spin"></i>努力加载中……');
        if (!lock) {
            lock = true;
            $.post('/api/express', {time: lastExpressTime}, function (data) {
                if (data.status == 1) {
                    var list = data.data;
                    var array = [];
                    for (var i = 0; i < list.length; i++) {
                        var express = list[i];
                        var hightlight = express['highlight'] == 1 ? 'highlight' : '';
                        var time = formatDateTime(express['time'] * 1000, 'hh:mm');
                        var source = '';
                        switch (express['sid']) {
                            case 1:
                                source = '小葱';
                                break;
                            case 2:
                                source = '金色财经';
                                break;
                        }
                        var urls = '';
                        if (express['urls'] != '') {
                            var images = express['urls'].split(',');
                            for (var j = 0; j < images.length; j++) {
                                urls += '<img class="express_img col-xs-6 col-md-4" src="' + images[j] + '">';
                            }
                        }
                        var html = '            <div class="express col-xs-12 col-md-12 ' + hightlight + '">\
                    <div class="dot"></div>\
                    <div class="content">\
                        <div class="time">' + time + '</div>\
                        <div class="title"><h4>' + express['title'] + '</h4></div>\
                        <div class="text">\
                            <p>' + express['body'] + '<span class="source">来源:<b>' + source + '</b></span></p>' + urls + '\
                        </div>\
                    </div>\
                </div>';
                        $('.express_left').append(html);
                    }
                    lastExpressTime = list[list.length - 1]['time'];
                }
                lock = false;
                $('.click_loading').html('点击加载');
            })
        }
    })
    //点击预览大图
    $("#img-zoom").click(function () {
        $('#img-modal').modal("hide");
    });
    $("#img-dialog").click(function () {
        $('#img-modal').modal("hide");
    });
    $('.express_left').on('click', '.express_img', function () {
        var src = $(this).attr("src");
        $('#img-zoom').attr("src", src);
        $('#img-modal').modal();
    });
});
