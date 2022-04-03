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

/**
 * An {@link OptionalSupplier} implementation that supplies a fixed
 * value.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public final class FixedValueSupplier<T> implements OptionalSupplier<T> {


  private final T value;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link FixedValueSupplier} that will {@linkplain
   * #get() supply} the supplied value.
   *
   * @param value the value to be {@linkplain #get() supplied}; may be
   * {@code null}
   */
  private FixedValueSupplier(final T value) {
    super();
    this.value = value;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns {@link Determinism#PRESENT} when invoked.
   *
   * @return {@link Determinism#PRESENT} when invoked
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
    return Determinism.PRESENT;
  }

  /**
   * Returns the value supplied {@linkplain
   * #FixedValueSupplier(Object) at construction time}, which may be
   * {@code null}.
   *
   * @return the value supplied {@linkplain
   * #FixedValueSupplier(Object) at construction time}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by
   * multiple threads.
   */
  @Override // OptionalSupplier<T>
  public final T get() {
    return this.value;
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link FixedValueSupplier} {@linkplain #get()
   * supplying} the supplied value.
   *
   * @param <T> the type of the value the returned {@link
   * FixedValueSupplier} will {@linkplain #get() supply}
   *
   * @param value the value the {@link #get()} method will return;
   * may be {@code null}
   *
   * @return a {@link FixedValueSupplier} {@linkplain #get()
   * supplying} the supplied value
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by
   * multiple threads.
   */
  public static final <T> FixedValueSupplier<T> of(final T value) {
    return new FixedValueSupplier<>(value);
  }

}
