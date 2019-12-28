const TV_LIB_PATH = "/btx-backend/xchange_frontend/src/assets/tv/charting_library/";
//const TV_LIB_PATH = "/assets/tv/charting_library/";
//const API = "http://localhost:8088/";


const WS_URL = "wss://market.gobtx.com/ws";
const API = "https://api.gobtx.com/";
const NEWS_API = API + "market/news";
const MARKET_HIS_URL = API + "market/history";


var TV_2_BK_MAP = {

    '1': 'm1',
    '3': 'm3',
    '5': 'm5',
    '15': 'm15',
    '30': 'm30',
    '60': 'h1',
    '120': 'h2',
    '240': 'h4',
    '480': 'h8',
    '720': 'h12',
    '1D': 'd1',
    '3D': 'd3',
    '1W': 'w1',
    '1M': 'M1',
    '12M': 'Y1'

};

var BK_2_TV_MAP = {
    'm1': '1',
    'm3': '3',
    'm5': '5',
    'm15': '15',
    'm30': '30',
    'h1': '60',
    'h2': '120',
    'h4': '240',
    'h8': '480',
    'h12': '720',
    'd1': '1D',
    'd3': '3D',
    'w1': '1W',
    'M1': '1M',
    'Y1': '12M'
}

var INTERVALS = [

    'm1',
    'm3',
    'm5',
    'm15',
    'm30',
    'h1',
    'h2',
    'h4',
    'h8',
    'h12',
    'd1',
    'd3',
    'w1',
    'M1',
    'Y1'

];

//pp:Price precision
//ap:Amount precision
const SYMBOL_PRECISION = {
    'BTCUSDT': {
        pp: 2,
        ap: 6
    },
    'ETHUSDT': {
        pp: 2,
        ap: 4
    },
    'XRPUSDT': {
        pp: 5,
        ap: 2
    },
    'BCHUSDT': {
        pp: 2,
        ap: 4
    },
    'LTCUSDT': {
        pp: 2,
        ap: 4
    },
    'EOSUSDT': {
        pp: 4,
        ap: 4
    }
}

const EXCHANGES = [
    'BINANCE', 'HUOBI'
];

const SYMBOLS = [
    'BTCUSDT',
    'ETHUSDT',
    'XRPUSDT',
    'BCHUSDT',
    'LTCUSDT',
    'EOSUSDT'
];

const PRICE_HOLDER = [
    {'symbol': 'BTCUSDT', 'exchange': 'BINANCE', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'ETHUSDT', 'exchange': 'BINANCE', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'XRPUSDT', 'exchange': 'BINANCE', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'BCHUSDT', 'exchange': 'BINANCE', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'LTCUSDT', 'exchange': 'BINANCE', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'EOSUSDT', 'exchange': 'BINANCE', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'BTCUSDT', 'exchange': 'HUOBI', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'ETHUSDT', 'exchange': 'HUOBI', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'XRPUSDT', 'exchange': 'HUOBI', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'BCHUSDT', 'exchange': 'HUOBI', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'LTCUSDT', 'exchange': 'HUOBI', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},
    {'symbol': 'EOSUSDT', 'exchange': 'HUOBI', 'price': '-', 'change': '-', 'changeRel': '-', 'volume': '-'},

];
const PRICE_HOLDER_MAP = {

    'BINANCE': {
        'BTCUSDT': 0,
        'ETHUSDT': 1,
        'XRPUSDT': 2,
        'BCHUSDT': 3,
        'LTCUSDT': 4,
        'EOSUSDT': 5,
    },

    'HUOBI': {
        'BTCUSDT': 6,
        'ETHUSDT': 7,
        'XRPUSDT': 8,
        'BCHUSDT': 9,
        'LTCUSDT': 10,
        'EOSUSDT': 11,
    },

};


function uuidv4() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

function formatDateHHMM(dt) {
    const hr = dt.getHours(),
        mt = dt.getMinutes();
    return (hr < 10 ? ('0' + hr) : hr) + ":" + (mt < 10 ? ('0' + mt) : mt);
}

function safeAction(f) {
    try {
        f();
    } catch (e) {
    }
}
