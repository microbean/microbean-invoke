/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2022 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.invoke;

import java.util.function.Supplier;

import org.microbean.development.annotation.OverridingEncouraged;

/**
 * A {@link Supplier} that may be <em>deterministic</em>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #deterministic()
 */
@FunctionalInterface
public interface DeterministicSupplier<T> extends Supplier<T> {

  /**
   * Returns {@code true} if and only if this {@link
   * DeterministicSupplier} is <em>deterministic</em>.
   *
   * <p>A {@link DeterministicSupplier} is deterministic if and only
   * if:</p>
   *
   * <ul>
   *
   * <li>Any two invocations of the {@link #get() get()} method return
   * objects that are indistinguishable from one another, or</li>
   *
   * <li>Any two invocations of the {@link #get() get()} method throw
   * {@link RuntimeException}s that are indistinguishable from one
   * another</li>
   *
   * </ul>
   *
   * <p>The default implementation of this method returns {@code
   * false}.</p>
   *
   * @return {@code true} if and only if this {@link
   * DeterministicSupplier} is <em>deterministic</em>
   *
   * @idempotency This method and its overrides must be idempotent and
   * deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #get()
   */
  @OverridingEncouraged
  public default boolean deterministic() {
    return false;
  }

}
