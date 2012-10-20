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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests of {@link Resource}
 */
public class ResourceTest extends HalServerTestCase {

  private static String url;

  /**
   * Setup method
   *
   * @throws Exception
   */
  @BeforeClass
  public static void setup() throws Exception {
    url = setUp(new RequestHandler() {

      @Override
      public void handle(Request request, HttpServletResponse response) {
        writeFile("/response.json");
        response.setStatus(HTTP_OK);
      }
    });
  }

  /**
   * Get status code of all resources
   *
   * @throws Exception
   */
  @Test
  public void getCode() throws Exception {
    Resource resource = new Resource(url);
    assertEquals(200, resource.code());
    for (Entry<String, List<Resource>> resources : resource.resources())
      for (Resource child : resources.getValue())
        assertEquals(200, child.code());
  }

  /**
   * Get self link href
   *
   * @throws Exception
   */
  @Test
  public void getSelfUri() throws Exception {
    Resource resource = new Resource(url);
    assertEquals("/orders", resource.selfUri());
  }

  /**
   * Get link to next resource
   *
   * @throws Exception
   */
  @Test
  public void getNextUri() throws Exception {
    Resource resource = new Resource(url);
    assertEquals("/orders?page=2", resource.nextUri());
  }

  /**
   * Get link to find resource
   *
   * @throws Exception
   */
  @Test
  public void getFindLink() throws Exception {
    Resource resource = new Resource(url);
    Link link = resource.link("find");
    assertNotNull(link);
    assertEquals("/orders{?id}", link.href);
    assertTrue(link.templated);
  }

  /**
   * Get resource property as a integer
   *
   * @throws Exception
   */
  @Test
  public void intProperty() throws Exception {
    Resource resource = new Resource(url);
    assertEquals(-1, resource.integer("doesntExist"));
    assertEquals(14, resource.integer("currentlyProcessing"));
    assertEquals(20, resource.integer("shippedToday"));
  }

  /**
   * Get resource property as a map
   *
   * @throws Exception
   */
  @SuppressWarnings("rawtypes")
  @Test
  public void mapProperty() throws Exception {
    Resource resource = new Resource(url);
    assertNull(resource.map("doesntExist"));
    Map<String, Object> map = resource.map("regions");
    assertNotNull(map);
    assertEquals(10D, ((Map) map.get("EMEA")).get("Europe"));
    assertEquals(4D, ((Map) map.get("EMEA")).get("Middle East"));
    assertEquals(1D, ((Map) map.get("EMEA")).get("Africa"));
    assertEquals(5D, map.get("AP"));
  }

  /**
   * Get resource property as a boolean
   *
   * @throws Exception
   */
  @Test
  public void booleanProperty() throws Exception {
    Resource resource = new Resource(url);
    assertFalse(resource.bool("doesntExist"));
    assertTrue(resource.bool("onTime"));
  }

  /**
   * Get embedded resources
   *
   * @throws Exception
   */
  @Test
  public void embeddedResources() throws Exception {
    Resource resource = new Resource(url);
    assertTrue(resource.hasResource("orders"));
    List<Resource> resources = resource.resources("orders");
    assertNotNull(resources);
    assertEquals(2, resources.size());

    Resource order1 = resources.get(0);
    assertEquals(30, order1.integer("total"));
    assertEquals("USD", order1.string("currency"));
    assertEquals("shipped", order1.string("status"));
    assertNull(order1.string("shippedToday"));
    assertEquals("/orders/123", order1.selfUri());
    assertEquals("/baskets/98712", order1.linkUri("basket"));
    assertEquals("/customers/7809", order1.linkUri("customer"));

    Resource order2 = resources.get(1);
    assertEquals(20, order2.integer("total"));
    assertEquals("USD", order2.string("currency"));
    assertEquals("processing", order2.string("status"));
    assertNull(order2.string("shippedToday"));
    assertEquals("/orders/124", order2.selfUri());
    assertEquals("/baskets/97213", order2.linkUri("basket"));
    assertEquals("/customers/12369", order2.linkUri("customer"));
  }

  /**
   * Get embedded resource
   *
   * @throws Exception
   */
  @Test
  public void embeddedResource() throws Exception {
    Resource order = new Resource(url).resource("orders");
    assertNotNull(order);
    assertEquals(30, order.integer("total"));
    assertEquals("USD", order.string("currency"));
    assertEquals("shipped", order.string("status"));
    assertNull(order.string("shippedToday"));
    assertEquals("/orders/123", order.selfUri());
    assertEquals("/baskets/98712", order.linkUri("basket"));
    assertEquals("/customers/7809", order.linkUri("customer"));
  }
}
