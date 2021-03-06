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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests of {@link Link}
 */
public class LinkTest {

  /**
   * Expand non-templated URI
   */
  @Test
  public void notTemplated() {
    Link link = new Link("/orders", null, null, null, false, null, null);
    assertEquals("/orders", link.expandHref());
    assertEquals("/orders", link.expandHref("a", "b"));
    assertEquals("/orders",
        link.expandHref(Collections.<String, Object> singletonMap("1", "2")));
    assertEquals("/orders", link.expandHref("a", "b", "c", "d"));
  }

  /**
   * Expand templated URI with no values
   */
  @Test
  public void noExpansion() {
    Link link = new Link("/orders{?id}", null, null, null, true, null, null);
    assertEquals("/orders", link.expandHref());
  }

  /**
   * Expand templated URI with single value
   */
  @Test
  public void expandedValue() {
    Link link = new Link("/orders{?id}", null, null, null, true, null, null);
    assertEquals("/orders?id=3", link.expandHref("id", 3));
  }

  /**
   * Expand templated URI with values map
   */
  @Test
  public void expandedValuesMap() {
    Link link = new Link("/avatar{?s,d}", null, null, null, true, null, null);
    Map<String, Object> values = new HashMap<String, Object>();
    values.put("s", 300);
    values.put("d", 404);
    assertEquals("/avatar?s=300&d=404", link.expandHref(values));
  }

  /**
   * Expand templated URI with values varargs
   */
  @Test
  public void expandedValuesVarargs() {
    Link link = new Link("/avatar{?s,d}", null, null, null, true, null, null);
    assertEquals("/avatar?s=200&d=random",
        link.expandHref("s", "200", "d", "random"));
  }

  /**
   * Verify link equality
   */
  @Test
  public void equalLinks() {
    Link l1 = new Link("a", null, null, null, true, null, null);
    assertTrue(l1.equals(l1));
    assertFalse(l1.equals(null));
    assertFalse(l1.equals("a"));

    assertFalse(l1.equals(new Link("a", null, null, null, false, null, null)));
    assertTrue(l1.equals(new Link("a", null, null, null, true, null, null)));
    assertFalse(l1.equals(new Link("b", null, null, null, true, null, null)));

    Link l2 = new Link("a", null, null, null, false, null, null);
    assertTrue(l2.equals(l2));
    assertFalse(l2.equals(null));
    assertFalse(l2.equals("a"));

    assertFalse(l2.equals(new Link("a", null, null, null, true, null, null)));
    assertTrue(l2.equals(new Link("a", null, null, null, false, null, null)));
    assertFalse(l2.equals(new Link("b", null, null, null, true, null, null)));
  }
}
