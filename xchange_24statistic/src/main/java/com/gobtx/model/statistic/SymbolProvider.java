package com.gobtx.model.statistic;

import com.gobtx.model.enums.Exchange;

import java.util.Collection;

/** Created by Aaron Kuai on 2019/12/23. */
@FunctionalInterface
public interface SymbolProvider {
  Collection<String> symbols(final Exchange exchange);
}
