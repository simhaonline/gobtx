package com.gobtx.xchange.configuration;

import com.gobtx.model.enums.Exchange;

import java.util.Collection;

/** Created by Aaron Kuai on 2019/11/25. */
public interface ConfigurationProvider {

  Collection<String> symbols(final Exchange exchange);
}
