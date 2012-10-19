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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests of iterating over next resource links
 */
public class IterationTest extends HalServerTestCase {

  private static String url;

  /**
   * Setup method
   *
   * @throws Exception
   */
  @BeforeClass
  public static void setup() throws Exception {
    final LinkedList<String> pages = new LinkedList<String>();
    pages.add("/response.json");
    pages.add("/response_next.json");
    url = setUp(new RequestHandler() {

      @Override
      public void handle(Request request, HttpServletResponse response) {
        writeFile(pages.removeFirst());
        response.setStatus(HTTP_OK);
      }
    });
  }

  /**
   * Get the next resource
   *
   * @throws Exception
   */
  @Test
  public void nextResource() throws Exception {
    Resource resource1 = new Resource(url);
    assertTrue(resource1.hasNext());
    Resource resource2 = resource1.next();
    assertNotNull(resource2);
    assertFalse(resource2.hasNext());
    assertEquals(resource1.nextUri(), resource2.selfUri());
    assertEquals(0, resource2.integer("currentlyProcessing"));
    assertEquals(350, resource2.integer("shippedToday"));
  }
}
