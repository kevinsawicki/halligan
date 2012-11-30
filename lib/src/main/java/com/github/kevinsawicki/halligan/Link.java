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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.damnhandy.uri.template.UriTemplate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Link properties
 */
public class Link implements Serializable {

  private static final long serialVersionUID = -3017682664576119547L;

  private String getValue(final String name, final Map<String, Object> values) {
    final Object value = values.remove(name);
    return value != null ? value.toString() : null;
  }

  /**
   * A URI that can be templated
   */
  public final String href;

  /**
   * Language of the target resource
   */
  public final String hreflang;

  /**
   * Secondary key
   */
  public final String name;

  /**
   * A URI that hints about the profile of the target resource
   */
  public final String profile;

  /**
   * True if the {@link #href} field is templated, false otherwise
   */
  public final boolean templated;

  /**
   * Human-readable label for the {@link #href}
   */
  public final String title;

  /**
   * Media-type hint for the target resource
   */
  public final String type;

  private final Map<String, Object> properties = new HashMap<String, Object>();

  /**
   * Create link with values
   *
   * @param href
   * @param hreflang
   * @param name
   * @param profile
   * @param templated
   * @param title
   * @param type
   */
  public Link(final String href, final String hreflang, final String name,
      final String profile, final boolean templated, final String title,
      final String type) {
    this.href = href;
    this.hreflang = hreflang;
    this.name = name;
    this.profile = profile;
    this.templated = templated;
    this.title = title;
    this.type = type;
  }

  /**
   * Create link with values from map
   *
   * @param properties
   */
  public Link(final Map<String, Object> properties) {
    if (properties != null) {
      this.properties.putAll(properties);
      href = getValue("href", this.properties);
      hreflang = getValue("hreflang", this.properties);
      name = getValue("name", this.properties);
      profile = getValue("profile", properties);
      templated = Boolean.valueOf(getValue("templated", this.properties));
      title = getValue("title", this.properties);
      type = getValue("type", this.properties);
    } else {
      this.href = null;
      this.hreflang = null;
      this.name = null;
      this.profile = null;
      this.templated = false;
      this.title = null;
      this.type = null;
    }
  }

  /**
   * Expand templated href with no params
   *
   * @return href with no expanded values if this link if templated, base href
   *         otherwise
   */
  public String expandHref() {
    if (templated)
      return UriTemplate.fromTemplate(href).expand();
    else
      return href;
  }

  /**
   * Expand templated href using given values
   *
   * @param values
   * @return href with values expanded if this link is templated, base href
   *         otherwise
   */
  public String expandHref(final Map<String, Object> values) {
    if (templated)
      return UriTemplate.fromTemplate(href).set(values).expand();
    else
      return href;
  }

  /**
   * Expand templated href using given name and value
   *
   * @param name
   * @param value
   * @return href with name/value expanded if this link is templated, base href
   *         otherwise
   */
  public String expandHref(final String name, final Object value) {
    if (templated)
      return UriTemplate.fromTemplate(href).set(name, value).expand();
    else
      return href;
  }

  /**
   * Expand templated href using given name/value pairs
   *
   * @param values
   * @return href with values expanded if this link is templated, base href
   *         otherwise
   */
  public String expandHref(final Object... values) {
    if (templated) {
      final UriTemplate template = UriTemplate.fromTemplate(href);
      for (int i = 0; i < values.length; i += 2)
        template.set(values[i].toString(), values[i + 1]);
      return template.expand();
    } else
      return href;
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
   * Get link property as an integer
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
   * Get link property as a long
   *
   * @param name
   * @return long value or -1 if the property is missing or not a {@link Number}
   */
  public long getLong(final String name) {
    final Object value = properties.get(name);
    return value instanceof Number ? ((Number) value).longValue() : -1;
  }

  /**
   * Get link property as a boolean
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
   * Get link property as a {@link String}
   *
   * @param name
   * @return string value of property or null if the property is missing
   */
  public String getString(final String name) {
    final Object value = properties.get(name);
    return value != null ? value.toString() : null;
  }

  /**
   * Get link property as a {@link Map}
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

  @Override
  public boolean equals(final Object obj) {
    if (obj == this)
      return true;
    if (!(obj instanceof Link))
      return false;
    if (href == null)
      return false;

    Link other = (Link) obj;
    return templated == other.templated && href.equals(other.href);
  }

  @Override
  public int hashCode() {
    if (href != null)
      if (templated)
        return Arrays.hashCode(new Object[] { href, TRUE });
      else
        return Arrays.hashCode(new Object[] { href, FALSE });
    else
      return super.hashCode();
  }

  @Override
  public String toString() {
    if (href != null)
      return href;
    else
      return super.toString();
  }
}
