package com.gobtx.frontend.ws.config;

import com.gobtx.common.web.model.request.RequestPacket;
import com.gobtx.model.enums.Exchange;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.*;
import okio.ByteString;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Created by Aaron Kuai on 2019/11/26. */
public class WebSocketBootstrapTest {

  @Test
  public void testPrintRange() {

    final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:MM");

    long[] from = new long[] {1576740302, 1576828500, 1576374720, 1576358220};
    long[] to = new long[] {1576828562, 1576740360, 1576740360, 1576374719};

    int size = from.length;
    for (int i = 0; i < size; i++) {

      System.out.println(
          formatter.format(new Date(from[i] * 1000))
              + "   -   "
              + formatter.format(new Date(to[i] * 1000)));

      System.out.println((to[i] - from[i]) / 60);
    }

    System.out.println((to[0] - from[size - 1]) / 60);
  }

  final String result =
      "{\"code\":200,\"bizCode\":0,\"message\":null,\"data\":[{\"exchange\":\"BINANCE\",\"interval\":\"m1\",\"symbol\":\"EOSUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":2.60120000,\"high\":2.60120000,\"low\":2.60120000,\"close\":2.60120000,\"volume\":0E-8,\"numberOfTrades\":0,\"closeTime\":1576076819999,\"amount\":0E-8,\"timeKey\":201912111506},{\"exchange\":\"BINANCE\",\"interval\":\"m1\",\"symbol\":\"LTCUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":44.10000000,\"high\":44.10000000,\"low\":44.10000000,\"close\":44.10000000,\"volume\":0E-8,\"numberOfTrades\":0,\"closeTime\":1576076819999,\"amount\":0E-8,\"timeKey\":201912111506},{\"exchange\":\"BINANCE\",\"interval\":\"m1\",\"symbol\":\"ETHUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":145.25000000,\"high\":145.26000000,\"low\":145.25000000,\"close\":145.26000000,\"volume\":0.45906000,\"numberOfTrades\":2,\"closeTime\":1576076819999,\"amount\":66.68223100,\"timeKey\":201912111506},{\"exchange\":\"BINANCE\",\"interval\":\"m1\",\"symbol\":\"XRPUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":0.22216000,\"high\":0.22216000,\"low\":0.22216000,\"close\":0.22216000,\"volume\":0E-8,\"numberOfTrades\":0,\"closeTime\":1576076819999,\"amount\":0E-8,\"timeKey\":201912111506},{\"exchange\":\"BINANCE\",\"interval\":\"m1\",\"symbol\":\"BTCUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":7233.97000000,\"high\":7234.90000000,\"low\":7233.97000000,\"close\":7234.90000000,\"volume\":0.17378000,\"numberOfTrades\":4,\"closeTime\":1576076819999,\"amount\":1257.14819903,\"timeKey\":201912111506},{\"exchange\":\"HUOBI\",\"interval\":\"m1\",\"symbol\":\"LTCUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":44.11,\"high\":44.11,\"low\":44.11,\"close\":44.11,\"volume\":0,\"numberOfTrades\":0,\"closeTime\":1576047962853,\"amount\":0,\"timeKey\":201912111506},{\"exchange\":\"HUOBI\",\"interval\":\"m1\",\"symbol\":\"EOSUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":2.602,\"high\":2.602,\"low\":2.602,\"close\":2.602,\"volume\":31.21,\"numberOfTrades\":1,\"closeTime\":1576047967346,\"amount\":11.994619523443506,\"timeKey\":201912111506},{\"exchange\":\"HUOBI\",\"interval\":\"m1\",\"symbol\":\"ETHUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":145.25,\"high\":145.25,\"low\":145.25,\"close\":145.25,\"volume\":4.982075,\"numberOfTrades\":1,\"closeTime\":1576047961829,\"amount\":0.0343,\"timeKey\":201912111506},{\"exchange\":\"HUOBI\",\"interval\":\"m1\",\"symbol\":\"XRPUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":0.22211,\"high\":0.22211,\"low\":0.22211,\"close\":0.22211,\"volume\":0,\"numberOfTrades\":0,\"closeTime\":1576047961500,\"amount\":0,\"timeKey\":201912111506},{\"exchange\":\"HUOBI\",\"interval\":\"m1\",\"symbol\":\"BTCUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":7233.67,\"high\":7233.7,\"low\":7233.67,\"close\":7233.7,\"volume\":13971.47418525,\"numberOfTrades\":5,\"closeTime\":1576047961563,\"amount\":1.931443,\"timeKey\":201912111506},{\"exchange\":\"HUOBI\",\"interval\":\"m1\",\"symbol\":\"BCHUSDT\",\"type\":0,\"openTime\":1576076760000,\"open\":206.58,\"high\":206.58,\"low\":206.58,\"close\":206.58,\"volume\":77.36421,\"numberOfTrades\":1,\"closeTime\":1576047961150,\"amount\":0.3745,\"timeKey\":201912111506}]}\n";

  Gson gson =
      new GsonBuilder()
          // .serializeNulls()
          // .setPrettyPrinting()
          .create();

  @Test
  public void testGsonParser() {

    final Map got = gson.fromJson(result, Map.class);

    System.out.println(got);
  }

  // market/history?symbol=EURJPY&interval=m1&startTime=20000000&limit=200&exchange=binance

  @Test
  public void testClientRestfulHistoryData() throws IOException {
    long startTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(500);

    long start = System.currentTimeMillis();
    final String restURL =
        "https://api.gobtx.com/market/history?symbol=BTCUSDT&interval=m1&startTime="
            + startTime
            + "&limit=200&exchange=binance";

    final OkHttpClient client = new OkHttpClient();

    Request request =
        new Request.Builder().url(restURL).header("User-Agent", "OkHttp Headers.java").build();

    Response response = client.newCall(request).execute();

    System.out.println("Result: " + response.isSuccessful());
    System.out.println("Server: " + response.header("Server"));
    System.out.println("body: " + response.body().string());

    System.out.println("Cost:::::" + (System.currentTimeMillis() - start));
  }

  @Test
  public void testClientRestful22() throws IOException {
    final String restURL = "https://api.gobtx.com/market/news";

    long start = System.currentTimeMillis();
    final OkHttpClient client = new OkHttpClient();

    Request request =
        new Request.Builder().url(restURL).header("User-Agent", "OkHttp Headers.java").build();

    Response response = client.newCall(request).execute();

    System.out.println("Result: " + response.isSuccessful());
    System.out.println("Server: " + response.header("Server"));
    System.out.println(response.body().string());

    System.out.println(System.currentTimeMillis() - start);
  }

  @Test
  public void testClientRestful() throws IOException {
    final String restURL = "https://api.gobtx.com/market/list";

    long start = System.currentTimeMillis();
    final OkHttpClient client = new OkHttpClient();

    Request request =
        new Request.Builder().url(restURL).header("User-Agent", "OkHttp Headers.java").build();

    Response response = client.newCall(request).execute();

    System.out.println("Result: " + response.isSuccessful());
    System.out.println("Server: " + response.header("Server"));
    System.out.println(response.body().string());

    System.out.println(System.currentTimeMillis() - start);
  }

  @Test
  public void testClientWS() throws InterruptedException {

    final String wsURL = "wss://market.gobtx.com/ws";
    // final String wsURL = "ws://localhost:10240/ws";

    final Request request = new Request.Builder().url(wsURL).build();

    final OkHttpClient okHttpclient = new OkHttpClient();

    Gson gson =
        new GsonBuilder()
            // .serializeNulls()
            // .setPrettyPrinting()
            .create();

    Map parameters = new HashMap();

    parameters.put("symbol", "BTCUSDT");
    parameters.put("exchange", Exchange.HUOBI.name());

    RequestPacket requestPacket =
        new RequestPacket().setPath("workshop").setRequestId(1).setParameters(parameters);

    final String value = gson.toJson(requestPacket);
    System.out.println(value);

    CountDownLatch countDownLatch = new CountDownLatch(1);

    WebSocket webSocket =
        okHttpclient.newWebSocket(
            request,
            new WebSocketListener() {
              @Override
              public void onOpen(WebSocket webSocket, Response response) {
                System.out.println("OPING " + response);
                webSocket.send(value);
              }

              @Override
              public void onMessage(WebSocket webSocket, String text) {
                System.out.println("ON MSG " + text);
              }

              @Override
              public void onMessage(WebSocket webSocket, ByteString bytes) {
                System.out.println("ON MSG BYTE " + bytes.toString());
              }

              @Override
              public void onClosing(WebSocket webSocket, int code, String reason) {
                System.out.println("closing " + reason);
              }

              @Override
              public void onClosed(WebSocket webSocket, int code, String reason) {
                System.out.println("closed " + reason);
              }

              @Override
              public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
                System.out.println("fail " + t);
              }
            });

    countDownLatch.await();
  }
}
