package com.gobtx.frontend.ws.hub;

import java.util.Collection;

/** Created by Aaron Kuai on 2019/11/20. */
@FunctionalInterface
public interface ServerKeySupplier {

  Collection<ServerKey> servers();
}
