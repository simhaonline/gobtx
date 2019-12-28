package com.gobtx.frontend.ws.netty;

import com.gobtx.frontend.ws.netty.session.ChannelContextMap;
import com.gobtx.frontend.ws.netty.session.CustomerIdentifier;
import io.netty.util.AttributeKey;

/** Created by Aaron Kuai on 2019/11/19. */
public interface Constants {

  AttributeKey<CustomerIdentifier> CUSTOMER_ID_ATTR_KEY =
      AttributeKey.valueOf(CustomerIdentifier.class, "CID");

  /** Client Context Map */
  AttributeKey<ChannelContextMap> CTX_MAP_ATTR_KEY =
      AttributeKey.valueOf(ChannelContextMap.class, "CXM");
}
