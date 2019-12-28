package com.gobtx.frontend.ws.netty;

import com.gobtx.common.web.model.response.ResponsePacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/** Created by Aaron Kuai on 2019/11/19. */
public class AcceptorWebSocketApplicationTest {

  public static final String data =
      "{\n"
          + "  \"code\": 13221,\n"
          + "  \"requestId\": 0,\n"
          + "  \"errorMsg\": \"Hello world\",\n"
          + "  \"path\": \"subscribe\",\n"
          + "  \"data\": {\n"
          + "    \"kk\": \"Nov 19, 2019 5:29:13 PM\",\n"
          + "    \"bd\": 12332133.13131231,\n"
          + "    \"paramters\": \"usdjpy.m1\",\n"
          + "    \"bdlist\": [\n"
          + "      1232132132132131.2321313213,\n"
          + "      9999.99999\n"
          + "    ]\n"
          + "  }\n"
          + "}";

  @Test
  public void normalThings() {

    ResponsePacket packet = new ResponsePacket();

    HashMap data = new HashMap();

    data.put("paramters", "usdjpy.m1");

    data.put("kk", new Date());
    data.put("bd", BigDecimal.valueOf(12332133.13131231d));

    data.put(
        "bdlist",
        Arrays.asList(
            new BigDecimal("1232132132132131.2321313213"), BigDecimal.valueOf(9999.99999d)));

    packet.setCode(13221).setErrorMsg("Hello world").setPath("subscribe").setData(data);

    Gson gson =
        new GsonBuilder()
            .setPrettyPrinting()
            // .registerTypeAdapter(BigDecimal.class, new GsonBigDecimalCodec())
            .create();

    String jsonString = gson.toJson(packet);

    System.out.println(jsonString);

    ResponsePacket packetGot = gson.fromJson(jsonString, ResponsePacket.class);

    System.out.println(packetGot.getData().get("bd"));
    System.out.println(packetGot.getData().get("bd").getClass());
    System.out.println(packetGot);
  }

  //
  @Test
  public void testGsonByteBuf() {

    ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(128);
  }
}
