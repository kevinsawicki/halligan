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

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests of {@link Resource#load()}
 */
public class SelfLoadTest extends HalServerTestCase {

  private static String url;

  private static LinkedList<String> pages = new LinkedList<String>();

  /**
   * Setup server
   *
   * @throws Exception
   */
  @BeforeClass
  public static void setup() throws Exception {
    url = setUp(new RequestHandler() {

      @Override
      public void handle(Request request, HttpServletResponse response) {
        writeFile(pages.removeFirst());
        response.setStatus(HTTP_OK);
      }
    });
  }

  /**
   * Add pages
   */
  @Before
  public void addPages() {
    pages.clear();
    pages.add("/response.json");
    pages.add("/response_self.json");
  }

  /**
   * Get the loaded self resource resource
   *
   * @throws Exception
   */
  @Test
  public void loadSelf() throws Exception {
    Resource root = new Resource(url);
    Resource partialOrder = root.getResource("orders");
    assertNotNull(partialOrder);
    assertFalse(partialOrder.hasProperty("itemCount"));
    assertFalse(partialOrder.hasProperty("coupon"));
    Resource fullOrder = partialOrder.load();
    assertNotNull(fullOrder);
    assertEquals(partialOrder.getSelfUri(), fullOrder.getSelfUri());
    assertEquals(10, fullOrder.getInt("itemCount"));
    assertTrue(fullOrder.getBoolean("coupon"));
  }
}
