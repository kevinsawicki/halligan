/******************************************************************************
 *  Copyright (c) 2012 GitHub Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Kevin Sawicki (GitHub Inc.) - initial API and implementation
 *****************************************************************************/
package com.github.kevinsawicki.halligan;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests of links with additional properties
 */
public class LinkPropertiesTest extends HalServerTestCase {

  /**
   * Setup method
   *
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    handler = new RequestHandler() {

      @Override
      public void handle(Request request, HttpServletResponse response) {
        writeFile("/link_properties.json");
        response.setStatus(HTTP_OK);
      }
    };
  }

  /**
   * Verify link with additional properties
   *
   * @throws Exception
   */
  @Test
  public void linkProperties() throws Exception {
    Resource resource = new Resource(url);
    assertEquals(200, resource.code());

    Link large = resource.getLink("large");
    assertNotNull(large);
    assertEquals(1024, large.getInt("width"));
    assertEquals(902, large.getInt("height"));

    Link medium = resource.getLink("medium");
    assertNotNull(medium);
    assertEquals(640, medium.getInt("width"));
    assertEquals(564, medium.getInt("height"));

    Link small = resource.getLink("small");
    assertNotNull(small);
    assertEquals(210, small.getInt("width"));
    assertEquals(157, small.getInt("height"));
    assertTrue(small.getBoolean("dither"));
    assertEquals("#F00", small.getString("background"));
  }
}
