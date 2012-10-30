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

import static com.github.kevinsawicki.halligan.DefaultGsonFactory.GSON_FACTORY;
import static com.google.gson.stream.JsonToken.BEGIN_OBJECT;
import static com.google.gson.stream.JsonToken.NAME;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
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
public class Resource implements Iterable<Resource>, Serializable {

  private static final long serialVersionUID = 8768898492847217862L;

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

  /**
   * Gson factory
   */
  protected final GsonFactory gson;

  private String prefix;

  private int code;

  /**
   * Resource properties
   */
  protected final Map<String, Object> properties = new LinkedHashMap<String, Object>();

  /**
   * Resource links
   */
  protected final Map<String, Link> links = new LinkedHashMap<String, Link>();

  /**
   * Embedded resources
   */
  protected final Map<String, List<Resource>> resources = new LinkedHashMap<String, List<Resource>>();

  /**
   * Create resource from URL
   *
   * @param url
   * @throws IOException
   */
  public Resource(final String url) throws IOException {
    this(GSON_FACTORY, url);
  }

  /**
   * Create resource from URL
   *
   * @param gson
   * @param url
   * @throws IOException
   */
  public Resource(final GsonFactory gson, final String url) throws IOException {
    this(gson);

    parse(url);
  }

  /**
   * Create resource with Gson factory
   *
   * @param gson
   */
  protected Resource(final GsonFactory gson) {
    this.gson = gson;
  }

  /**
   * Create child resource
   *
   * @param parent
   * @param gson
   * @throws IOException
   */
  protected Resource(final Resource parent, final GsonFactory gson)
      throws IOException {
    code = parent.code;
    prefix = parent.prefix;
    this.gson = gson;
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

  /**
   * Create new child resource
   *
   * @return new resource
   * @throws IOException
   */
  protected Resource createResource() throws IOException {
    return new Resource(this, gson);
  }

  /**
   * Create new root-level resource backed by given URL
   *
   * @param url
   * @return new resource
   * @throws IOException
   * @throw IOException
   */
  protected Resource createResource(final String url) throws IOException {
    if (url.length() > 0 && url.charAt(0) == '/')
      return new Resource(prefix + url);
    else
      return new Resource(url);
  }

  private Resource requestResource(String url) throws IOException {
    if (url.length() > 0 && url.charAt(0) == '/')
      url = prefix + url;
    return createResource(url);
  }

  /**
   * Fill this resource by opening a request to the URL and parsing the response
   *
   * @param url
   * @return this resource
   * @throws IOException
   */
  protected Resource parse(final String url) throws IOException {
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
      parse(reader);
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

    return this;
  }

  private Resource parse(final JsonReader reader) throws IOException {
    reader.beginObject();
    while (reader.hasNext() && reader.peek() == NAME) {
      String name = reader.nextName();
      if ("_links".equals(name))
        parseLinks(reader);
      else if ("_embedded".equals(name))
        parseResources(reader);
      else
        parseProperty(reader, name);
    }
    reader.endObject();
    return this;
  }

  /**
   * Parse resources from current value
   *
   * @param reader
   * @throws IOException
   */
  protected void parseResources(final JsonReader reader) throws IOException {
    reader.beginObject();
    while (reader.hasNext()) {
      String name = reader.nextName();
      JsonToken next = reader.peek();
      switch (next) {
      case BEGIN_OBJECT:
        resources.put(name,
            Collections.singletonList(createResource().parse(reader)));
        break;
      case BEGIN_ARRAY:
        reader.beginArray();
        List<Resource> entries = new ArrayList<Resource>();
        while (reader.peek() == BEGIN_OBJECT)
          entries.add(createResource().parse(reader));
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
   * @param reader
   * @param name
   * @throws IOException
   */
  protected void parseProperty(final JsonReader reader, final String name)
      throws IOException {
    JsonToken next = reader.peek();
    switch (next) {
    case BEGIN_OBJECT:
      properties.put(name, gson.getGson().fromJson(reader, Map.class));
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
   * @param reader
   */
  protected void parseLinks(final JsonReader reader) {
    Map<String, Link> links = gson.getGson().fromJson(reader, TYPE_LINKS);
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
   * Get resource property as a long
   *
   * @param name
   * @return long value or -1 if the property is missing or not a {@link Number}
   */
  public long getLong(final String name) {
    final Object value = properties.get(name);
    return value instanceof Number ? ((Number) value).longValue() : -1;
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
    return requestResource(getNextUri());
  }

  /**
   * Load this resource using the self URI
   *
   * @return resource loaded from {@link #getSelfUri()} value
   * @throws IOException
   */
  public Resource load() throws IOException {
    return requestResource(getSelfUri());
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
