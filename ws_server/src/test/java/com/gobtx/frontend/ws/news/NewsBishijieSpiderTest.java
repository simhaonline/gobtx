package com.gobtx.frontend.ws.news;

import org.junit.Test;

/** Created by Aaron Kuai on 2019/12/27. */
public class NewsBishijieSpiderTest {

  @Test
  public void testCut() {

    final String kk = "20:12 土耳其警方抓获四名比特币小偷，涉案金额170万美元";
    System.out.println(kk.substring(6));
  }

  @Test
  public void doFetch() throws InterruptedException {

    NewsBishijieSpider spider = new NewsBishijieSpider();

    Thread.sleep(10000);

    System.out.println(spider.latest(10));
  }
}
