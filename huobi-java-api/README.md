# Huobi API


Source From:

https://github.com/HuobiRDCenter/huobi_Java


0. https://github.com/huobiapi/API_Docs_en/wiki/REST_Reference
1. https://huobiapi.github.io





查看数据格式：

https://codebeautify.org/jsonviewer




1.  K 线图： https://api.huobi.pro/market/history/kline?period=1day&size=200&symbol=btcusdt
2.  支持的产品： https://api.huobi.pro/v1/common/symbols
3.  支持的币种 https://api.huobi.pro/v1/common/currencys



### Symbol 

此接口返回所有火币全球站支持的交易对。


? 

dac/btc

```

      "base-currency": "dac",
      "quote-currency": "btc",
      "price-precision": 10,
      "amount-precision": 2,
      "symbol-partition": "innovation",
      "symbol": "dacbtc",
      "state": "online",
      "value-precision": 8,
      "min-order-amt": 1,
      "max-order-amt": 20000000,
      "min-order-value": 0.0001

```




### currency 

此接口返回所有火币全球站支持的币种。

```

    "hb10",
    "usdt",
    "btc",
    "bch",
    "eth",
    "xrp",
    "ltc",
    "ht",
    "ada",
    "eos",
    "iota",
    "xem",

```



### k LINE 

btc/usdt

amount	float	以基础币种计量的交易量:  14240.5721554321   BTC 

vol	float	以报价币种计量的交易量:      75204772.60073034  USDT 

```

      "base-currency": "btc",
      "quote-currency": "usdt",
      "price-precision": 2,
      "amount-precision": 6,
      "symbol-partition": "main",
      "symbol": "btcusdt",
      "state": "online",
      "value-precision": 8,
      "min-order-amt": 0.0001,
      "max-order-amt": 1000,
      "min-order-value": 1,
      "leverage-ratio": 5,
      "super-margin-leverage-ratio": 3

----------------------------------------

走北京时间 cut 的 0点， 如果要处理 1day key 来cut 的话


      "id": 1556467200,
      "open": 5333.33,
      "close": 5232.53,
      "high": 5333.43,
      "low": 5194,
      "vol": 75204772.60073034,
      "amount": 14240.5721554321,
      "count": 147540

```
