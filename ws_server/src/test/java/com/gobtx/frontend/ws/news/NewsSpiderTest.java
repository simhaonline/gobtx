package com.gobtx.frontend.ws.news;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/** Created by Aaron Kuai on 2019/12/24. */
public class NewsSpiderTest {

  @Test
  public void testUrl() throws MalformedURLException {
    URL url = new URL("https://www.bishijie.com/kuaixun/513640.html");
    System.out.println("https://www.bishijie.com/kuaixun/".length());

    System.out.println("https://www.bishijie.com/kuaixun/513640.html".substring(33));

    String newsTarget = "https://www.bishijie.com/kuaixun/513640.html";
    final String path = newsTarget.substring(33);
    final String id = path.substring(0, path.length() - 5);
    System.out.println(id);
  }

  @Test
  public void jsoupTest() throws IOException {

    Document doc = Jsoup.connect("https://www.bishijie.com/").get();

    Elements newsHeadlines = doc.select(".content");

    for (Element headline : newsHeadlines) {

      // https://www.bishijie.com/kuaixun/513496.html

      // 513496.html is the key

      Element newsUrl = headline.selectFirst("a");
      final String newsTarget = newsUrl.absUrl("href");
      final String path = newsTarget.substring(33);
      final String id = path.substring(0, path.length() - 3);
      System.out.println(id);

      Element spanNewstime = newsUrl.selectFirst(".newstime");

      final String title = newsUrl.text();

      final String content = headline.selectFirst(".h63").text();

      // .image-box
      // https://img.bishijie.com/newsflash-5e058b30e80f0
      // https://img.bishijie.com/newsflash-5e058b30e80f0?imageView2/1/w/300/h/168/q/75|imageslim

      System.out.println(spanNewstime.text());
      System.out.println(newsTarget);
      System.out.println(title);
      System.out.println(content);
      System.out.println("\n---------------------\n");
    }

    // <div class="content" data-v-80a8f564=""><a
    // href="https://www.bishijie.com/kuaixun/513594.html" target="_blank" data-v-80a8f564=""><h3
    // data-v-80a8f564=""><span class="newstime" data-v-80a8f564="">
    //                        18:08
    //                        <i class="dian" data-v-80a8f564=""></i></span>
    //                      Rozeus合作伙伴Bflysoft与CJ大韩通运（CJ Korea Express）签署媒体大数据检测服务合同
    //                    </h3></a>
    //    <div class="news-content" data-v-80a8f564="">
    //        <div class="h63"
    // data-v-80a8f564="">据官方消息，韩国去中心化媒体监督解决方案Rozeus的合作伙伴Bflysoft近日与大韩通运完成签约。Rozeus监控平台以媒体大数据为基础，提供对网络新闻、评论、社交、电视、YouTube等的监控、分析服务，通过实时媒体监控以及分析，帮助用户获得热门信息的报告、新观点，并实现危机控制效果。CJ大韩通运是韩国最大的货物配送公司，主导全求物流革新。</div>
    //        <!---->
    //    </div>
    //    <div class="sourceshare" data-v-80a8f564="">
    //        <!---->
    //        <div class="share-wrap" data-v-80a8f564="">
    //            <div class="clearfix bullBox" data-v-80a8f564="">
    //                <div class="add" data-v-80a8f564=""></div>
    //                <div class="subtraction" data-v-80a8f564=""></div>
    //                <div class="bull" data-v-80a8f564="">
    //                    <img >看多 1
    //                </div>
    //                <!---->
    //            </div>
    //            <div class="clearfix bearBox" data-v-80a8f564="">
    //                <div class="add" data-v-80a8f564=""></div>
    //                <div class="subtraction" data-v-80a8f564=""></div>
    //                <div class="bear" data-v-80a8f564="">
    //                    <img >看空 1
    //                </div>
    //                <!---->
    //            </div>
    //            <p class="cell share-btn" data-v-80a8f564=""><em data-v-80a8f564=""></em>分享
    //            </p>
    //            <!---->
    //        </div>
    //    </div>
    // </div>

  }

  @Test
  public void start() throws InterruptedException {

    NewsTodayMoonSpider spider = new NewsTodayMoonSpider();

    Thread.sleep(125_000);

    // 1577166450
    // 1577166926136

    System.out.println("\n\n-----------------\n\n");
    System.out.println(spider.latest(5));
  }
}
