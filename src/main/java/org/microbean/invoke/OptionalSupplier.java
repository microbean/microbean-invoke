/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2021–2022 microBean™.
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
import java.util.Objects;
import java.util.Optional;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import java.util.stream.Stream;

import org.microbean.development.annotation.Convenience;
import org.microbean.development.annotation.OverridingDiscouraged;
import org.microbean.development.annotation.OverridingEncouraged;

/**
 * A {@link DeterministicSupplier} with additional contractual
 * requirements.
 *
 * <p><strong>An {@link OptionalSupplier} does not behave like an
 * {@link Optional} or a {@link
 * java.util.concurrent.CompletableFuture},</strong> despite the
 * deliberate similarities of some method names.</p>
 *
 * <p>An implementation of this interface is not a <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/doc-files/ValueBased.html">value-based
 * class</a>.</p>
 *
 * @param <T> the type of value implementations of this interface
 * {@linkplain #get() supply}
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #deterministic()
 *
 * @see #get()
 *
 * @see #optional()
 */
@FunctionalInterface
public interface OptionalSupplier<T> extends DeterministicSupplier<T> {

  /**
   * Returns either the result of invoking the {@link #get()} method,
   * if a {@link RuntimeException} does not occur, or the result of
   * invoking the {@link Function#apply(Object)} method on the
   * supplied {@code handler} with any {@link RuntimeException} that
   * does occur.
   *
   * @param handler the exception handler; must not be {@code null}
   *
   * @return either the result of invoking the {@link #get()} method,
   * if a {@link RuntimeException} does not occur, or the result of
   * invoking the {@link Function#apply(Object)} method on the
   * supplied {@code handler} with any {@link RuntimeException} that
   * does occur
   *
   * @exception NullPointerException if {@code handler} is {@code
   * null}
   *
   * @nullability This method may return {@code null}
   *
   * @idempotency This method is not guaranteed to be idempotent or
   * deterministic.
   *
   * @threadsafety This method itself is safe for concurrent use by
   * multiple threads, but the {@link #get()} method may not be, and
   * the {@link Function#apply(Object)} method of the supplied {@code
   * handler} may not be.
   */
  @OverridingDiscouraged
  public default T exceptionally(final Function<? super RuntimeException, ? extends T> handler) {
    try {
      return this.get();
    } catch (final RuntimeException e) {
      return handler.apply(e);
    }
  }

  /**
   * Returns a value, which may be {@code null}.
   *
   * <p>This method's contract extends {@link Supplier#get()}'s
   * contract with the following additional requirements:</p>
   *
   * <ul>
   *
   * <li>An implementation of this method need not be deterministic
   * (unless the {@link #deterministic() deterministic()} method
   * returns {@code true})</li>
   *
   * <li>An implementation of this method may indicate the (possibly
   * transitory) absence of a value by any of the following means:
   *
   * <ul>
   *
   * <li>Throwing a {@link NoSuchElementException}.</li>
   *
   * <li>Throwing an {@link UnsupportedOperationException}.  Normally
   * such an implementation will also return {@code true} from its
   * {@link #deterministic() deterministic()} method.</li>
   *
   * </ul></li>
   *
   * </ul>
   *
   * @return a value, which may be {@code null}
   *
   * @exception NoSuchElementException to indicate (usually
   * transitory) absence
   *
   * @exception UnsupportedOperationException to indicate (usually
   * permanent) absence
   *
   * @nullability Implementations of this method may and often will
   * return {@code null} in the normal course of events.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency Implementations of this method must be idempotent
   * but need not be deterministic.
   *
   * @see #deterministic()
   */
  @Override
  public T get();

  /**
   * Invokes the {@link #get()} method and handles either the result
   * or any {@link RuntimeException}s that are thrown using the
   * supplied {@link BiFunction}.
   *
   * <p>The first argument to the handler will be the return value of
   * the {@link #get()} method, which may be {@code null}.  The second
   * argument to the handler will be any {@link RuntimeException} that
   * was thrown during the invocation of the {@link #get()}
   * method.</p>
   *
   * @param <U> the type of the value returned
   *
   * @param handler the handler; must not be {@code null}
   *
   * @return the return value of the supplied {@code handler}'s {@link
   * BiFunction#apply(Object, Object)} method, which may be {@code
   * null}
   *
   * @nullability The default implementation of this method and its
   * overrides may return {@code null}.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @idempotency No guarantees about either idempotency or
   * determinism are made.
   */
  @OverridingDiscouraged
  public default <U> U handle(final BiFunction<? super T, ? super RuntimeException, ? extends U> handler) {
    try {
      return handler.apply(this.get(), null);
    } catch (final RuntimeException e) {
      return handler.apply(null, e);
    }
  }

  /**
   * Invokes the {@link Consumer#accept(Object)} method on the
   * supplied {@code action} with the return value of an invocation of
   * the {@link #get()} method, unless the {@link #get()} method
   * throws either a {@link NoSuchElementException} or an {@link
   * UnsupportedOperationException}, in which case no action is taken.
   *
   * @param action the {@link Consumer} representing the action to
   * take; must not be {@code null}
   *
   * @exception NullPointerException if {@code action} is {@code null}
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic, but the supplied {@link Consumer}
   * may not be.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads, but the supplied {@link
   * Consumer}'s {@link Consumer#accept(Object)} method may not be
   *
   * @see #get()
   */
  @Convenience
  @OverridingDiscouraged
  public default void ifPresent(final Consumer<? super T> action) {
    try {
      action.accept(this.get());
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
    }
  }

  /**
   * Invokes the {@link Consumer#accept(Object)} method on the
   * supplied {@code presentAction} with the return value of an
   * invocation of the {@link #get()} method, unless the {@link
   * #get()} method throws either a {@link NoSuchElementException} or
   * an {@link UnsupportedOperationException}, in which case the
   * {@link Runnable#run()} method of the supplied {@code
   * absentAction} is invoked instead.
   *
   * @param presentAction the {@link Consumer} representing the action
   * to take if a value is present; must not be {@code null}
   *
   * @param absentAction the {@link Runnable} representing the action
   * to take if a value is absent; must not be {@code null}
   *
   * @exception NullPointerException if {@code action} or {@code
   * absentAction} is {@code null}
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic, but the supplied {@link Consumer}
   * and {@link Runnable} may not be.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads, but the supplied {@link
   * Consumer}'s {@link Consumer#accept(Object)} method may not be and
   * the supplied {@link Runnable}'s {@link Runnable#run()} method may
   * not be
   *
   * @see #get()
   */
  @Convenience
  @OverridingDiscouraged
  public default void ifPresentOrElse(final Consumer<? super T> presentAction, final Runnable absentAction) {
    try {
      presentAction.accept(this.get());
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      absentAction.run();
    }
  }

  /**
   * Returns a non-{@code null} but possibly {@linkplain
   * Optional#isEmpty() empty} {@link Optional} representing this
   * {@link OptionalSupplier}'s {@linkplain #get() value}.
   *
   * <p>The default implementation of this method does not and its
   * overrides must not return {@code null}.</p>
   *
   * <p>The default implementation of this method catches all {@link
   * NoSuchElementException}s and {@link
   * UnsupportedOperationException}s and returns an empty {@link
   * Optional} in these cases, which may not be valid for some use
   * cases.  To detect permanent versus transitory absence, potential
   * callers should use the {@link #get()} method directly or either
   * the {@link #optional(Function)} or {@link #optional(BiFunction)}
   * methods.</p>
   *
   * @return a non-{@code null} but possibly {@linkplain
   * Optional#isEmpty() empty} {@link Optional} representing this
   * {@link OptionalSupplier}'s {@linkplain #get() value}
   *
   * @nullability The default implementation of this method does not
   * and overrides must not return {@code null}.
   *
   * @threadsafety The default implementation of this method is and
   * overrides must be safe for concurrent use by multiple threads.
   *
   * @idempotency The default implementation and overrides of this
   * method may not be idempotent or deterministic.
   *
   * @see #optional(BiFunction)
   *
   * @see #get()
   */
  @Convenience
  @OverridingDiscouraged
  public default Optional<T> optional() {
    return this.optional(OptionalSupplier::returnNull);
  }

  /**
   * Invokes the {@link #handle(BiFunction)} method, supplying it with
   * the supplied {@code handler}, supplies its return value to the
   * {@link Optional#ofNullable(Object)} method, and returns the
   * result.
   *
   * @param <U> the type of the returned {@link Optional}
   *
   * @param handler the handler; must not be {@code null}
   *
   * @return the result of invoking the {@link
   * Optional#ofNullable(Object)} method supplied with the return
   * value of invoking the {@link #handle(BiFunction)} method with the
   * supplied {@code handler}
   *
   * @exception NullPointerException if {@code handler} is {@code
   * null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #handle(BiFunction)
   *
   * @see Optional#ofNullable(Object)
   */
  @Convenience
  @OverridingDiscouraged
  public default <U> Optional<U> optional(final BiFunction<? super T, ? super RuntimeException, ? extends U> handler) {
    return Optional.ofNullable(this.handle(handler));
  }

  /**
   * Invokes the {@link #exceptionally(Function)} method, supplying it
   * with the supplied {@code handler}, supplies its return value to
   * the {@link Optional#ofNullable(Object)} method, and returns the
   * result.
   *
   * @param handler the handler; must not be {@code null}
   *
   * @return the result of invoking the {@link
   * Optional#ofNullable(Object)} method supplied with the return
   * value of invoking the {@link #exceptionally(Function)} method
   * with the supplied {@code handler}
   *
   * @exception NullPointerException if {@code handler} is {@code
   * null}
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #exceptionally(Function)
   *
   * @see Optional#ofNullable(Object)
   */
  @Convenience
  @OverridingDiscouraged
  public default Optional<T> optional(final Function<? super RuntimeException, ? extends T> handler) {
    return Optional.ofNullable(this.exceptionally(handler));
  }

  /**
   * Invokes the {@link Optional#orElse(Object)} method on the return
   * value of the {@link #optional()} method, supplying it with the
   * supplied {@code other}, and returns the result.
   *
   * @param other the alternate value; may be {@code null}
   *
   * @return the result of invoking the {@link
   * Optional#orElse(Object)} method on the return value of the {@link
   * #optional()} method, supplying it with the supplied {@code
   * other}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method itself is safe for concurrent use by
   * multiple threads, but the {@link Supplier#get()} method of the
   * supplied {@code supplier} may not be
   *
   * @see #optional()
   *
   * @see Optional#orElse(Object)
   */
  @Convenience
  @OverridingDiscouraged
  public default T orElse(final T other) {
    try {
      return this.get();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      return other;
    }
  }

  /**
   * Invokes the {@link Optional#orElseGet(Supplier)} method on the
   * return value of the {@link #optional()} method, supplying it with
   * the supplied {@code supplier}, and returns the result.
   *
   * @param supplier the alternate value {@link Supplier}; must not be
   * {@code null}
   *
   * @return the result of invoking the {@link
   * Optional#orElseGet(Supplier)} method on the return value of the
   * {@link #optional()} method, supplying it with the supplied {@code
   * supplier}
   *
   * @exception NullPointerException if {@code supplier} is {@code
   * null}
   *
   * @nullability This method may return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method itself is safe for concurrent use by
   * multiple threads, but the {@link Supplier#get()} method of the
   * supplied {@code supplier} may not be
   *
   * @see #optional()
   *
   * @see Optional#orElseGet(Supplier)
   */
  @Convenience
  @OverridingDiscouraged
  public default T orElseGet(final Supplier<? extends T> supplier) {
    try {
      return this.get();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      return supplier.get();
    }
  }

  /**
   * Invokes the {@link #orElseThrow(Supplier)} method with {@link
   * NoSuchElementException#NoSuchElementException()
   * NoSuchElementException::new} and returns the result.
   *
   * @return the result of invoking the {@link #orElseThrow(Supplier)}
   * method with {@link
   * NoSuchElementException#NoSuchElementException()
   * NoSuchElementException::new}
   *
   * @exception NoSuchElementException if the {@link #optional()}
   * method returns an {@linkplain Optional#isEmpty() empty} {@link
   * Optional}
   *
   * @nullability This method never returns, and its overrides must
   * never return, {@code null}.
   *
   * @idempotency This method is, and its overrides must be, as
   * idempotent and deterministic as the {@link #optional()} method.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads
   *
   * @see #orElseThrow(Supplier)
   */
  @Convenience
  @OverridingDiscouraged
  public default T orElseThrow() {
    try {
      return this.get();
    } catch (final UnsupportedOperationException e) {
      throw new NoSuchElementException(e.getMessage(), e);
    }
  }

  /**
   * Invokes the {@link Optional#orElseThrow(Supplier)} method on the
   * return value of the {@link #optional()} method, supplying it with
   * the supplied {@code throwableSupplier}, and returns the result.
   *
   * @param <X> the type of {@link Throwable} the supplied {@code
   * throwableSupplier} {@linkplain Supplier#get() supplies}
   *
   * @param throwableSupplier the {@link Throwable} {@link Supplier};
   * must not be {@code null}
   *
   * @return the result of invoking the {@link
   * Optional#orElseThrow(Supplier)} method on the return value of the
   * {@link #optional()} method, supplying it with the supplied {@code
   * throwableSupplier}
   *
   * @exception NullPointerException if {@code supplier} is {@code
   * null}
   *
   * @exception X (actually {@code <X>}) if the {@link
   * #optional()} method returns an {@linkplain Optional#isEmpty()
   * empty} {@link Optional} and the {@link
   * Optional#orElseThrow(Supplier)} method throws an exception
   *
   * @nullability This method never returns, and its overrides must
   * never return, {@code null}.
   *
   * @idempotency This method is and its overrides must be idempotent
   * and deterministic.
   *
   * @threadsafety This method itself is, and its overrides must be,
   * safe for concurrent use by multiple threads.
   *
   * @see #optional()
   *
   * @see Optional#orElseThrow(Supplier)
   */
  @Convenience
  @OverridingDiscouraged
  public default <X extends Throwable> T orElseThrow(final Supplier<? extends X> throwableSupplier) throws X {
    try {
      return this.get();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      final X throwable = throwableSupplier.get();
      if (throwable.getCause() == null) {
        throwable.initCause(e);
      } else {
        throwable.addSuppressed(e);
      }
      throw throwable;
    }
  }

  /**
   * Invokes the {@link Optional#stream()} method on the return value
   * of the {@link #optional()} method, and returns the result.
   *
   * @return the result of invoking the {@link
   * Optional#stream()} method on the return value of the
   * {@link #optional()} method
   *
   * @nullability This method does not, and its overrides must not,
   * return {@code null}.
   *
   * @idempotency This method is, and its overrides must be,
   * idempotent and deterministic.
   *
   * @threadsafety This method is, and its overrides must be, safe for
   * concurrent use by multiple threads.
   *
   * @see #optional()
   *
   * @see Optional#stream()
   */
  @Convenience
  @OverridingDiscouraged
  public default Stream<T> stream() {
    try {
      return Stream.of(this.get());
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      return Stream.empty();
    }
  }

  private static <T> T returnNull(final RuntimeException e) {
    if (e instanceof NoSuchElementException || e instanceof UnsupportedOperationException) {
      return null;
    } else {
      throw e;
    }
  }

}
