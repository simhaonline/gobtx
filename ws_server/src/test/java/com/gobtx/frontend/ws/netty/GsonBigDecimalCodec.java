package com.gobtx.frontend.ws.netty;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/** Created by Aaron Kuai on 2019/11/19. */
public class GsonBigDecimalCodec
    implements JsonDeserializer<BigDecimal>,
        JsonSerializer<BigDecimal>,
        InstanceCreator<BigDecimal> {
  @Override
  public BigDecimal createInstance(Type type) {
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    // TODO: 2019/11/19 a this will not work
    return new BigDecimal(json.getAsString());
  }

  @Override
  public JsonElement serialize(BigDecimal src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }
}
