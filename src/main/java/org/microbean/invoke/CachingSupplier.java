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
import java.util.Optional;

import java.util.concurrent.atomic.AtomicReference;

import java.util.function.Supplier;

/**
 * An {@link OptionalSupplier} that computes the value it will
 * return from its {@link #get()} method when that method is first
 * invoked, and that returns that computed value for all subsequent
 * invocations of that method.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @param <T> The type of object returned by the {@link #get()}
 * method
 *
 * @see #CachingSupplier(Object)
 *
 * @see #CachingSupplier(Supplier)
 *
 * @see #get()
 *
 * @see #set(Object)
 */
public final class CachingSupplier<T> implements OptionalSupplier<T> {


  /*
   * Instance fields.
   */


  private final Supplier<? extends T> delegate;

  private final AtomicReference<Optional<T>> ref;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link CachingSupplier}.
   *
   * @see #CachingSupplier(Supplier)
   *
   * @see #get()
   *
   * @see #set(Object)
   *
   * @see
   * AtomicReference#updateAndGet(java.util.function.UnaryOperator)
   */
  public CachingSupplier() {
    this((Supplier<? extends T>)null);
  }

  /**
   * Creates a new {@link CachingSupplier}.
   *
   * <p>An invocation of this constructor will result in the {@link
   * #set(Object)} method always returning {@code false}.</p>
   *
   * @param value the value that will be returned by all invocations
   * of the {@link #get()} method; may be {@code null} in which case
   * the {@link #get()} method will return {@code null} forever
   *
   * @see #get()
   */
  public CachingSupplier(final T value) {
    super();
    final Optional<T> optional = Optional.ofNullable(value);
    this.ref = new AtomicReference<>(optional);
    this.delegate = OptionalSupplier.of(optional.orElse(null));
  }

  /**
   * Creates a new {@link CachingSupplier}.
   *
   * @param supplier the {@link Supplier} that will be used to supply
   * the value that will be returned by all invocations of the {@link
   * #get()} method; may be {@code null} in which case the {@link
   * #get()} method will throw a {@link NoSuchElementException} until,
   * at least, the {@link #set(Object)} method is called; <strong>must
   * be safe for concurrent use by multiple threads and must be
   * side-effect free</strong>
   *
   * @see #get()
   */
  public CachingSupplier(final Supplier<? extends T> supplier) {
    super();
    this.ref = new AtomicReference<>();
    this.delegate = supplier == null ? CachingSupplier::noSuchElementException : supplier;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the value this {@link CachingSupplier} will forever
   * supply, computing it if necessary with the first invocation by
   * using the {@link Supplier} supplied at {@linkplain
   * #CachingSupplier(Supplier) construction time}.
   *
   * <p>If the {@link Supplier} supplied at {@linkplain
   * #CachingSupplier(Supplier) construction time} returns {@code
   * null} from its {@link Supplier#get()} method, then this method
   * will forever return {@code null} as well.</p>
   *
   * @return the value, which may very well be {@code null}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method's idempotency and determinism are
   * determined by the idempotency and determinisim of the {@link
   * Supplier} supplied at {@linkplain #CachingSupplier(Supplier)
   * construction time}.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #CachingSupplier(Object)
   *
   * @see #CachingSupplier(Supplier)
   *
   * @see #set(Object)
   */
  @Override // Supplier<T>
  public final T get() {
    Optional<T> optional = this.ref.get();
    if (optional == null) {
      optional = Optional.ofNullable(this.delegate.get());
      if (!this.ref.compareAndSet(null, optional)) {
        optional = this.ref.get();
      }
    }
    return optional.orElse(null);
  }

  /**
   * Sets the value that will be returned forever afterwards by the
   * {@link #get()} method and returns {@code true} if and only if the
   * value was previously unset.
   *
   * @param newValue the new value that will be returned by the {@link
   * #get()} method forever afterwards; may be {@code null}
   *
   * @return {@code true} if and only if this assignment was permitted;
   * {@code false} otherwise
   *
   * @idempotency This method is idempotent but not deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #get()
   *
   * @see #CachingSupplier(Supplier)
   *
   * @see AtomicReference#compareAndSet(Object, Object)
   */
  public final boolean set(final T newValue) {
    return this.ref.compareAndSet(null, Optional.ofNullable(newValue));
  }

  /**
   * Returns an {@link Determinism} suitable for this {@link CachingSupplier}.
   *
   * <p>In most cases, this method returns {@link
   * Determinism#PRESENT}.  In the case that the {@linkplain
   * #CachingSupplier() zero-argument constructor} was invoked, and
   * the {@link #set(Object)} method has not yet been called, this
   * method will return {@link Determinism#DETERMINISTIC}.  Once the
   * {@link #set(Object)} method has been called, this method will
   * return {@link Determinism#PRESENT}.</p>
   *
   * @return one of {@link Determinism#PRESENT} or {@link
   * Determinism#DETERMINISTIC}
   *
   * @nullability This method does not return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override
  public final Determinism determinism() {
    return this.ref.get() == null ? Determinism.DETERMINISTIC : Determinism.PRESENT;
  }

  /**
   * Returns a non-{@code null} {@link String} representation of this
   * {@link CachingSupplier}.
   *
   * <p>The format of the return value is deliberately unspecified and
   * subject to change without notice from version to version of this
   * class.</p>
   *
   * @return a {@link String} representation of this {@link
   * CachingSupplier}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent but not deterministic
   * until a value has been supplied, either via the {@link
   * #CachingSupplier(Object)} or {@link #CachingSupplier(Supplier)}
   * constructors or the {@link #set(Object)} method.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override // Object
  public final String toString() {
    return String.valueOf(this.get());
  }


  /*
   * Static methods.
   */


  private static final <T> T noSuchElementException() {
    throw new NoSuchElementException();
  }

}
