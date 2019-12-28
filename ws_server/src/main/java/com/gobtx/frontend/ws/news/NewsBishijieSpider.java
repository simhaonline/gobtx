package com.gobtx.frontend.ws.news;

import com.gobtx.common.executor.GlobalExecutorService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

/** Created by Aaron Kuai on 2019/12/27. */
public class NewsBishijieSpider extends NewsSpider {

  public static final String URL = "https://www.bishijie.com/";

  public static final Pattern SPLIT = Pattern.compile(":", Pattern.LITERAL);

  protected static long time2Epoch(final String time) {

    final Calendar calendar = Calendar.getInstance();

    SPLIT.split(time);

    final String[] hm = time.split("");

    int hr = Integer.parseInt(hm[0]);
    int minute = Integer.parseInt(hm[1]);

    calendar.set(Calendar.HOUR, hr);
    calendar.set(Calendar.MINUTE, minute);

    return calendar.toInstant().toEpochMilli() / 1000;
  }

  public NewsBishijieSpider() {
    GlobalExecutorService.INSTANCE.submit(() -> fetch());
  }

  @Override
  protected int doFetch() throws Exception {
    int res = 0;

    Document doc = Jsoup.connect(URL).get();
    Elements newsHeadlines = doc.select(".content");

    long largest = -1;

    for (Element headline : newsHeadlines) {

      try {
        Element newsUrl = headline.selectFirst("a");
        final String newsTarget = newsUrl.absUrl("href");
        final String path = newsTarget.substring(33);
        final String ids = path.substring(0, path.length() - 5);

        // https://www.bishijie.com/kuaixun/513496.html
        final long id = Long.parseLong(ids);

        if (id < lastId) {
          break;
        }

        if (largest < 0) {
          largest = id;
        }

        News news = new News();
        news.setId(id);
        // 15:05
        Element elTime = newsUrl.selectFirst(".newstime");

        String time = elTime.text();

        news.setTime(time2Epoch(time));

        final String title = newsUrl.text().substring(6);
        final String content = headline.selectFirst(".h63").text();

        news.setTitle(title).setBody(content);

        Element imgBox = headline.selectFirst(".image-box");
        if (imgBox != null) {

          Elements imgs = imgBox.select("img");
          if (imgs != null && imgs.size() > 0) {

            List<String> imgUrls = new ArrayList<>(imgs.size());
            for (Element el : imgs) {
              imgUrls.add(el.attr("src"));
            }
            news.setUrls(String.join(",", imgUrls));
          }
        }

        latestNews.add(news);
        if (logger.isDebugEnabled()) {
          logger.debug("SUCCESS_LOAD_NEWS {},{},{}", vendor(), id, title);
        }
        res++;
        // .image-box
        // https://img.bishijie.com/newsflash-5e058b30e80f0
        // https://img.bishijie.com/newsflash-5e058b30e80f0?imageView2/1/w/300/h/168/q/75|imageslim

      } catch (Throwable throwable) {
        logger.warn("FAIL_PARSE_DOC {},{}", vendor(), ExceptionUtils.getStackTrace(throwable));
      }
    }
    if (largest > lastId) {
      lastId = largest;
    }
    return res;
  }

  @Override
  protected String vendor() {
    return "Bishijie";
  }
}
