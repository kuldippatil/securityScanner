package com.security.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author kuldippatil
 */

  @Getter
  @Setter
  @ToString
  public class PriceReport {
    private Change priceChange;
    private Change totalChange;
    @Getter
    @Setter
    private class Change {
      Integer oldPrice;
      Integer newPrice;
    }
  }

