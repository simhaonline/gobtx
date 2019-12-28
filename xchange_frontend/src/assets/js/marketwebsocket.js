var marketWebSocket;
var marketWebSocketStatus;
var marketWatchDog;
var retryCnt = 0;

//Do not know how to do this
var wsTaskMQ = [];

SUB_LF_CALLBACK.noTradeSub = function (exchange, symbol) {
    var subRequest =
        JSON.stringify({
            path: 'unsubTrade',
            parameters: {
                symbols: symbol,
                exchange: exchange
            }
        });

    pushRequest(subRequest);
};

function pushRequest(subRequest) {

    if (marketWebSocket != null && marketWebSocketStatus === 1) {
        marketWebSocket.send(subRequest);
    } else {
        WSLog("连接尚未完成" + subRequest)
        wsTaskMQ.push({
            kick: function () {
                marketWebSocket.send(subRequest);
            }
        });
    }
}

SUB_LF_CALLBACK.firstTradeSub = function (exchange, symbol) {

    WSLog('订阅交易信息 ' + exchange + ":" + symbol);

    var subRequest =
        JSON.stringify({
            path: 'trade',
            parameters: {
                symbols: symbol,
                exchange: exchange
            }
        });

    pushRequest(subRequest);
};
SUB_LF_CALLBACK.noKlineSub = function (exchange, symbol, interval) {

    var subRequest = JSON.stringify({
        path: 'unsubscribe',
        parameters: {
            symbols: symbol,
            exchange: exchange,
            interval: interval
        }
    });
    try {
        marketWebSocket.send(subRequest);
    } catch (e) {
    }
};
SUB_LF_CALLBACK.firstKlineSub = function (exchange, symbol, interval) {
    WSLog('订阅K线信息 ' + exchange + ":" + symbol + "  " + interval);
    var subRequest = JSON.stringify({
        path: 'subscribe',
        parameters: {
            symbols: symbol,
            exchange: exchange,
            interval: interval
        }
    });
    try {
        marketWebSocket.send(subRequest);
    } catch (e) {
        pubTrack({level: 'error', msg: e});
        wsTaskMQ.push({
            kick: function () {
                ws.send(subRequest);
            }
        })
    }
};


function WSLog(msg) {
    pubTrack({from: 'GW', msg: msg})
}

function socketReady() {

    let i, k;
    const kSub = CheckKlineSubs();

    if (kSub.length > 0) {
        WSLog("试图订阅K线 " + JSON.stringify(kSub));
        for (i = 0; i < kSub.length; i++) {
            const sb = kSub[i];
            const ex = sb.ex;
            const itSub = sb.sub;//this is a map
            for (k = 0; k < INTERVALS.length; k++) {
                if (!!itSub[INTERVALS[k]]) {
                    const symbols = itSub[INTERVALS[k]].join(",");
                    WSLog('订阅K线信息 ' + ex + ":" + symbols + "  " + INTERVALS[k]);
                    const subRequest = JSON.stringify({
                        path: 'subscribe',
                        parameters: {
                            symbols: symbols,
                            exchange: ex,
                            interval: INTERVALS[k]
                        }
                    });
                    try {
                        marketWebSocket.send(subRequest);
                    } catch (e) {
                    }
                }
            }
        }
    } else {
        WSLog("无K线数据需要对接");
    }
    const tSub = CheckTradeSubs();
    if (tSub.length > 0) {
        WSLog("试图订阅交易信息 " + JSON.stringify(tSub));
        for (i = 0; i < tSub.length; i++) {
            const ss = tSub[i].ss.join(",");
            WSLog('订阅交易信息 ' + tSub[i].ex + ":" + ss);
            const subRequest =
                JSON.stringify({
                    path: 'trade',
                    parameters: {
                        symbols: ss,
                        exchange: tSub[i].ex
                    }
                });

            try {
                marketWebSocket.send(subRequest);
            } catch (e) {
            }
        }
    } else {
        WSLog("无交易信息需要对接");
    }
}

function startMarketSocket() {
    if (marketWebSocket != null) {
        return
    }
    marketWebSocketStatus = 0;
    var ws = window['MozWebSocket'] ? new MozWebSocket(WS_URL) : window['WebSocket'] ? new WebSocket(WS_URL) : null;
    if (ws) {
        WSLog('连接服务器 [' + retryCnt + ']');
        ws.binaryType = 'arraybuffer';
        ws.onopen = function () {
            marketWebSocketStatus = 1;
            retryCnt = 0;
            WSLog('服务器连接成功');
            marketIntervalCheck();
            socketReady();
        };

        ws.onclose = function () {
            WSLog('服务器断开');
            marketWebSocketStatus = -1;
            //Try to reconnect it
            retryCnt++;
            marketWebSocket = null;
            setTimeout(startMarketSocket, 2000 * retryCnt)
        };

        ws.onmessage = function (evt) {

            var event = JSON.parse(evt.data);
            if (event['T'] == 1) {
                //This is the kLine information
                //{"T":1,"eX":"BINANCE","I":"m1","s":"BTCUSDT","tK":201911290540,"oT":1575006000000,"cT":1575006059999,"o":7445.02000000,"h":7446.31000000,"l":7440.00000000,"c":7443.44000000,"v":24.69045300,"a":183739.34703303,"NOT":290}
                pubKlineEvent(event['eX'], event['s'], event['I'], event);
            } else if (event['T'] == 2) {
                //{"T":2,"eX":"BINANCE","rT":1575006046842,"tT":1575006046840,"p":7444.42000000,"q":0.06021700,"BM":0,"B":0}
                pubTradeEvent(event['eX'], event['s'], event);
            } else if ('X' == event['T']) {
                //{T: "X", RID: 0, PATH: "trade", E_CODE: 10001}
                WSLog('系统错误 ' + event['PATH'] + " " + event['E_CODE']);
            } else {
                //Something is very bad happen
                WSLog('未知消息 ' + JSON.stringify(event));
            }
        }
        marketWebSocket = ws;
        //Start a watch dog to check the status of the websocket
        //every 10 seconds to check the WS status
        if (marketWatchDog != null) {
            //Clear previous's watch dog
            clearInterval(marketWatchDog);
        }
        marketWatchDog = setInterval(marketIntervalCheck, 10000);
        //marketIntervalCheck();
    } else {
        WSLog('不支持WebSocket');
    }
}


function marketIntervalCheck() {
    if (marketWebSocketStatus < 0 || !!!marketWebSocket) {
        //it is dead then we do the check
        startMarketSocket();
    } else {
        //All the MQ task need to push it
        if (marketWebSocket != null) {
            if (wsTaskMQ.length > 0) {
                //poll from the MQ
                var removed = wsTaskMQ.splice(0, wsTaskMQ.length - 1);
                for (var i = 0; i < removed.length - 1; i++) {
                    try {
                        removed[i].kick();
                    } catch (e) {
                        console.log("FAIL_KICK_JOB " + e)
                    }
                }
            }
        }
    }
}

window.onbeforeunload = function () {
    if (marketWebSocket != null) {
        marketWebSocket.close();
        marketWebSocket = null;
    }
    if (marketWatchDog != null) {
        clearInterval(marketWatchDog);
        marketWatchDog = null;
    }
};




