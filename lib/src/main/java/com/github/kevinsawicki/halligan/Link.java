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

import com.damnhandy.uri.template.UriTemplate;

import java.io.Serializable;
import java.util.Map;

/**
 * Link properties
 */
public class Link implements Serializable {

  private static final long serialVersionUID = -3017682664576119547L;

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

  @Override
  public int hashCode() {
    if (href != null)
      return href.hashCode();
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
