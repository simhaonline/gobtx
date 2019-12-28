package com.gobtx.frontend.ws.news;

import com.gobtx.common.executor.GlobalScheduleService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Created by Aaron Kuai on 2019/12/27. */
public abstract class NewsSpider {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected static final Random random = new Random(System.currentTimeMillis());

  protected long lastId = -1;

  protected TreeSet<News> latestNews = new TreeSet<>();

  protected final AtomicLong failCnt = new AtomicLong();

  protected int getDelayInSeconds() {
    return 120;
  }

  protected int getRandomRange() {
    return 107;
  }

  private static int getRandomNumberInRange(int min, int max) {
    return random.nextInt((max - min) + 1) + min;
  }

  public void fetch() {

    long start = System.currentTimeMillis();
    int loaded = 0;
    try {

      if (logger.isDebugEnabled()) {
        logger.debug("KICK_NEWS_FETCH {}", vendor());
      }
      loaded = doFetch();
      failCnt.set(0);
    } catch (Throwable throwable) {
      logger.error("FAIL_FETCH_NEWS {},{}", vendor(), ExceptionUtils.getStackTrace(throwable));
      failCnt.incrementAndGet();
    } finally {
      int delay = (int) (getDelayInSeconds() * (failCnt.get() + 1));

      GlobalScheduleService.INSTANCE.schedule(
          () -> fetch(), getRandomNumberInRange(delay, delay + getRandomRange()), TimeUnit.SECONDS);

      logger.warn(
          "REFRESH_NEWS_COST {} {}s {}",
          vendor(),
          (System.currentTimeMillis() - start) / 1_000,
          loaded);
    }
  }

  protected abstract int doFetch() throws Exception;

  protected abstract String vendor();

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
