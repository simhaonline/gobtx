package com.gobtx.xchange.configuration;

/**
 * Created by Aaron Kuai on 2019/11/25.
 *
 * <p>This mapper is to mapper the Exchanger internal Symbol to normalized internal used symbol
 */
public interface SymbolMapper {

  /**
   * @param externalSymbol used in each exchange may be very different
   * @return the internal used symbol
   */
  default String map(final String externalSymbol) {
    return externalSymbol;
  }
}
