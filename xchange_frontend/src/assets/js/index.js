//This for the floating menu
// var ua = navigator.userAgent;
// /Safari|iPhone/i.test(ua) && 0 == /chrome/i.test(ua) && $("#aside-nav").addClass("no-filter");
// var drags = {down: !1, x: 0, y: 0, winWid: 0, winHei: 0, clientX: 0, clientY: 0},
//     asideNav = $("#aside-nav")[0],
//     getCss = function (a, e) {
//         return a.currentStyle ? a.currentStyle[e] : document.defaultView.getComputedStyle(a, !1)[e]
//     };
// $("#aside-nav").on("mousedown", function (a) {
//     drags.down = !0,
//         drags.clientX = a.clientX,
//         drags.clientY = a.clientY,
//         drags.x = getCss(this, "right"),
//         drags.y = getCss(this, "top"),
//         drags.winHei = $(window).height(),
//         drags.winWid = $(window).width(),
//         $(document).on("mousemove", function (a) {
//             if (drags.winWid > 640 && (a.clientX < 120 || a.clientX > drags.winWid - 50))//50px
//                 return !1 /*,console.log(!1)*/;
//             if (a.clientY < 180 || a.clientY > drags.winHei - 120)
//                 return !1;
//             var e = a.clientX - drags.clientX,
//                 t = a.clientY - drags.clientY;
//             asideNav.style.top = parseInt(drags.y) + t + "px";
//             asideNav.style.right = parseInt(drags.x) - e + "px";
//         })
// }).on("mouseup", function () {
//     drags.down = !1, $(document).off("mousemove")
// });
//End of the floating menu


var config2 = {

    settings: {
        hasHeaders: true,
        constrainDragToContainer: true,
        reorderEnabled: true,
        selectionEnabled: false,
        popoutWholeStack: false,
        blockedPopoutsThrowError: true,
        closePopoutsOnUnload: true,
        showPopoutIcon: false,
        showMaximiseIcon: true,
        showCloseIcon: true
    },
    content: [
        {
            type: 'column',
            content: [
                {
                    type: 'row',
                    content: [
                        {
                            type: 'stack',
                            cusId: 'trading_stack',
                            width: 80,
                            content: [
                                {
                                    type: 'component',
                                    componentName: 'tradingview',
                                    title: 'BINANCE:BTCUSDT',
                                    componentState: {symbol: 'BTCUSDT', exchange: 'BINANCE'}
                                },
                                {
                                    type: 'component',
                                    componentName: 'tradingview',
                                    title: 'HUOBI:BTCUSDT',
                                    componentState: {symbol: 'BTCUSDT', exchange: 'HUOBI'}
                                }

                            ]
                        },
                        {
                            type: 'component',
                            componentName: 'news',
                            title: 'News'
                        }
                    ]

                },
                {
                    height: 30,
                    type: 'row',
                    content: [
                        {
                            width: 40,
                            type: 'component',
                            componentName: 'stockGrid',
                            title: 'List'
                        },
                        {
                            type: 'column',
                            content: [
                                {
                                    height: 40,
                                    type: 'component',
                                    componentName: 'xterm',
                                    title: 'Xterm'
                                },
                                {
                                    type: 'component',
                                    componentName: 'example',
                                    title: 'IM',
                                    componentState: {text: 'IM'}
                                }
                            ]

                        },
                        {
                            width: 20,
                            type: 'component',
                            componentName: 'tradeEvent',
                            title: 'Trade',
                            componentState: {text: 'Top right 10%'}
                        }
                    ]
                }
            ]
        }
    ]
}

const myLayout = new window.GoldenLayout(config2, $('#layoutContainer'));

myLayout.on('stackCreated', function (stack) {

    if (!!stack.config['cusId'] && 'trading_stack' == stack.config['cusId']) {
        myLayout.workShopRoot = stack;
    }
});

myLayout.on('itemDestroyed', function (item) {

});


var NewsComponent = function (container, state) {
    this._container = container;
    this.console = $('<div class="express_container"></div>');
    this._lastId = -1;
    container.getElement().append(this.console);
    container.on('open', this._scheduleNewsCreation, this);
}

NewsComponent.prototype._scheduleNewsCreation = function () {
    const interval = setInterval(function () {
        clearInterval(interval);
        this.createNewsPanel();
    }.bind(this), 1);
}

NewsComponent.prototype.createNewsPanel = function () {
    const that = this;
    this._container.on('destroy', this._destroy, this);

    function refresh() {

        $.post(NEWS_API).done(
            function (response) {
                var list = response.data;
                if (!!list && list.length > 0) {
                    var lastBigger = that._lastId;
                    that._lastId = list[0].id;
                    var array = [];
                    for (var i = 0; i < list.length; i++) {
                        var express = list[i];
                        if (express.id <= lastBigger) break;

                        var highlight = express['highlight'] == true ? 'highlight' : '';
                        var time = formatDateHHMM(new Date(express['time'] * 1000));
                        var urls = '';
                        // if (express['urls'] != '') {
                        //     var images = express['urls'].split(',');
                        //     for (var j = 0; j < images.length; j++) {
                        //         urls += '<img class="express_img" src="' + images[j] + '">';
                        //     }
                        // }
                        var html = '<div class="express ' + highlight + '">\
                            <div class="dot"></div>\
                            <div class="content">\
                                <div class="time">' + time + '</div>\
                                <div class="title"><h4>' + express['title'] + '</h4></div>\
                                <div class="text">\
                                    <p>' + express['body'] + '</p>' + urls + '\
                                </div>\
                            </div>\
                        </div>';

                        array.push(html)
                    }
                    if (array.length > 0) {
                        that.console.prepend(array.join(''));
                    }

                }
            }).fail(function (reason) {
        })
    };
    refresh();
    this._interval = setInterval(refresh, 132000);
}

NewsComponent.prototype._destroy = function () {
    if (!!this._interval) {
        safeAction(new function () {
            clearInterval(this._interval);
            this._interval = null;
        }.bind(this));
    }
}

myLayout.registerComponent('news', NewsComponent);

var TradeEventComponent = function (container, state) {
    this._container = container;
    this.console = $('<ul class="console"></ul>');
    this.unsub = null;
    container.getElement().append(this.console);
    container.on('open', this._scheduleTradeEventCreation, this);

}

TradeEventComponent.prototype._scheduleTradeEventCreation = function () {
    var interval = setInterval(function () {
        clearInterval(interval);
        this.createTradeEventPanel();
    }.bind(this), 1);
}

TradeEventComponent.prototype.createTradeEventPanel = function () {

    //Hook the event to this
    //Hook to all the event and then
    var that = this;
    this.unsub = subGlobalTradeEvent(function (evt) {
        //11:2: {"T":2,"s":"BTCUSDT","eX":"BINANCE","rT":1577156538415,"tT":1577156538412,"p":7329.97,"q":0.178822,"BM":0,"B":0}
        that.console.prepend(
            "<li>" + formatDateHHMM(new Date(evt.tT)) + " "
            + evt.p.toFixed(SYMBOL_PRECISION[evt.s].pp) + " " + evt.q.toFixed(SYMBOL_PRECISION[evt.s].ap) +
            " " + evt.s + " " + evt.eX + " </li>");
    });

    this._container.on('destroy', this._destroy, this);
}

TradeEventComponent.prototype._destroy = function () {
    //Hook the event to this
    //Hook to all the event and then
    if (this.unsub != null) {
        this.unsub.unsubscribe();
        this.unsub = null;
    }
}

myLayout.registerComponent('tradeEvent', TradeEventComponent);


myLayout.registerComponent('example', function (container, state) {
    container.getElement().html('<h2>' + state.text + '</h2>');
});


var XtermComponent = function (container, state) {
    this._container = container;
    this.console = $('<ul class="console"></ul>');
    this.unsub = null;


    container.getElement().append(this.console);
    container.on('open', this._scheduleXtermCreation, this);
}

XtermComponent.prototype._scheduleXtermCreation = function () {
    var interval = setInterval(function () {
        clearInterval(interval);
        this.createXterm();

    }.bind(this), 5);
}

XtermComponent.prototype.createXterm = function () {
    //Hook the event to this
    //Hook to all the event and then
    var that = this;
    this.unsub = trackGlobalEvent(function (evt) {
        that.console.prepend("<li>" + formatDateHHMM(new Date()) +
            ": " + JSON.stringify(evt) + " " + " </li>"
        )
    });

    this._container.on('destroy', this._destroy, this);
}
XtermComponent.prototype._destroy = function () {
    //Hook the event to this
    //Hook to all the event and then
    if (this.unsub != null) {
        this.unsub.unsubscribe();
        this.unsub = null;
    }
}


myLayout.registerComponent('xterm', XtermComponent);


/****************************************
 * Trading View Component
 * **************************************/

var TradingViewComponent = function (container, state) {
    this._container = container;
    this._state = state;
    this._exchange = state.exchange;
    this._symbol = state.symbol;
    this._id = uuidv4();//"tv_id_" + this._exchange + "_" + this._symbol + "_" + uuidv4;
    this._fullSymbol = state.exchange + ":" + state.symbol;
    container.getElement().html('<div class="trading_view" id="' + this._id + '" class="tv_container" style="width: 100%;height: 100%"></div>');
    this.tv = null;
    container.on('open', this._scheduleGridCreation, this);
};


TradingViewComponent.prototype._scheduleGridCreation = function () {
    var interval = setInterval(function () {
        clearInterval(interval);
        this._createGrid();
    }.bind(this), 5);
};

TradingViewComponent.prototype._createGrid = function () {

    var widget = new TradingView.widget({
        container_id: this._id,
        autosize: true,
        symbol: this._fullSymbol, //'AAPL'
        fullscreen: false,
        interval: "1",
        timezone: "Asia/Shanghai",
        locale: "zh",
        debug: false,
        allow_symbol_change: false,
        drawings_access: {
            type: "black",
            tools: [{name: "Regression Trend"}]
        },
        theme: "Dark",
        //	BEWARE: no trailing slash is expected in feed URL
        //  Replace this with our self
        //datafeed: new Datafeeds.UDFCompatibleDatafeed("https://demo_feed.tradingview.com"),
        datafeed: new MktDataFeed(this._symbol, this._exchange),
        disabled_features: [
            //"header_resolutions",
            "timeframes_toolbar",
            //"header_symbol_search",
            //"header_chart_type",
            //"header_compare",
            "header_undo_redo",
            "header_screenshot",
            "header_saveload",
            "use_localstorage_for_settings",
            //"left_toolbar",
            "volume_force_overlay"
        ],
        enabled_features: [
            "hide_last_na_study_output",
            "move_logo_to_main_pane",
            "hide_left_toolbar_by_default",
            "remove_library_container_border",
        ],
        //TODO This is test env remember to change this
        library_path: TV_LIB_PATH,
        overrides: {
            "paneProperties.background": "#222222",
            "paneProperties.vertGridProperties.color": "#454545",
            "paneProperties.horzGridProperties.color": "#454545",
            "symbolWatermarkProperties.transparency": 90,
            "scalesProperties.textColor": "#AAA",
            'hide_left_toolbar_by_default': "hidden",
        }
    });


    this.tv = widget;

    widget.onChartReady(function () {
        var chart = widget.chart();
        chart.createStudy('Moving Average', false, false, [5], null, {
            'plot.color': '#965FC4'
        });

        chart.createStudy('Moving Average', false, false, [10], null, {
            'plot.color': '#84AAD5'
        });

    });

    this._container.on('resize', this._resize, this);
    this._container.on('destroy', this._destroy, this);
    this._resize();
}
;

TradingViewComponent.prototype._resize = function () {
    //console.log("resizing");
};

TradingViewComponent.prototype._destroy = function () {
    //console.log("destroy")

    if (this.tv !== null) {
        this.tv.remove()
        this.tv = null
    }

};

myLayout.registerComponent('tradingview', TradingViewComponent);


/****************************************
 * StockGrid Component
 * **************************************/

var StockGridComponent = function (container, state) {
        this._container = container;
        this._state = state;
        this._grid = null;

        function statusFormatter(row, cell, value, columnDef, dataContext) {
            if (value == null || value === "") {
                return "-";
            } else if (value > 0) {
                return "<span class='number-right number-pos'>" + value + "</span>";
            } else {
                return "<span class='number-right number-neg'>" + value + "</span>";
            }
        }

        function numberFormatter(row, cell, value, columnDef, dataContext) {
            if (value == null || value === "") {
                return "-";
            }
            return "<span class='number-right'>" + value + "</span>";
        }


        this._columns = [
            {id: "exchange", name: "eX", field: "exchange"},
            {id: "symbol", name: "Symbol", field: "symbol"},
            {id: "price", name: "Price", field: "price", formatter: numberFormatter},
            {id: "change", name: "%", field: "change", formatter: statusFormatter},
            {id: "changeRel", name: "Change", field: "changeRel", formatter: statusFormatter},
            {id: "volume", name: "Vol", field: "volume", formatter: numberFormatter}
        ];
        this._options = {
            editable: false,
            enableAddRow: false,
            enableCellNavigation: true,
            enableColumnReorder: false,
            cellHighlightCssClass: "changed",
        };

        container.on('open', this._scheduleGridCreation, this);
    }
;


StockGridComponent.prototype._scheduleGridCreation = function () {
    var interval = setInterval(function () {
        var stylesheetNodes = $('link[rel=stylesheet]'), i;

        for (i = 0; i < stylesheetNodes.length; i++) {
            if (stylesheetNodes[i].sheet === null) {
                return;
            }
        }

        clearInterval(interval);
        this._createGrid();

    }.bind(this), 10);
};

StockGridComponent.prototype._createGrid = function () {

    this._grid = new Slick.Grid(
        this._container.getElement(),
        PRICE_HOLDER,
        this._columns,
        this._options
    );

    this._container.on('resize', this._resize, this);
    this._container.on('destroy', this._destroy, this);
    this._resize();

    var that = this;

    this._grid.onClick.subscribe(function (e) {
        const cell = that._grid.getCellFromEvent(e);

        const row = PRICE_HOLDER[cell.row];

        const newItemConfig = {

            type: 'component',
            componentName: 'tradingview',
            title: row.exchange + ":" + row.symbol,
            componentState: {
                symbol: row.symbol, exchange: row.exchange
            }
        };

        //Should we check the exist exchange and
        const root = (!!myLayout.workShopRoot) ? myLayout.workShopRoot : myLayout.root.contentItems[0];

        root.addChild(newItemConfig);
    });


    function refresh() {

        $.get(API + "market/list").done(
            function (response) {
                if (response.code == 200) {
                    var data = response.data;
                    if (!!data && data.length > 0) {
                        var changes = {};
                        for (var i = 0; i < data.length; i++) {
                            var item = data[i];
                            var ix = PRICE_HOLDER_MAP[item.exchange][item.symbol];

                            PRICE_HOLDER[ix].change = ((item.close - item.prevClosed) * 100 / item.prevClosed).toFixed(2);
                            PRICE_HOLDER[ix].price = item.close.toFixed(SYMBOL_PRECISION[item.symbol].pp)
                            PRICE_HOLDER[ix].changeRel = (item.close - item.prevClosed).toFixed(SYMBOL_PRECISION[item.symbol].pp);
                            PRICE_HOLDER[ix].volume = (item.volume / 1000).toFixed(2);

                            changes[ix] = {};
                            changes[ix]["price"] = "changed";
                            changes[ix]["changeRel"] = "changed";
                            changes[ix]["volume"] = "changed";
                            changes[ix]["change"] = "changed";

                            that._grid.invalidateRow(ix);
                        }

                        that._grid.setCellCssStyles("highlight", changes);
                        that._grid.render();
                    }
                }
            }).fail(function (reason) {
        })
    };

    refresh();

    //Every 10 seconds try to flush
    this._interval = setInterval(refresh, 10000);
};

StockGridComponent.prototype._resize = function () {
    this._grid.resizeCanvas();
    this._grid.autosizeColumns();
};

StockGridComponent.prototype._destroy = function () {
    this._grid.destroy();
    if (!!this._interval) {
        safeAction(new function () {
            clearInterval(this._interval);
            this._interval = null;
        }.bind(this));
    }
};
myLayout.registerComponent('stockGrid', StockGridComponent);


startMarketSocket();
myLayout.init();


//Init the  symbol list


