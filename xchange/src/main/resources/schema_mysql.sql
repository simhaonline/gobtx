create table binance_minutely (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_minutely_symbol_index on binance_minutely (symbol);
create index binance_minutely_timeKey_index on binance_minutely (timeKey);

create table binance_three_minute (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_three_minute_symbol_index on binance_three_minute (symbol);
create index binance_three_minute_timeKey_index on binance_three_minute (timeKey);

create table binance_five_minute (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_five_minute_symbol_index on binance_five_minute (symbol);
create index binance_five_minute_timeKey_index on binance_five_minute (timeKey);

create table binance_fifteen_minute (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_fifteen_minute_symbol_index on binance_fifteen_minute (symbol);
create index binance_fifteen_minute_timeKey_index on binance_fifteen_minute (timeKey);

create table binance_half_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_half_hour_symbol_index on binance_half_hour (symbol);
create index binance_half_hour_timeKey_index on binance_half_hour (timeKey);

create table binance_hourly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_hourly_symbol_index on binance_hourly (symbol);
create index binance_hourly_timeKey_index on binance_hourly (timeKey);

create table binance_two_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_two_hour_symbol_index on binance_two_hour (symbol);
create index binance_two_hour_timeKey_index on binance_two_hour (timeKey);

create table binance_four_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_four_hour_symbol_index on binance_four_hour (symbol);
create index binance_four_hour_timeKey_index on binance_four_hour (timeKey);

create table binance_six_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_six_hour_symbol_index on binance_six_hour (symbol);
create index binance_six_hour_timeKey_index on binance_six_hour (timeKey);

create table binance_eight_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_eight_hour_symbol_index on binance_eight_hour (symbol);
create index binance_eight_hour_timeKey_index on binance_eight_hour (timeKey);

create table binance_half_day (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_half_day_symbol_index on binance_half_day (symbol);
create index binance_half_day_timeKey_index on binance_half_day (timeKey);

create table binance_daily (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_daily_symbol_index on binance_daily (symbol);
create index binance_daily_timeKey_index on binance_daily (timeKey);

create table binance_three_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_three_hour_symbol_index on binance_three_hour (symbol);
create index binance_three_hour_timeKey_index on binance_three_hour (timeKey);

create table binance_weekly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_weekly_symbol_index on binance_weekly (symbol);
create index binance_weekly_timeKey_index on binance_weekly (timeKey);

create table binance_monthly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_monthly_symbol_index on binance_monthly (symbol);
create index binance_monthly_timeKey_index on binance_monthly (timeKey);

create table binance_yearly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index binance_yearly_symbol_index on binance_yearly (symbol);
create index binance_yearly_timeKey_index on binance_yearly (timeKey);

create table huobi_minutely (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_minutely_symbol_index on huobi_minutely (symbol);
create index huobi_minutely_timeKey_index on huobi_minutely (timeKey);

create table huobi_three_minute (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_three_minute_symbol_index on huobi_three_minute (symbol);
create index huobi_three_minute_timeKey_index on huobi_three_minute (timeKey);

create table huobi_five_minute (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_five_minute_symbol_index on huobi_five_minute (symbol);
create index huobi_five_minute_timeKey_index on huobi_five_minute (timeKey);

create table huobi_fifteen_minute (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_fifteen_minute_symbol_index on huobi_fifteen_minute (symbol);
create index huobi_fifteen_minute_timeKey_index on huobi_fifteen_minute (timeKey);

create table huobi_half_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_half_hour_symbol_index on huobi_half_hour (symbol);
create index huobi_half_hour_timeKey_index on huobi_half_hour (timeKey);

create table huobi_hourly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_hourly_symbol_index on huobi_hourly (symbol);
create index huobi_hourly_timeKey_index on huobi_hourly (timeKey);

create table huobi_two_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_two_hour_symbol_index on huobi_two_hour (symbol);
create index huobi_two_hour_timeKey_index on huobi_two_hour (timeKey);

create table huobi_four_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_four_hour_symbol_index on huobi_four_hour (symbol);
create index huobi_four_hour_timeKey_index on huobi_four_hour (timeKey);

create table huobi_six_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_six_hour_symbol_index on huobi_six_hour (symbol);
create index huobi_six_hour_timeKey_index on huobi_six_hour (timeKey);

create table huobi_eight_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_eight_hour_symbol_index on huobi_eight_hour (symbol);
create index huobi_eight_hour_timeKey_index on huobi_eight_hour (timeKey);

create table huobi_half_day (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_half_day_symbol_index on huobi_half_day (symbol);
create index huobi_half_day_timeKey_index on huobi_half_day (timeKey);

create table huobi_daily (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_daily_symbol_index on huobi_daily (symbol);
create index huobi_daily_timeKey_index on huobi_daily (timeKey);

create table huobi_three_hour (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_three_hour_symbol_index on huobi_three_hour (symbol);
create index huobi_three_hour_timeKey_index on huobi_three_hour (timeKey);

create table huobi_weekly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_weekly_symbol_index on huobi_weekly (symbol);
create index huobi_weekly_timeKey_index on huobi_weekly (timeKey);

create table huobi_monthly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_monthly_symbol_index on huobi_monthly (symbol);
create index huobi_monthly_timeKey_index on huobi_monthly (timeKey);

create table huobi_yearly (
symbol varchar(12) not null,
timeKey bigint default 0,
openTime bigint default  0,
closeTime bigint default 0,
open DECIMAL(36,12) default 0,
high DECIMAL(36,12) default 0,
low DECIMAL(36,12) default 0,
close DECIMAL(36,12) default 0,
volume DECIMAL(36,12) default 0,
amount DECIMAL(36,12) default 0,
numberOfTrades   bigint default  0,
primary key (symbol, timeKey)
);


create index huobi_yearly_symbol_index on huobi_yearly (symbol);
create index huobi_yearly_timeKey_index on huobi_yearly (timeKey);
