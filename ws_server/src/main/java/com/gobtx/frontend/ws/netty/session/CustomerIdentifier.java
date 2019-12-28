package com.gobtx.frontend.ws.netty.session;

import com.gobtx.common.web.model.session.Identifier;
import io.netty.channel.Channel;

import java.io.Serializable;
import java.util.Objects;

/** Created by Aaron Kuai on 2019/11/19. */
public class CustomerIdentifier implements Identifier, Serializable {

  private static final long serialVersionUID = 9009251768768713623L;

  protected final String accountId;

  protected final boolean anonymous;

  public static CustomerIdentifier anonymousIdentifier(final Channel channel) {

    return new CustomerIdentifier(channel);
  }

  protected CustomerIdentifier(final Channel channel) {
    this.accountId = channel.id().asLongText();
    this.anonymous = true;
  }

  public CustomerIdentifier(final String accountId) {
    this.accountId = accountId;
    this.anonymous = false;
  }

  public String getAccountId() {
    return accountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CustomerIdentifier that = (CustomerIdentifier) o;
    return Objects.equals(accountId, that.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId);
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  @Override
  public String toString() {
    return "CustomerIdentifier{" + "accountId='" + accountId + '\'' + '}';
  }
}
