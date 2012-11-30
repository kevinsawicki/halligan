/*
 * Copyright (c) 2012 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package com.github.kevinsawicki.halligan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Default {@link Gson} factory that uses the {@link GsonBuilder} default
 * settings
 */
public class DefaultGsonFactory implements GsonFactory, JsonDeserializer<Link> {

  private static final long serialVersionUID = 7213102387291325215L;

  /**
   * Default factory
   */
  public static final DefaultGsonFactory GSON_FACTORY = new DefaultGsonFactory();

  private transient Gson gson;

  /**
   * Create default builder
   *
   * @return builder
   */
  protected GsonBuilder createBuilder() {
    return new GsonBuilder().registerTypeAdapter(Link.class, this);
  }

  public Gson getGson() {
    if (gson == null)
      gson = createBuilder().create();
    return gson;
  }

  public Link deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    @SuppressWarnings("unchecked")
    Map<String, Object> linkProperties = getGson().fromJson(json, Map.class);
    return new Link(linkProperties);
  }
}
