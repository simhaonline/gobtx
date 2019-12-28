package com.gobtx.frontend.ws.hub;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/** Created by Aaron Kuai on 2019/11/20. */
@Component
@Profile("local-hub-finder")
public class LocalServerKeySupplier implements ServerKeySupplier, InitializingBean {

  @Autowired HubServerProperties properties;

  public static final Pattern SPLIT1 = Pattern.compile(";", Pattern.LITERAL);
  public static final Pattern SPLIT2 = Pattern.compile(":", Pattern.LITERAL);

  final Set<IPV4ServerKey> keys = new LinkedHashSet<>();

  @Override
  public void afterPropertiesSet() throws Exception {

    for (final String each : SPLIT1.split(properties.getHubHosts())) {

      final String[] metas = SPLIT2.split(each);

      keys.add(new IPV4ServerKey(metas[0], Integer.parseInt(metas[1])));
    }
  }

  @Override
  public Collection<ServerKey> servers() {
    return Collections.unmodifiableCollection(keys);
  }
}
