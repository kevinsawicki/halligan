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

import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.google.gson.stream.JsonToken.NAME;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Resource class
 */
public class Resource {

  private static final Type TYPE_LINKS = new TypeToken<Map<String, Link>>() {
  }.getType();

  private final int code;

  private final Map<String, Object> properties = new LinkedHashMap<String, Object>();

  private final Map<String, Link> links = new LinkedHashMap<String, Link>();

  private final Map<String, List<Resource>> resources = new LinkedHashMap<String, List<Resource>>();

  /**
   * Create resource from URL
   *
   * @param url
   * @throws IOException
   */
  public Resource(final String url) throws IOException {
    BufferedReader buffer;
    try {
      HttpRequest request = HttpRequest.get(url);
      request.accept("application/hal+json");
      code = request.code();
      buffer = request.bufferedReader();
    } catch (HttpRequestException e) {
      throw e.getCause();
    }

    Gson gson = new GsonBuilder().create();
    JsonReader reader = new JsonReader(buffer);
    try {
      reader.beginObject();
      parse(gson, reader);
    } catch (JsonParseException e) {
      IOException ioException = new IOException("JSON parsing failed");
      ioException.initCause(e);
      throw ioException;
    } finally {
      try {
        reader.close();
      } catch (IOException ignored) {
        // Ignored
      }
    }
  }

  private Resource(final Resource parent, final Gson gson,
      final JsonReader reader) throws IOException {
    code = parent.code;
    reader.beginObject();
    parse(gson, reader);
    reader.endObject();
  }

  private void parse(final Gson gson, final JsonReader reader)
      throws IOException {
    while (reader.hasNext() && reader.peek() == NAME) {
      String name = reader.nextName();
      if ("_links".equals(name))
        parseLinks(gson, reader);
      else if ("_embedded".equals(name))
        parseResources(gson, reader);
      else
        parseProperty(gson, reader, name);
    }
  }

  /**
   * Parse resources from current value
   *
   * @param gson
   * @param reader
   * @throws IOException
   */
  protected void parseResources(final Gson gson, final JsonReader reader)
      throws IOException {
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      JsonToken next = reader.peek();
      switch (next) {
      case BEGIN_OBJECT:
        resources.put(name,
            Collections.singletonList(new Resource(this, gson, reader)));
        break;
      case BEGIN_ARRAY:
        List<Resource> entries = new ArrayList<Resource>();
        reader.beginArray();
        while (reader.peek() == BEGIN_OBJECT)
          entries.add(new Resource(this, gson, reader));
        reader.endArray();
        resources.put(name, entries);
        break;
      default:
        throw new IOException("_embedded object value is a " + next.name()
            + " and must be an array or object");
      }
    }
    reader.endObject();
  }

  /**
   * Parse resource property
   *
   * @param gson
   * @param reader
   * @param name
   * @throws IOException
   */
  protected void parseProperty(final Gson gson, final JsonReader reader,
      final String name) throws IOException {
    JsonToken next = reader.peek();
    switch (next) {
    case BEGIN_OBJECT:
      properties.put(name, gson.fromJson(reader, Map.class));
      break;
    case STRING:
      properties.put(name, reader.nextString());
      break;
    case NUMBER:
      properties.put(name, reader.nextDouble());
      break;
    case NULL:
      properties.put(name, null);
      reader.nextNull();
      break;
    case BOOLEAN:
      properties.put(name, reader.nextBoolean());
      break;
    default:
      throw new IOException("Unrecognized property value token: " + next);
    }
  }

  /**
   * Parse links from current reader's next object value
   *
   * @param gson
   * @param reader
   */
  protected void parseLinks(final Gson gson, final JsonReader reader) {
    Map<String, Link> links = gson.fromJson(reader, TYPE_LINKS);
    if (links != null && !links.isEmpty())
      this.links.putAll(links);
  }

  /**
   * Get HTTP status code from response
   *
   * @return code
   */
  public int code() {
    return code;
  }

  /**
   * Get URI to self
   *
   * @return URI to self or null if no self link exists
   */
  public String selfUri() {
    return linkUri("self");
  }

  /**
   * Get URI to next resource
   *
   * @return URI to next resource or null if no next link exists
   */
  public String nextUri() {
    return linkUri("next");
  }

  /**
   * Get URI of link with name
   *
   * @param name
   * @return URI or null if no link with given name exists
   */
  public String linkUri(final String name) {
    final Link link = link(name);
    return link != null ? link.href : null;
  }

  /**
   * Get link with name
   *
   * @param name
   * @return link or null if none for given name
   */
  public Link link(final String name) {
    return links.get(name);
  }

  /**
   * Get resource property as an integer
   *
   * @param name
   * @return integer value or -1 if the property is missing or not a
   *         {@link Number}
   */
  public int integer(final String name) {
    final Object value = properties.get(name);
    return value instanceof Number ? ((Number) value).intValue() : -1;
  }

  /**
   * Get resource property as a boolean
   *
   * @param name
   * @return boolean value or false if the property is missing or not a
   *         {@link Boolean}
   */
  public boolean bool(final String name) {
    final Object value = properties.get(name);
    return value instanceof Boolean ? ((Boolean) value).booleanValue() : false;
  }

  /**
   * Get resource property as a {@link String}
   *
   * @param name
   * @return string value of property or null if the property is missing
   */
  public String string(final String name) {
    final Object value = properties.get(name);
    return value != null ? value.toString() : null;
  }

  /**
   * Get embedded resources with given name
   *
   * @param name
   * @return list of resources
   */
  public List<Resource> resources(final String name) {
    return resources.get(name);
  }

  /**
   * Get all embedded resources
   *
   * @return iterator over all embedded resources
   */
  public Iterable<Entry<String, List<Resource>>> resources() {
    return resources.entrySet();
  }
}
