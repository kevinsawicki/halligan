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

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator over resources using the next link
 */
public class ResourceIterator implements Iterator<Resource> {

  private Resource resource;

  private boolean advanced;

  /**
   * Create iterator starting at given resource
   * <p>
   * The given resource will be returned on the first call to {@link #next()}
   *
   * @param resource
   */
  public ResourceIterator(final Resource resource) {
    this.resource = resource;
  }

  public boolean hasNext() {
    return !advanced || resource.hasNext();
  }

  public Resource next() {
    if (!hasNext())
      throw new NoSuchElementException("Resource does not have a next link");

    if (advanced)
      try {
        resource = resource.next();
      } catch (IOException e) {
        NoSuchElementException nsee = new NoSuchElementException(
            "Requesting next resource failed");
        nsee.initCause(e);
        throw nsee;
      }
    else
      advanced = true;
    return resource;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
