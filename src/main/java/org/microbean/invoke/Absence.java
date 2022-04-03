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

import java.util.NoSuchElementException;

/**
 * An {@link OptionalSupplier} implementation that indicates permanent
 * absence.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class Absence<T> implements OptionalSupplier<T> {


  /*
   * Static fields.
   */


  private static final Absence<?> INSTANCE = new Absence<Void>();


  /*
   * Constructors.
   */


  private Absence() {
    super();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns {@link Determinism#ABSENT} when invoked.
   *
   * @return {@link Determinism#ABSENT} when invoked
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // OptionalSupplier<T>
  public final Determinism determinism() {
    return Determinism.ABSENT;
  }

  /**
   * Throws a {@link NoSuchElementException} when invoked.
   *
   * @return nothing
   *
   * @exception NoSuchElementException when invoked
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // OptionalSupplier<T>
  public final T get() {
    throw new NoSuchElementException();
  }


  /*
   * Static methods.
   */


  /**
   * Returns the sole instance of this class.
   *
   * @param <T> the type of the nonexistent value the returned {@link
   * Absence} will never {@linkplain #get() supply}
   *
   * @return the sole instance of this class
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @SuppressWarnings("unchecked")
  public static final <T> Absence<T> instance() {
    return (Absence<T>)INSTANCE;
  }

}
