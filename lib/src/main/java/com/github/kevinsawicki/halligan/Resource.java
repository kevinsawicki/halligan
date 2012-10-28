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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Resource class
 */
public class Resource implements Iterable<Resource> {

  private static final Type TYPE_LINKS = new TypeToken<Map<String, Link>>() {
  }.getType();

  private static String getPrefix(final URL url) {
    String prefix = url.getProtocol() + "://" + url.getHost();
    int port = url.getPort();
    if (port != -1)
      return prefix + ':' + port;
    else
      return prefix;
  }

  private transient final Gson gson;

  private final String prefix;

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
    this(new GsonBuilder().create(), url);
  }

  /**
   * Create resource form URL
   *
   * @param gson
   * @param url
   * @throws IOException
   */
  public Resource(final Gson gson, final String url) throws IOException {
    this.gson = gson;

    BufferedReader buffer;
    try {
      HttpRequest request = createRequest(url);
      code = request.code();
      buffer = request.bufferedReader();
      prefix = getPrefix(request.getConnection().getURL());
    } catch (HttpRequestException e) {
      throw e.getCause();
    }

    JsonReader reader = new JsonReader(buffer);
    try {
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
    prefix = parent.prefix;
    this.gson = gson;
    parse(gson, reader);
  }

  /**
   * Create request to URL
   *
   * @param url
   * @return request
   * @throws HttpRequestException
   */
  protected HttpRequest createRequest(final String url)
      throws HttpRequestException {
    return HttpRequest.get(url).accept("application/hal+json");
  }

  private void parse(final Gson gson, final JsonReader reader)
      throws IOException {
    reader.beginObject();
    while (reader.hasNext() && reader.peek() == NAME) {
      String name = reader.nextName();
      if ("_links".equals(name))
        parseLinks(gson, reader);
      else if ("_embedded".equals(name))
        parseResources(gson, reader);
      else
        parseProperty(gson, reader, name);
    }
    reader.endObject();
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
        reader.beginArray();
        List<Resource> entries = new ArrayList<Resource>();
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
   * Get the HTTP status code of the response
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
  public String getSelfUri() {
    return getLinkUri("self");
  }

  /**
   * Get URI to next resource
   *
   * @return URI to next resource or null if no next link exists
   */
  public String getNextUri() {
    return getLinkUri("next");
  }

  /**
   * Get URI to find resource
   *
   * @return URI to find resource or null if no find link exists
   */
  public String getFindUri() {
    return getLinkUri("find");
  }

  /**
   * Get URI of link with name
   *
   * @param name
   * @return URI or null if no link with given name exists
   */
  public String getLinkUri(final String name) {
    final Link link = getLink(name);
    return link != null ? link.href : null;
  }

  /**
   * Get link with name
   *
   * @param name
   * @return link or null if none for given name
   */
  public Link getLink(final String name) {
    return links.get(name);
  }

  /**
   * Get resource property as an integer
   *
   * @param name
   * @return integer value or -1 if the property is missing or not a
   *         {@link Number}
   */
  public int getInt(final String name) {
    final Object value = properties.get(name);
    return value instanceof Number ? ((Number) value).intValue() : -1;
  }

  /**
   * Get resource property as an integer
   *
   * @param name
   * @return integer value or -1 if the property is missing or not a
   *         {@link Number}
   */
  public double getDouble(final String name) {
    final Object value = properties.get(name);
    return value instanceof Number ? ((Number) value).doubleValue() : -1;
  }

  /**
   * Get resource property as a boolean
   *
   * @param name
   * @return boolean value or false if the property is missing or not a
   *         {@link Boolean}
   */
  public boolean getBoolean(final String name) {
    final Object value = properties.get(name);
    return value instanceof Boolean ? ((Boolean) value).booleanValue() : false;
  }

  /**
   * Get resource property as a {@link String}
   *
   * @param name
   * @return string value of property or null if the property is missing
   */
  public String getString(final String name) {
    final Object value = properties.get(name);
    return value != null ? value.toString() : null;
  }

  /**
   * Get resource property as a {@link Map}
   *
   * @param name
   * @return map value of property of null if the property is missing or not a
   *         {@link Map}
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMap(final String name) {
    final Object value = properties.get(name);
    return value instanceof Map ? (Map<String, Object>) value : null;
  }

  /**
   * Get embedded resources with given name
   *
   * @param name
   * @return list of resources
   */
  public List<Resource> getResources(final String name) {
    return resources.get(name);
  }

  /**
   * Get embedded resource with name
   *
   * @param name
   * @return resource
   */
  public Resource getResource(final String name) {
    List<Resource> resources = getResources(name);
    return resources != null && !resources.isEmpty() ? resources.get(0) : null;
  }

  /**
   * Does this resource have a property with the given name?
   *
   * @param name
   * @return true if property exists, false otherwise
   */
  public boolean hasProperty(final String name) {
    return properties.containsKey(name);
  }

  /**
   * Does this resource have embedded resources?
   *
   * @return true if at least one embedded resource, false otherwise
   */
  public boolean hasResources() {
    return !resources.isEmpty();
  }

  /**
   * Does this resource have one or more embedded resources with the given name?
   *
   * @param name
   * @return true if one or more embedded resources exist, false otherwise
   */
  public boolean hasResource(final String name) {
    List<Resource> resources = getResources(name);
    return resources != null && !resources.isEmpty();
  }

  /**
   * Does this resource have a link to the next resource?
   *
   * @return true if link exists for the next resource, false otherwise
   */
  public boolean hasNext() {
    String nextUri = getNextUri();
    return nextUri != null && nextUri.length() > 0;
  }

  /**
   * Load the next resource
   *
   * @return next resource
   * @throws IOException
   */
  public Resource next() throws IOException {
    return new Resource(gson, prefix + getNextUri());
  }

  /**
   * Load this resource using the self URI
   *
   * @return resource loaded from {@link #getSelfUri()} value
   * @throws IOException
   */
  public Resource load() throws IOException {
    return new Resource(gson, prefix + getSelfUri());
  }

  /**
   * Get all embedded resources
   *
   * @return iterator over all embedded resources
   */
  public Iterable<Entry<String, List<Resource>>> getResources() {
    return resources.entrySet();
  }

  /**
   * Create iterator starting at the current resource and advancing down the
   * chain of next links.
   * <p>
   * This returned iterator will return this resource on the first call to
   * {@link Iterator#next()} followed by requesting and parsing the resource
   * defined at this resource's {@link #getNextUri()}
   */
  public Iterator<Resource> iterator() {
    return new ResourceIterator(this);
  }
}
