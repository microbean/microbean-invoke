/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022–2023 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.invoke;

import java.util.Optional;

/**
 * An interface whose implementations can return a {@link CharSequence} for content-based hashing.
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 */
@FunctionalInterface
public interface ContentHashable {

  /**
   * Returns an {@link Optional} whose content is a {@link CharSequence} representing a "content hashable" view of the
   * implementation.
   *
   * <p>If the returned {@link Optional} {@linkplain Optional#isEmpty() is empty}, no "content hashable" view of the
   * implementation exists, and content hashing of this implementation may lead to undefined behavior.</p>
   *
   * <p>Implementations of this method must produce a determinate value, or undefined behavior will result.</p>
   *
   * @return an {@link Optional} whose content is a {@link CharSequence} representing a "content hashable" view of the
   * implementation
   */
  public Optional<? extends CharSequence> contentHashInput();

}
