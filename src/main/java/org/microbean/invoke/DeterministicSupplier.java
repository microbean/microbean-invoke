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

import java.util.Objects;

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
   * <li>Any two invocations of the {@link #get() get()} method, on
   * any thread, return either the same object or objects that are
   * indistinguishable from one another and that can be substituted
   * for each other interchangeably, or</li>
   *
   * <li>Any two invocations of the {@link #get() get()} method, on
   * any thread, throw {@link RuntimeException}s of exactly the same
   * type</li>
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


  /*
   * Static methods.
   */


  /**
   * Returns a new {@link DeterministicSupplier} whose {@link
   * #deterministic()} method will return the supplied {@code
   * deterministic} value and whose {@link #get()} method will return
   * the result of invoking the {@link Supplier#get()} method on the
   * supplied {@code supplier}.
   *
   * @param <T> the type of value the returned {@link
   * DeterministicSupplier} will {@linkplain #get() supply}
   *
   * @param deterministic whether the supplied {@link Supplier} is
   * deterministic
   *
   * @param supplier the {@link Supplier} whose {@link #get()} method
   * will be called; must not be {@code null}
   *
   * @return a new {@link DeterministicSupplier} whose {@link
   * #deterministic()} method will return the supplied {@code
   * deterministic} value and whose {@link #get()} method will return
   * the result of invoking the {@link Supplier#get()} method on the
   * supplied {@code supplier}
   *
   * @exception NullPointerException if {@code supplier} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is not idempotent but is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static <T> DeterministicSupplier<T> of(final boolean deterministic, final Supplier<? extends T> supplier) {
    Objects.requireNonNull(supplier);
    return new DeterministicSupplier<>() {
      @Override // DeterministicSupplier<T>
      public final boolean deterministic() {
        return deterministic;
      }
      @Override // DeterministicSupplier<T>
      public final T get() {
        return supplier.get();
      }
    };
  }

  /**
   * Returns a new {@link DeterministicSupplier} whose {@link
   * #deterministic()} method will return {@code true} and whose
   * {@link #get()} method will return the supplied {@code value}.
   *
   * @param <T> the type of value the returned {@link
   * DeterministicSupplier} will {@linkplain #get() supply}
   *
   * @param value the value the new {@link DeterministicSupplier} will
   * return from its {@link #get()} method; may be {@code null}
   *
   * @return a new {@link DeterministicSupplier} whose {@link
   * #deterministic()} method will return {@code true} and whose
   * {@link #get()} method will return the supplied {@code value}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is not idempotent but is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static <T> DeterministicSupplier<T> of(final T value) {
    return new DeterministicSupplier<>() {
      @Override // DeterministicSupplier<T>
      public final boolean deterministic() {
        return true;
      }
      @Override // DeterministicSupplier<T>
      public final T get() {
        return value;
      }
    };
  }

}
