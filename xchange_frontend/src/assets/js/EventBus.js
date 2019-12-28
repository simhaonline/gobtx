/** XChange <--> Symbol */
var XC_TRADE_EVT_SUBS = {};
var XC_KLINE_EVT_SUBS = {};
var GLOBAL_EVT = {};
var GLOBAL_TRADE_EVT_SUBS = {};

function trackGlobalEvent(callback) {
    var id = Symbol('id');
    GLOBAL_EVT[id] = callback;

    return {
        gid: id,
        unsubscribe: function unsubscribe() {
            delete GLOBAL_EVT[id];
        }
    }
}

function pubTrack(evt) {
    if (Object.getOwnPropertySymbols(GLOBAL_EVT).length === 0) return;
    Object.getOwnPropertySymbols(GLOBAL_EVT).forEach(function (key) {
        return GLOBAL_EVT[key](evt);
    });
}

var SUB_LF_CALLBACK = {
    noTradeSub: function (exchange, symbol) {

    },
    firstTradeSub: function (exchange, symbol) {

    },
    noKlineSub: function (exchange, symbol, interval) {

    },
    firstKlineSub: function (exchange, symbol, interval) {

    }
};

//This is to check how many Kline is subscribes?
//XC_KLINE_EVT_SUBS[exchange][symbol][interval]

function CheckKlineSubs() {

    const exSub = [];
    for (var i = 0; i < EXCHANGES.length; i++) {

        if (!!XC_KLINE_EVT_SUBS[EXCHANGES[i]]) {
            const exCtx = XC_KLINE_EVT_SUBS[EXCHANGES[i]];

            const exIntervalSymbols = {};
            var has = false;
            for (var j = 0; j < SYMBOLS.length; j++) {
                if (!!exCtx[SYMBOLS[j]]) {
                    const symCtx = exCtx[SYMBOLS[j]];
                    for (var k = 0; k < INTERVALS.length; k++) {
                        if (!!symCtx[INTERVALS[k]] && Object.getOwnPropertySymbols(symCtx[INTERVALS[k]]).length > 0) {
                            //Exchange -> Symbol -> Interval  has subscribe
                            if (!exIntervalSymbols[INTERVALS[k]]) {
                                exIntervalSymbols[INTERVALS[k]] = [];
                            }
                            exIntervalSymbols[INTERVALS[k]].push(SYMBOLS[j]);
                            has = true;
                        }
                    }
                }
            }
            if (has) {
                exSub.push({ex: EXCHANGES[i], sub: exIntervalSymbols});
            }
        }
    }
    return exSub;
}

//This is to check how many Trade is subscribes
function CheckTradeSubs() {
    //XC_TRADE_EVT_SUBS[exchange][symbol]
    const exSub = [];
    for (var i = 0; i < EXCHANGES.length; i++) {
        var symbols = [];
        if (!!XC_TRADE_EVT_SUBS[EXCHANGES[i]]) {
            const exCtx = XC_TRADE_EVT_SUBS[EXCHANGES[i]];
            for (var j = 0; j < SYMBOLS.length; j++) {
                if (!!exCtx[SYMBOLS[j]] && Object.getOwnPropertySymbols(exCtx[SYMBOLS[j]]).length > 0) {
                    //Exchange --> symbol has subscribe
                    symbols.push(SYMBOLS[j]);
                }
            }
        }
        if (symbols.length > 0) {
            exSub.push({ex: EXCHANGES[i], ss: symbols});
        }
    }
    return exSub;

}

function subGlobalTradeEvent(callback) {

    const id = Symbol('id');
    GLOBAL_TRADE_EVT_SUBS[id] = callback;
    return {
        gid: id,
        unsubscribe: function unsubscribe() {
            delete GLOBAL_TRADE_EVT_SUBS[id];
        }
    }
}

function subTradeEvent(exchange, symbol, callback) {

    const id = Symbol('id');

    if (!XC_TRADE_EVT_SUBS[exchange]) XC_TRADE_EVT_SUBS[exchange] = {};
    if (!XC_TRADE_EVT_SUBS[exchange][symbol]) {
        XC_TRADE_EVT_SUBS[exchange][symbol] = {};
        SUB_LF_CALLBACK.firstTradeSub(exchange, symbol);
    }
    XC_TRADE_EVT_SUBS[exchange][symbol][id] = callback;
    return {
        gid: id,
        unsubscribe: function unsubscribe() {
            delete XC_TRADE_EVT_SUBS[exchange][symbol][id];

            if (Object.getOwnPropertySymbols(XC_TRADE_EVT_SUBS[exchange][symbol]).length === 0) {
                delete XC_TRADE_EVT_SUBS[exchange][symbol];
                SUB_LF_CALLBACK.noTradeSub(exchange, symbol);
            }
        }
    }
}

function pubTradeEvent(exchange, symbol, evt) {

    //Really we need this?
    //pubTrack(evt);
    Object.getOwnPropertySymbols(GLOBAL_TRADE_EVT_SUBS).forEach(function (key) {
        GLOBAL_TRADE_EVT_SUBS[key](evt);
    });

    if (!XC_TRADE_EVT_SUBS[exchange] || !XC_TRADE_EVT_SUBS[exchange][symbol]) return;
    var target = XC_TRADE_EVT_SUBS[exchange][symbol];
    Object.getOwnPropertySymbols(target).forEach(function (key) {
        return target[key](evt);
    });

}

function subKlineEvent(exchange, symbol, interval, callback) {
    var id = Symbol('id');
    if (!XC_KLINE_EVT_SUBS[exchange]) XC_KLINE_EVT_SUBS[exchange] = {};
    if (!XC_KLINE_EVT_SUBS[exchange][symbol]) XC_KLINE_EVT_SUBS[exchange][symbol] = {};
    if (!XC_KLINE_EVT_SUBS[exchange][symbol][interval]) {
        XC_KLINE_EVT_SUBS[exchange][symbol][interval] = {};
        SUB_LF_CALLBACK.firstKlineSub(exchange, symbol, interval);
    }

    XC_KLINE_EVT_SUBS[exchange][symbol][interval][id] = callback;

    return {
        gid: id,
        unsubscribe: function unsubscribe() {
            delete XC_KLINE_EVT_SUBS[exchange][symbol][interval][id];

            if (Object.getOwnPropertySymbols(XC_KLINE_EVT_SUBS[exchange][symbol][interval]).length === 0) {
                delete XC_KLINE_EVT_SUBS[exchange][symbol][interval];
                SUB_LF_CALLBACK.noKlineSub(exchange, symbol, interval);
            }
        }
    }
}

function pubKlineEvent(exchange, symbol, interval, evt) {

    if (!XC_KLINE_EVT_SUBS[exchange] || !XC_KLINE_EVT_SUBS[exchange][symbol] || !XC_KLINE_EVT_SUBS[exchange][symbol][interval]) return;

    var target = XC_KLINE_EVT_SUBS[exchange][symbol][interval];
    Object.getOwnPropertySymbols(target).forEach(function (key) {
        target[key](evt);
    });
}
