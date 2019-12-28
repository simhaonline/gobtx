package com.gobtx.xchange.configuration;

import com.gobtx.xchange.huobi.HuobiInitiator;
import com.gobtx.xchange.huobi.HuobiMarketDataService;
import com.gobtx.xchange.huobi.HuobiMetaDataService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Created by Aaron Kuai on 2019/11/13. */
@Configuration
@Import({HuobiMarketDataService.class, HuobiMetaDataService.class, HuobiInitiator.class})
public class HuobiConfiguration {}
