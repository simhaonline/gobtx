create table mock_minutely (
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


create index mock_minutely_symbol_index on mock_minutely (symbol);
create index mock_minutely_timeKey_index on mock_minutely (timeKey);

create table mock_three_minute (
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


create index mock_three_minute_symbol_index on mock_three_minute (symbol);
create index mock_three_minute_timeKey_index on mock_three_minute (timeKey);

create table mock_five_minute (
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


create index mock_five_minute_symbol_index on mock_five_minute (symbol);
create index mock_five_minute_timeKey_index on mock_five_minute (timeKey);

create table mock_fifteen_minute (
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


create index mock_fifteen_minute_symbol_index on mock_fifteen_minute (symbol);
create index mock_fifteen_minute_timeKey_index on mock_fifteen_minute (timeKey);

create table mock_half_hour (
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


create index mock_half_hour_symbol_index on mock_half_hour (symbol);
create index mock_half_hour_timeKey_index on mock_half_hour (timeKey);

create table mock_hourly (
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


create index mock_hourly_symbol_index on mock_hourly (symbol);
create index mock_hourly_timeKey_index on mock_hourly (timeKey);

create table mock_two_hour (
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


create index mock_two_hour_symbol_index on mock_two_hour (symbol);
create index mock_two_hour_timeKey_index on mock_two_hour (timeKey);

create table mock_four_hour (
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


create index mock_four_hour_symbol_index on mock_four_hour (symbol);
create index mock_four_hour_timeKey_index on mock_four_hour (timeKey);

create table mock_six_hour (
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


create index mock_six_hour_symbol_index on mock_six_hour (symbol);
create index mock_six_hour_timeKey_index on mock_six_hour (timeKey);

create table mock_eight_hour (
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


create index mock_eight_hour_symbol_index on mock_eight_hour (symbol);
create index mock_eight_hour_timeKey_index on mock_eight_hour (timeKey);

create table mock_half_day (
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


create index mock_half_day_symbol_index on mock_half_day (symbol);
create index mock_half_day_timeKey_index on mock_half_day (timeKey);

create table mock_daily (
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


create index mock_daily_symbol_index on mock_daily (symbol);
create index mock_daily_timeKey_index on mock_daily (timeKey);

create table mock_three_hour (
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


create index mock_three_hour_symbol_index on mock_three_hour (symbol);
create index mock_three_hour_timeKey_index on mock_three_hour (timeKey);

create table mock_weekly (
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


create index mock_weekly_symbol_index on mock_weekly (symbol);
create index mock_weekly_timeKey_index on mock_weekly (timeKey);

create table mock_monthly (
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


create index mock_monthly_symbol_index on mock_monthly (symbol);
create index mock_monthly_timeKey_index on mock_monthly (timeKey);

create table mock_yearly (
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


create index mock_yearly_symbol_index on mock_yearly (symbol);
create index mock_yearly_timeKey_index on mock_yearly (timeKey);
