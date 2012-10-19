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

/**
 * Link properties
 */
public class Link {

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
}
