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
    for (Entry<String, List<Resource>> resources : resource.getResources())
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
    assertEquals("/orders", resource.getSelfUri());
  }

  /**
   * Get link to next resource
   *
   * @throws Exception
   */
  @Test
  public void getNextUri() throws Exception {
    Resource resource = new Resource(url);
    assertEquals("/orders?page=2", resource.getNextUri());
  }

  /**
   * Get link to find resource
   *
   * @throws Exception
   */
  @Test
  public void getFindLink() throws Exception {
    Resource resource = new Resource(url);
    assertEquals("/orders{?id}", resource.getFindUri());
    Link link = resource.getLink("find");
    assertNotNull(link);
    assertEquals("/orders{?id}", link.href);
    assertTrue(link.templated);
  }

  /**
   * Get resource property as an integer
   *
   * @throws Exception
   */
  @Test
  public void intProperty() throws Exception {
    Resource resource = new Resource(url);
    assertEquals(-1, resource.getInt("doesntExist"));
    assertEquals(14, resource.getInt("currentlyProcessing"));
    assertEquals(20, resource.getInt("shippedToday"));
  }

  /**
   * Get resource property as a double
   *
   * @throws Exception
   */
  @Test
  public void doubleProperty() throws Exception {
    Resource resource = new Resource(url);
    assertEquals(-1D, resource.getDouble("doesntExist"), 0);
    assertEquals(4.5D, resource.getDouble("ratio"), 0);
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
    assertNull(resource.getMap("doesntExist"));
    Map<String, Object> map = resource.getMap("regions");
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
    assertFalse(resource.getBoolean("doesntExist"));
    assertTrue(resource.getBoolean("onTime"));
  }

  /**
   * Get embedded resources
   *
   * @throws Exception
   */
  @Test
  public void embeddedResources() throws Exception {
    Resource resource = new Resource(url);
    assertTrue(resource.hasResources());
    assertTrue(resource.hasResource("orders"));
    List<Resource> resources = resource.getResources("orders");
    assertNotNull(resources);
    assertEquals(2, resources.size());

    Resource order1 = resources.get(0);
    assertFalse(order1.hasResources());
    assertEquals(30, order1.getInt("total"));
    assertEquals("USD", order1.getString("currency"));
    assertEquals("shipped", order1.getString("status"));
    assertNull(order1.getString("shippedToday"));
    assertEquals("/orders/123", order1.getSelfUri());
    assertEquals("/baskets/98712", order1.getLinkUri("basket"));
    assertEquals("/customers/7809", order1.getLinkUri("customer"));

    Resource order2 = resources.get(1);
    assertFalse(order2.hasResources());
    assertEquals(20, order2.getInt("total"));
    assertEquals("USD", order2.getString("currency"));
    assertEquals("processing", order2.getString("status"));
    assertNull(order2.getString("shippedToday"));
    assertEquals("/orders/124", order2.getSelfUri());
    assertEquals("/baskets/97213", order2.getLinkUri("basket"));
    assertEquals("/customers/12369", order2.getLinkUri("customer"));
  }

  /**
   * Get embedded resource
   *
   * @throws Exception
   */
  @Test
  public void embeddedResource() throws Exception {
    Resource order = new Resource(url).getResource("orders");
    assertNotNull(order);
    assertEquals(30, order.getInt("total"));
    assertEquals("USD", order.getString("currency"));
    assertEquals("shipped", order.getString("status"));
    assertNull(order.getString("shippedToday"));
    assertEquals("/orders/123", order.getSelfUri());
    assertEquals("/baskets/98712", order.getLinkUri("basket"));
    assertEquals("/customers/7809", order.getLinkUri("customer"));
  }
}
