package com.gobtx.frontend.ws.news;

import com.gobtx.common.executor.GlobalExecutorService;
import com.gobtx.common.executor.GlobalScheduleService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Created by Aaron Kuai on 2019/12/24. */
@Deprecated
public class NewsTodayMoonSpider {

  static final Logger logger = LoggerFactory.getLogger(NewsTodayMoonSpider.class);

  protected final Gson gson = new GsonBuilder().create();

  protected TreeSet<News> latestNews = new TreeSet<>();

  final String str_id = "id"; // 90967,
  final String str_oid = "oid"; // 1657326,
  final String str_sid = "sid"; // 1,
  final String str_highlight = "highlight"; // 0,
  final String str_title = "title"; // xxxx
  final String str_body = "body"; // xxxx
  final String str_urls = "urls"; // "",
  final String str_time = "time"; // 1577166450

  public static final String url = "https://www.todamoon.today/api/express";

  protected final OkHttpClient client;

  public NewsTodayMoonSpider() {
    client = new OkHttpClient();
    GlobalExecutorService.INSTANCE.submit(() -> start());
  }

  AtomicLong failCnt = new AtomicLong(1);

  private void start() {

    RequestBody formBody = new FormBody.Builder().build();

    Request request = new Request.Builder().url(url).post(formBody).build();

    long start = System.currentTimeMillis();
    int loaded = 0;
    String body = null;
    try {

      // if (logger.isDebugEnabled()) {
      //  logger.debug("TRY_FETCH_NEWS");
      // }

      Response response = client.newCall(request).execute();
      // Parse this Json Value
      body = response.body().string();

      final Map data = gson.fromJson(body, Map.class);
      final List payload = (List) data.get("data");
      if (payload != null && !payload.isEmpty()) {

        // System.out.println("\n======================\n");
        // System.out.println(payload.get(0));
        // System.out.println(payload.get(payload.size() - 1));

        if (logger.isDebugEnabled()) {
          logger.debug("NEWS_LOADED  {},{}", payload.get(0), payload.get(payload.size() - 1));
        }

        final long last = latestNews.isEmpty() ? -1 : latestNews.first().id;

        for (final Object row : payload) {
          Map each = (Map) row;

          long id = new BigDecimal(String.valueOf(each.get(str_id))).longValue();

          if (id > last) {

            News news = new News();
            news.setId(id)
                .setOid(new BigDecimal(String.valueOf(each.get(str_oid))).longValue())
                .setSid(new BigDecimal(String.valueOf(each.get(str_sid))).longValue())
                .setTitle(String.valueOf(each.get(str_title)))
                .setBody(String.valueOf(each.get(str_body)))
                .setUrls(String.valueOf(each.get(str_urls)))
                .setTime(new BigDecimal(String.valueOf(each.get(str_time))).longValue());
            loaded++;
            latestNews.add(news);
          } else {
            // System.err.println("WTf " + last + "   " + each);
            break;
          }
        }
      }

      // Do something with the response.
      GlobalScheduleService.INSTANCE.schedule(() -> start(), 75, TimeUnit.SECONDS);
    } catch (Throwable e) {
      logger.warn("FAIL_PULL_DATA {},{}", body, ExceptionUtils.getStackTrace(e));
      GlobalScheduleService.INSTANCE.schedule(
          () -> start(), 75 * failCnt.getAndIncrement(), TimeUnit.SECONDS);
    } finally {
      logger.warn("REFRESH_NEWS_COST {}s {}", (System.currentTimeMillis() - start) / 1_000, loaded);
    }
  }

  public List<News> latest(int size) {
    List<News> res = new ArrayList<>(size);

    final Iterator<News> iterator = latestNews.iterator();
    int cnt = 1;
    while (iterator.hasNext() && (cnt++) <= size) {
      res.add(iterator.next());
    }

    return res;
  }
}
