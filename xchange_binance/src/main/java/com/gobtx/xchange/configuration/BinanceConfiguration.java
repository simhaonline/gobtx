package com.gobtx.xchange.configuration;

import com.gobtx.xchange.binance.BinanceInitiator;
import com.gobtx.xchange.binance.BinanceMarketDataService;
import com.gobtx.xchange.binance.BinanceMetaDataService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({BinanceMarketDataService.class, BinanceMetaDataService.class, BinanceInitiator.class})
public class BinanceConfiguration {}
