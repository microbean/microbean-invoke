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

import java.util.function.Supplier;

/**
 * An {@link OptionalSupplier} that tries one {@link Supplier} first,
 * before falling back to another one, while properly implementing the
 * {@link #determinism()} method.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
final class DefaultingOptionalSupplier<T> implements OptionalSupplier<T> {

  private final Supplier<? extends T> defaults;

  private final Supplier<? extends T> supplier;

  private volatile Determinism determinism;

  private DefaultingOptionalSupplier() {
    this(null, null);
  }

  private DefaultingOptionalSupplier(final Supplier<? extends T> supplier) {
    this(supplier, null);
  }

  private DefaultingOptionalSupplier(Supplier<? extends T> supplier, Supplier<? extends T> defaults) {
    super();
    final Determinism determinism;
    if (supplier == null) {
      if (defaults == null) {
        supplier = Absence.instance();
        determinism = Determinism.ABSENT;
      } else if (defaults instanceof OptionalSupplier<? extends T> dos) {
        supplier = defaults;
        defaults = Absence.instance();
        determinism = dos.determinism();
      } else {
        supplier = defaults;
        defaults = Absence.instance();
        determinism = Determinism.NON_DETERMINISTIC;
      }
    } else if (supplier instanceof OptionalSupplier<? extends T> sos) {
      if (defaults == null) {
        determinism = sos.determinism();
      } else if (defaults instanceof OptionalSupplier<? extends T> dos) {
        final Determinism sd = sos.determinism();
        final Determinism dd = dos.determinism();
        if (sd == Determinism.ABSENT) {
          if (dd == Determinism.ABSENT) {
            defaults = Absence.instance();
            determinism = Determinism.ABSENT;
          } else {
            supplier = defaults;
            defaults = Absence.instance();
            determinism = sd;
          }
        } else if (sd == Determinism.PRESENT) {
          defaults = Absence.instance();
          determinism = Determinism.PRESENT;
        } else if (sd == Determinism.DETERMINISTIC) {
          if (dd == Determinism.DETERMINISTIC) {
            determinism = Determinism.DETERMINISTIC;
          } else {
            determinism = Determinism.NON_DETERMINISTIC;
          }
        } else {
          determinism = Determinism.NON_DETERMINISTIC;
        }
      } else {
        determinism = Determinism.NON_DETERMINISTIC;
      }
    } else {
      determinism = Determinism.NON_DETERMINISTIC;
    }
    this.supplier = supplier;
    this.defaults = defaults;
    this.determinism = determinism;
  }

  /**
   * Returns an {@link Determinism} suitable for this {@link
   * DefaultingOptionalSupplier}.
   *
   * @return a {@link Determinism} suitable for this {@link
   * DefaultingOptionalSupplier}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  @Override
  public final Determinism determinism() {
    return this.determinism;
  }

  /**
   * Attempts to return the return value of an invocation of the
   * {@link Supplier#get()} method on the main {@link Supplier}
   * supplied {@linkplain #of(Supplier, Supplier) to one of the
   * factory methods}, unless that invocation causes a {@link
   * NoSuchElementException} or an {@link
   * UnsupportedOperationException} to be thrown, in which case
   * attempts to return the return value of an invocation of the
   * {@link Supplier#get()} method on the default {@link Supplier}
   * supplied {@linkplain #of(Supplier, Supplier) to one of the
   * factory methods}.
   *
   * @return the value in question, which may be {@code null}
   *
   * @exception NoSuchElementException if no value is present
   *
   * @exception UnsupportedOperationException if no value is present
   *
   * @nullability This method may return {@code null} at any point.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #of(Supplier, Supplier)
   */
  @Override
  public final T get() {
    final Determinism d = this.determinism();
    try {
      final T value = this.supplier.get();
      switch (d) {
      case DETERMINISTIC:
        // We were told whatever the supplier does it will forever do.
        // We just didn't know what it would do.  Now we know what it
        // will do: it will always return a value.  Adjust our
        // determinism accordingly.
        this.determinism = Determinism.PRESENT;
        return value;
      case PRESENT:
      case NON_DETERMINISTIC:
        return value;
      case ABSENT:
        throw new IllegalStateException();
      default:
        throw new AssertionError();
      }
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      switch (d) {
      case NON_DETERMINISTIC:
        return this.defaults.get();
      case DETERMINISTIC:
        try {
          return this.defaults.get();
        } catch (final NoSuchElementException | UnsupportedOperationException e2) {
          this.determinism = Determinism.ABSENT;
          throw e2;
        }
      case ABSENT:
      case PRESENT:
        throw new IllegalStateException();
      default:
        throw new AssertionError();
      }
    }
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link DefaultingOptionalSupplier} whose {@link #determinism()}
   * method will always return {@link Determinism#ABSENT} and whose
   * {@link #get()} method will always throw a {@link
   * NoSuchElementException} or an {@link
   * UnsupportedOperationException}.
   *
   * @param <T> the type of object the returned {@link
   * DefaultingOptionalSupplier} will {@linkplain #get() supply}
   *
   * @return a {@link DefaultingOptionalSupplier} whose {@link #determinism()}
   * method will always return {@link Determinism#ABSENT} and whose
   * {@link #get()} method will always throw a {@link
   * NoSuchElementException} or an {@link
   * UnsupportedOperationException}
   *
   * @nullability This method will never return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Absence#instance()
   *
   * @see #get()
   *
   * @see #determinism()
   */
  public static final <T> DefaultingOptionalSupplier<T> of() {
    return new DefaultingOptionalSupplier<>(null, null);
  }

  /**
   * Returns a {@link DefaultingOptionalSupplier} with no defaults.
   *
   * @param <T> the type of object the returned {@link
   * DefaultingOptionalSupplier} will {@linkplain #get() supply}
   *
   * @param supplier the {@link Supplier} in question; may be {@code
   * null} in which case a {@link DefaultingOptionalSupplier} that behaves
   * identically to an {@link Absence} instance will be returned
   *
   * @return a {@link DefaultingOptionalSupplier} with no defaults
   *
   * @nullability This method will never return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see Absence#instance()
   *
   * @see #get()
   *
   * @see #determinism()
   */
  public static final <T> DefaultingOptionalSupplier<T> of(final Supplier<? extends T> supplier) {
    return new DefaultingOptionalSupplier<>(supplier, null);
  }

  /**
   * Returns a {@link DefaultingOptionalSupplier} that will, loosely speaking,
   * try the supplied {@code supplier} first, and, if it throws a
   * {@link NoSuchElementException} or an {@link
   * UnsupportedOperationException} from its {@link Supplier#get()}
   * method, will then try the supplied {@code defaults}.
   *
   * <p>The supplied {@link Supplier} instances can, themselves, be
   * {@link OptionalSupplier}s, and, if so, the resulting {@link
   * DefaultingOptionalSupplier} will have its {@link #determinism()} method
   * appropriately implemented.</p>
   *
   * @param <T> the type of object the returned {@link
   * DefaultingOptionalSupplier} will {@linkplain #get() supply}
   *
   * @param supplier the first {@link Supplier} to try; may be {@code null}
   *
   * @param defaults the fallback {@link Supplier} to try; may be
   * {@code null}
   *
   * @return a {@link DefaultingOptionalSupplier}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   *
   * @see #get()
   *
   * @see #determinism()
   */
  public static final <T> DefaultingOptionalSupplier<T> of(final Supplier<? extends T> supplier, final Supplier<? extends T> defaults) {
    return new DefaultingOptionalSupplier<>(supplier, defaults);
  }

}
