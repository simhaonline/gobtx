var MktDataFeed = function (symbol, exchange) {
    this.symbol = symbol;
    this.exchange = exchange;

    this.lastBarTime = null;

    this.subscriptionRecord = {};

    //Resolution(interval) -->
    //  lastBarTime
    //  lastBar
    //  currentBar
    //  To mock the pulse updates of the TV
    //  Resolution and
    //
    //  interval
    //

    this.unsubscribeTradeHandler = null;
    this.unsubscribeKlineHandler = null;
    this.lastSubs = {};

};

var SUPPORTED_RESOLUTIONS = ["1", "3", "5", "15", "30", "60", "120", "240", "480", "720", "1D", "3D", "1W", "1M"];//, "1Y"];

MktDataFeed.prototype.onReady = function (callback) {
    var config = {};
    config.exchanges = [{
        "value": "BINANCE"
    }, {
        "value": "HUOBI"
    }];

    config.supported_resolutions = SUPPORTED_RESOLUTIONS;
    config.supports_group_request = false;
    config.supports_marks = false;
    config.supports_search = true;
    config.supports_time = false;
    config.supports_timescale_marks = false;

    //Not use the promise yet
    setTimeout(function () {
        callback(config);
    }, 0);


};


MktDataFeed.prototype.subscribeBars = function (symbolInfo, resolution, onRealtimeCallback, subscriberUID, onResetCacheNeededCallback) {
    //1. hook to the event bus
    //2. trigger the listener to the
    //if it is the minute bar ignore it as it issued very first time

    //should first try get Bar and then  subscribe the bar

    //console.log("Try Sub " + symbolInfo + "  " + resolution + "  " + subscriberUID);


    //Try to sub the trade evt
    this.unsubscribeTradeHandler = subTradeEvent(this.exchange, this.symbol, function (evt) {
        //console.log("trade info " + evt);
        //ON MSG {"T":2,"s":"BTCUSDT","eX":"BINANCE","rT":1576845308303,"tT":1576845308300,"p":7162.00000000,"q":0.21627200,"BM":1,"B":0}

        var context = that.subscriptionRecord[resolution];

        if (!!context && !!context.lastBar) {

            const p = evt.p;
            const q = evt.q;

            context.lastBar.close = p;
            if (p > context.lastBar.high) {
                context.lastBar.high = p;
            }
            if (p < context.lastBar.low) {
                context.lastBar.low = p;
            }
            context.lastBar.volume += q;
            onRealtimeCallback(context.lastBar);
        }


    });

    const interval = TV_2_BK_MAP["" + resolution];

    this.lastSubs[subscriberUID] = interval;

    const that = this;

    this.unsubscribeKlineHandler =

        subKlineEvent(this.exchange, this.symbol, interval, function (evt) {
            //{"T":1,"eX":"BINANCE","I":"m1","s":"BTCUSDT","tK":201911290540,"oT":1575006000000,"cT":1575006059999,"o":7445.02000000,"h":7446.31000000,"l":7440.00000000,"c":7443.44000000,"v":24.69045300,"a":183739.34703303,"NOT":290}

            if (!!!that.subscriptionRecord[resolution] || !!!that.subscriptionRecord[resolution].lastBar) {
                //The subscribe come earlier than the historical things
                return;
            }
            const context = that.subscriptionRecord[resolution];

            if (evt.oT < context.lastBar.time) {
                //This is a Chaos bar
                return;
            } else if (evt.oT == context.lastBar.time) {
                //Update Old Bar
                context.lastBar.open = evt.o;
                context.lastBar.high = evt.h;
                context.lastBar.low = evt.l;
                context.lastBar.close = evt.c;
                context.lastBar.volume = evt.v;
                onRealtimeCallback(context.lastBar);
            } else {
                //This is a new Bar
                onRealtimeCallback(context.lastBar);

                const newBar = {
                    time: evt.oT,  //Open time
                    open: evt.o,  // Open
                    high: evt.h,  //high
                    low: evt.l,   //low
                    close: evt.c, //close
                    volume: evt.v //Amount or volume?
                };

                onRealtimeCallback(newBar);
                context.lastBar = newBar;
            }

        });

};

MktDataFeed.prototype.unsubscribeBars = function (subscriberUID) {

    //console.log("Try unsub::::  " + subscriberUID + " >>>> " + this.lastSubs[subscriberUID])
    //console.log("Last sub:::  " + this.lastSubs);

    if (!!this.lastSubs[subscriberUID]) {
        delete this.subscriptionRecord[this.lastSubs[subscriberUID]];
        delete this.lastSubs[subscriberUID];
    }

    if (this.unsubscribeKlineHandler != null) {
        this.unsubscribeKlineHandler.unsubscribe();
    }
    if (this.unsubscribeTradeHandler != null) {
        this.unsubscribeTradeHandler.unsubscribe();
    }
};

MktDataFeed.prototype.resolveSymbol = function (symbolName, onSymbolResolvedCallback, onResolveErrorCallback) {

    var data = {
        "name": this.symbol,
        "exchange-traded": this.exchange,
        "exchange-listed": this.exchange,
        "minmov": 1,
        "volumescale": 5,
        "has_daily": true,
        "has_weekly_and_monthly": true,
        "has_intraday": true,
        "description": this.symbol,
        "type": "bitcoin",
        "session": "24x7",
        "supported_resolutions": SUPPORTED_RESOLUTIONS,
        "pricescale": Math.pow(10, this.scale || 2),
        "ticker": "",
        "timezone": "Asia/Shanghai"
    };
    setTimeout(function () {
        onSymbolResolvedCallback(data);
    }, 0);

};


function narrow2Second(ms) {
    return (ms / 1000 | 0) * 1000;
}


MktDataFeed.prototype.getBars = function (symbolInfo, resolution, from, to, onHistoryCallback, onErrorCallback, firstDataRequest) {
    //This is to get the bars it go the restful API
    //console.log("get Bars " + symbolInfo + "  " + resolution + "  " + new Date(from * 1000) + "   " + new Date(to * 1000))


    if (!!!this.subscriptionRecord[resolution]) {

        this.subscriptionRecord[resolution] = {
            resolution: resolution,
            symbolInfo: symbolInfo,
            lastBar: null
        };
    }

    var context = this.subscriptionRecord[resolution];

    var bars = [];

    var params = {
        symbol: this.symbol,
        exchange: this.exchange,
        startTime: from * 1000,
        first: firstDataRequest,
        endTime: firstDataRequest ? new Date().getTime() : to * 1000,
        interval: TV_2_BK_MAP["" + resolution]
    };

    $.ajax({
        type: 'POST',
        url: MARKET_HIS_URL,
        data: params,
        dataType: 'json'
    }).done(function (response) {

        if (response.code != 200) {
            onErrorCallback(response.message);
            return
        }

        var data = response.data;
        var noData = false;
        if (!!data && data.length > 0) {

            //Always ASC  small-> bigger

            for (var i = 0; i < data.length; i++) {

                const item = data[i];
                bars.push({time: item[0], open: item[1], high: item[2], low: item[3], close: item[4], volume: item[5]})
            }


            //Only record the biggest one

            if (!!!context.lastBar || bars[bars.length - 1].time > context.lastBar.time) {
                context.lastBar = bars[bars.length - 1];
            }

        } else {
            noData = true;
            context.lastBar = null;
            context.currentBar = null;
        }
        onHistoryCallback(bars, {noData: noData});
    }).fail(function (reason) {
        onErrorCallback(reason);
    });


};

MktDataFeed.prototype.searchSymbols = function (userInput, exchange, symbolType, onResultReadyCallback) {

    //console.log(userInput)
    //console.log(exchange)
};

MktDataFeed.prototype.getServerTime = function (callback) {

};
