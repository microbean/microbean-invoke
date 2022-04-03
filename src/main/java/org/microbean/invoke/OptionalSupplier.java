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
 * A {@link Supplier} with additional contractual requirements.
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
 * @see #determinism()
 *
 * @see #get()
 *
 * @see #optional()
 */
@FunctionalInterface
public interface OptionalSupplier<T> extends Supplier<T> {

  /**
   * Returns either the result of invoking the {@link #get()} method,
   * if a {@link RuntimeException} does not occur, or the result of
   * invoking the {@link Function#apply(Object) apply(Object)} method
   * on the supplied {@code handler} with any {@link RuntimeException}
   * that does occur, including {@link NoSuchElementException} and
   * {@link UnsupportedOperationException} instances that are used to
   * indicate value absence.
   *
   * @param handler the exception handler; must not be {@code null}
   *
   * @return either the result of invoking the {@link #get()} method,
   * if a {@link RuntimeException} does not occur, or the result of
   * invoking the {@link Function#apply(Object) apply(Object)} method
   * on the supplied {@code handler} with any {@link RuntimeException}
   * that does occur
   *
   * @exception NullPointerException if {@code handler} is {@code
   * null}
   *
   * @nullability This method and its (discouraged) overrides may
   * return {@code null}
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method itself is, and its (discouraged)
   * overrides must be, safe for concurrent use by multiple threads,
   * but the {@link Function#apply(Object) apply(Object)} method of
   * the supplied {@code handler} may not be.
   *
   * @see #get()
   */
  @OverridingDiscouraged
  public default T exceptionally(final Function<? super RuntimeException, ? extends T> handler) {
    try {
      return this.get();
    } catch (final RuntimeException e) {
      try {
        return handler.apply(e);
      } catch (final RuntimeException r) {
        r.addSuppressed(e);
        throw r;
      }
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
   * <li><strong>It is deliberately and explicitly permitted for
   * implementations of this method to return {@code null} for any
   * reason.</strong> Callers of this method must be appropriately
   * prepared.</li>
   *
   * <li>If an invocation of the {@link #determinism()} method, on any
   * thread, returns any of {@link Determinism#ABSENT}, {@link
   * Determinism#DETERMINISTIC} or {@link Determinism#PRESENT}, then
   * additional requirements apply to the implementation of this
   * method; see the {@link #determinism()} method documentation for
   * details.</li>
   *
   * <li>An implementation of this method need not be deterministic if
   * an invocation of the {@link #determinism()} method returns {@link
   * Determinism#NON_DETERMINISTIC}.</li>
   *
   * <li>An implementation of this method may indicate the (possibly
   * transitory) absence of a value by any of the following means:
   *
   * <ul>
   *
   * <li>Throwing a {@link NoSuchElementException}.</li>
   *
   * <li>Throwing an {@link UnsupportedOperationException}.</li>
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
   * @nullability Implementations of this method may, and often will,
   * return {@code null}, even in the normal course of events.
   *
   * @threadsafety Implementations of this method must be safe for
   * concurrent use by multiple threads.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.  An ideal implementation should be idempotent.  If
   * the {@link #determinism() determinism()} method returns a {@link
   * Determinism} that is not {@link Determinism#NON_DETERMINISTIC}, then any
   * implementation of this method must be deterministic.
   *
   * @see #determinism()
   */
  @Override
  public T get();

  /**
   * Returns the result of invoking the {@link
   * BiFunction#apply(Object, Object) apply(Object, Object)} method on
   * the supplied {@code handler}, supplying it with the result of an
   * invocation of the {@link #get()} method, which may be {@code
   * null}, or any {@link RuntimeException} that an invocation of the
   * {@link #get()} method throws, including {@link
   * NoSuchElementException} and {@link UnsupportedOperationException}
   * instances that are used to indicate value absence.
   *
   * <p>The first argument to the handler will be the return value of
   * the {@link #get()} method, which may be {@code null}.  The second
   * argument to the handler will be any {@link RuntimeException} that
   * was thrown during the invocation of the {@link #get()} method,
   * including {@link NoSuchElementException} and {@link
   * UnsupportedOperationException} instances that are used to
   * indicate value absence.</p>
   *
   * @param <U> the type of the value returned
   *
   * @param handler the handler; must not be {@code null}
   *
   * @return the result of invoking the {@link
   * BiFunction#apply(Object, Object) apply(Object, Object)} method on
   * the supplied {@code handler}, supplying it with the result of an
   * invocation of the {@link #get()} method, which may be {@code
   * null}, or any {@link RuntimeException} that an invocation of the
   * {@link #get()} method throws, including {@link
   * NoSuchElementException} and {@link UnsupportedOperationException}
   * instances that are used to indicate value absence
   *
   * @exception NullPointerException if {@code handler} is {@code
   * null}
   *
   * @nullability This method and its (discouraged) overrides may
   * return {@code null}.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @see #get()
   */
  @OverridingDiscouraged
  public default <U> U handle(final BiFunction<? super T, ? super RuntimeException, ? extends U> handler) {
    T value;
    try {
      value = this.get();
    } catch (final RuntimeException e) {
      try {
        return handler.apply(null, e);
      } catch (final RuntimeException r) {
        r.addSuppressed(e);
        throw r;
      }
    }
    return handler.apply(value, null);
  }

  /**
   * Invokes the {@link Consumer#accept(Object) accept(Object)} method
   * on the supplied {@code action} with the return value of an
   * invocation of the {@link #get()} method, which may be {@code
   * null}, unless the {@link #get()} method throws either a {@link
   * NoSuchElementException} or an {@link
   * UnsupportedOperationException}, indicating value absence, in
   * which case no action is taken.
   *
   * @param action the {@link Consumer} representing the action to
   * take; must not be {@code null}
   *
   * @exception NullPointerException if {@code action} is {@code null}
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads, but the
   * supplied {@link Consumer}'s {@link Consumer#accept(Object)
   * accept(Object)} method may not be
   *
   * @see #get()
   */
  @Convenience
  @OverridingDiscouraged
  public default void ifPresent(final Consumer<? super T> action) {
    T value;
    try {
      value = this.get();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      return;
    }
    action.accept(value);
  }

  /**
   * Invokes the {@link Consumer#accept(Object) accept(Object)} method
   * on the supplied {@code presentAction} with the return value of an
   * invocation of the {@link #get()} method, which may be {@code
   * null}, or, if the {@link #get()} method indicates value absence
   * by throwing either a {@link NoSuchElementException} or an {@link
   * UnsupportedOperationException}, invokes the {@link Runnable#run()
   * run()} method of the supplied {@code absentAction} instead.
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
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads, but the
   * supplied {@link Consumer}'s {@link Consumer#accept(Object)
   * accept(Object)} method may not be and the supplied {@link
   * Runnable}'s {@link Runnable#run() run()} method may not be
   *
   * @see #get()
   */
  @Convenience
  @OverridingDiscouraged
  public default void ifPresentOrElse(final Consumer<? super T> presentAction, final Runnable absentAction) {
    T value;
    try {
      value = this.get();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      try {
        absentAction.run();
        return;
      } catch (final RuntimeException r) {
        r.addSuppressed(e);
        throw r;
      }
    }
    presentAction.accept(value);
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
   * @nullability The default implementation of this method does not,
   * and its (discouraged) overrides must not, return {@code null}.
   *
   * @threadsafety The default implementation of this method is, and
   * its (discouraged) overrides must be, safe for concurrent use by
   * multiple threads.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
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
   * @nullability This method does not, and its (discouraged)
   * overrides must not, return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads.
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
   * @nullability This method does not, and its (discouraged)
   * overrides must not, return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads.
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
   * Returns the result of invoking the {@link #get()} method, which
   * may be {@code null}, or, if the {@link #get()} method indicates
   * value absence by throwing either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, returns the supplied
   * alternate value, which may be {@code null}.
   *
   * @param other the alternate value; may be {@code null}
   *
   * @return the result of invoking the {@link #get()} method, which
   * may be {@code null}, or, if the {@link #get()} method indicates
   * value absence by throwing either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, returns the supplied
   * alternate value, which may be {@code null}
   *
   * @nullability This method and its (discouraged) overrides may
   * return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads.
   *
   * @see #get()
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
   * Returns the result of invoking the {@link #get()} method, which
   * may be {@code null}, or, if the {@link #get()} method indicates
   * value absence by throwing either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, returns the result
   * of invoking the {@link #get()} method on the supplied {@link
   * Supplier}, which may be {@code null}.
   *
   * @param supplier the alternate value {@link Supplier}; must not be
   * {@code null}
   *
   * @return the result of invoking the {@link #get()} method, which
   * may be {@code null}, or, if the {@link #get()} method indicates
   * value absence by throwing either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, returns the result
   * of invoking the {@link #get()} method on the supplied {@link
   * Supplier}, which may be {@code null}
   *
   * @exception NullPointerException if {@code supplier} is {@code
   * null}
   *
   * @nullability This method and its (discouraged) overrides may
   * return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * msut be, safe for concurrent use by multiple threads, but the
   * {@link Supplier#get() get()} method of the supplied {@code supplier}
   * may not be
   *
   * @see #get()
   */
  @Convenience
  @OverridingDiscouraged
  public default T orElseGet(final Supplier<? extends T> supplier) {
    try {
      return this.get();
    } catch (final NoSuchElementException | UnsupportedOperationException e) {
      try {
        return supplier.get();
      } catch (final RuntimeException r) {
        r.addSuppressed(e);
        throw r;
      }
    }
  }

  /**
   * Returns the result of invoking the {@link #get()} method, which
   * may be {@code null}.
   *
   * @return the result of invoking the {@link #get()} method, which
   * may be {@code null}
   *
   * @exception NoSuchElementException if value absence was indicated
   * by the {@link #get()} method
   *
   * @nullability This method and its (discouraged) overrides may
   * return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads.
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
   * Returns the result of invoking the {@link #get()} method, which
   * may be {@code null}, or, if the {@link #get()} method indicates
   * value absence by throwing either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, throws the return
   * value of an invocation of the supplied {@link Supplier}'s {@link
   * Supplier#get() get()} method.
   *
   * @param <X> the type of {@link Throwable} the supplied {@code
   * throwableSupplier} {@linkplain Supplier#get() supplies}
   *
   * @param throwableSupplier the {@link Throwable} {@link Supplier};
   * must not be {@code null}
   *
   * @return the result of invoking the {@link #get()} method, which
   * may be {@code null}, or, if the {@link #get()} method indicates
   * value absence by throwing either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, throws the return
   * value of an invocation of the supplied {@link Supplier}'s {@link
   * Supplier#get() get()} method
   *
   * @exception NullPointerException if {@code supplier} is {@code
   * null}
   *
   * @exception X (actually {@code <X>}) if the {@link #get()} method
   * throws either a {@link NoSuchElementException} or an {@link
   * UnsupportedOperationException}
   *
   * @nullability This method and its (discouraged) overrides may
   * return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method itself is, and its (discouraged)
   * overrides must be, safe for concurrent use by multiple threads.
   *
   * @see #get()
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
   * Returns an {@link Determinism} denoting the presence of values
   * returned by this {@link OptionalSupplier}'s {@link #get() get()}
   * method.
   *
   * <p>Overrides of this method must be compatible with the
   * implementation of the {@link #get()} method.  Specifically:</p>
   *
   * <ul>
   *
   * <li>If an override of this method returns {@link
   * Determinism#PRESENT}, then the implementation of the {@link #get()}
   * method must never throw either a {@link NoSuchElementException}
   * or an {@link UnsupportedOperationException}, and, for any two
   * invocations, on any thread, must return either the same object, or
   * objects that are indistinguishable from one another and that can
   * be substituted for each other interchangeably.</li>
   *
   * <li>If an override of this method returns {@link
   * Determinism#ABSENT}, then it is expected that any two invocations of
   * the {@link #get()} method, on any thread, will each throw either
   * a {@link NoSuchElementException} or an {@link
   * UnsupportedOperationException}.  If an implementation of the
   * {@link #get()} method instead returns a value, contrary to these
   * requirements, then the value must be treated as irrelevant or
   * undefined.</li>
   *
   * <li>If an override of this method returns {@link
   * Determinism#DETERMINISTIC}, then any two invocations of the {@link
   * #get()} method implementation, on any thread, must either throw
   * either a {@link NoSuchElementException} or an {@link
   * UnsupportedOperationException}, or must return either the same
   * object, or objects that are indistinguishable from one another
   * and that can be substituted for each other interchangeably.</li>
   *
   * <li>If an override of this method returns {@link
   * Determinism#NON_DETERMINISTIC}, then there are no additional requirements
   * placed upon the implementation of the {@link #get()} method
   * besides those defined in {@linkplain #get() its existing
   * contract}.</li>
   *
   * </ul>
   *
   * <p>Additionally, once an override of this method returns a {@link
   * Determinism} whose {@link Determinism#deterministic() deterministic()}
   * method returns {@code true}, then it must forever afterwards, for
   * all invocations, on all possible threads, return the same {@link
   * Determinism}.</p>
   *
   * <p>If any of the requirements above is not met, undefined
   * behavior may result.</p>
   *
   * <p>The default implementation of this method returns {@link
   * Determinism#NON_DETERMINISTIC}.  Overrides are strongly encouraged.</p>
   *
   * @return an {@link Determinism} denoting the presence of values
   * returned by this {@link OptionalSupplier}'s {@link #get() get()}
   * method
   *
   * @nullability This method does not, and its (encouraged) overrides
   * must not, return {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.  Its
   * (encouraged) overrides must be idempotent.  They may not be
   * deterministic (see above).
   *
   * @threadsafety This method is, and its (encouraged) overrides must
   * be, safe for concurrent use by multiple threads.
   */
  @OverridingEncouraged
  public default Determinism determinism() {
    return Determinism.NON_DETERMINISTIC;
  }

  /**
   * Returns the result of invoking the {@link Stream#of(Object)}
   * method on the return value of an invocation of the {@link #get()}
   * method, which may be {@code null}, or, if the {@link #get()}
   * method indicates value absence by throwing either a {@link
   * NoSuchElementException} or an {@link
   * UnsupportedOperationException}, returns the result of invoking
   * the {@link Stream#empty()} method.
   *
   * <p>Note that this means the sole element of the {@link Stream}
   * that is returned may be {@code null}.  If this is undesirable,
   * consider invoking {@link Optional#stream() stream()} on the
   * return value of the {@link #optional()} method instead.</p>
   *
   * @return the result of invoking the {@link Stream#of(Object)}
   * method on the return value of an invocation of the {@link #get()}
   * method, which may be {@code null}, or, if the {@link #get()}
   * method indicates value absence by throwing either a {@link
   * NoSuchElementException} or an {@link
   * UnsupportedOperationException}, returns the result of invoking
   * the {@link Stream#empty()} method
   *
   * @nullability This method does not, and its (discouraged)
   * overrides must not, return {@code null}.
   *
   * @idempotency No guarantees are made about idempotency or
   * determinism.
   *
   * @threadsafety This method is, and its (discouraged) overrides
   * must be, safe for concurrent use by multiple threads.
   *
   * @see #get()
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


  /*
   * Static methods.
   */


  private static <T> T returnNull(final RuntimeException e) {
    if (e instanceof NoSuchElementException || e instanceof UnsupportedOperationException) {
      return null;
    } else {
      throw e;
    }
  }

  /**
   * Returns a new {@link OptionalSupplier} whose {@link #determinism()}
   * method will return the supplied {@link Determinism} and whose {@link
   * #get()} method will return the result of invoking the {@link
   * Supplier#get()} method on the supplied {@code supplier}.
   *
   * @param <T> the type of value the returned {@link
   * OptionalSupplier} will {@linkplain #get() supply}
   *
   * @param determinism the {@link Determinism} to use; must not be {@code null}
   *
   * @param supplier the {@link Supplier} whose {@link #get()} method
   * will be called; must not be {@code null}
   *
   * @return a new {@link OptionalSupplier} whose {@link #determinism()}
   * method will return the supplied {@link Determinism} and whose {@link
   * #get()} method will return the result of invoking the {@link
   * Supplier#get()} method on the supplied {@code supplier}
   *
   * @exception NullPointerException if {@code determinism} or {@code
   * supplier} is {@code null}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is not idempotent but is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static <T> OptionalSupplier<T> of(final Determinism determinism, final Supplier<? extends T> supplier) {
    Objects.requireNonNull(determinism);
    Objects.requireNonNull(supplier);
    return new OptionalSupplier<>() {
      @Override // OptionalSupplier<T>
      public final Determinism determinism() {
        return determinism;
      }
      @Override // OptionalSupplier<T>
      public final T get() {
        return supplier.get();
      }
    };
  }

  /**
   * Invokes {@link Absence#instance()} and returns the result.
   *
   * @param <T> the type of the nonexistent value the returned {@link
   * OptionalSupplier} will never {@linkplain #get() supply}
   *
   * @return the result of invoking {@link Absence#instance()}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is idempotent and deterministic.
   *
   * @threadsafety This method is safe for concurrent use by
   * multiple threads.
   *
   * @see Absence#instance()
   */
  public static <T> OptionalSupplier<T> of() {
    return Absence.instance();
  }

  /**
   * Returns an {@link OptionalSupplier} representing the supplied
   * {@link Supplier}.
   *
   * @param <T> the type of value the returned {@link
   * OptionalSupplier} will {@linkplain #get() supply}
   *
   * @param supplier the {@link Supplier}; may be {@code null} in
   * which case the return value of an invocation of {@link
   * Absence#instance()} will be returned
   *
   * @return an {@link OptionalSupplier} representing the supplied
   * {@link Supplier}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is not idempotent but is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by
   * multiple threads.
   */
  public static <T> OptionalSupplier<T> of(final Supplier<T> supplier) {
    if (supplier == null) {
      return Absence.instance();
    } else if (supplier instanceof OptionalSupplier<T> os) {
      return os;
    } else {
      return new OptionalSupplier<>() {
        @Override
        public final T get() {
          return supplier.get();
        }
      };
    }
  }

  /**
   * Returns a new {@link OptionalSupplier} whose {@link #determinism()}
   * method will return {@link Determinism#PRESENT} and whose {@link
   * #get()} method will return the supplied {@code value}.
   *
   * @param <T> the type of value the returned {@link
   * OptionalSupplier} will {@linkplain #get() supply}
   *
   * @param value the value the new {@link OptionalSupplier} will
   * return from its {@link #get()} method; may be {@code null}
   *
   * @return a new {@link OptionalSupplier} whose {@link #determinism()}
   * method will return {@link Determinism#PRESENT} and whose {@link
   * #get()} method will return the supplied {@code value}
   *
   * @nullability This method never returns {@code null}.
   *
   * @idempotency This method is not idempotent but is deterministic.
   *
   * @threadsafety This method is safe for concurrent use by multiple
   * threads.
   */
  public static <T> OptionalSupplier<T> of(final T value) {
    return FixedValueSupplier.of(value);
  }


  /*
   * Inner and nested classes.
   */


  /**
   * A token indicating transitory or permanent presence or absence.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see #deterministic()
   *
   * @see OptionalSupplier#determinism()
   */
  public static enum Determinism {

    /**
     * An {@link Determinism} indicating an unknown, possibly
     * transitory, determinism or absence.
     */
    NON_DETERMINISTIC(false),

    /**
     * An {@link Determinism} indicating an unknown permanent presence
     * or absence.
     */
    DETERMINISTIC(true),

    /**
     * An {@link Determinism} indicating permanent absence.
     */
    ABSENT(true),

    /**
     * An {@link Determinism} indicating permanent determinism.
     */
    PRESENT(true);

    private final boolean deterministic;

    private Determinism(final boolean deterministic) {
      this.deterministic = deterministic;
    }

    /**
     * Returns {@code true} if the presence or absence denoted by this
     * {@link Determinism} is deterministic.
     *
     * @return {@code true} if the presence or absence denoted by this
     * {@link Determinism} is deterministic
     *
     * @idempotency This method is idempotent and deterministic.
     *
     * @threadsafety This method is safe for concurrent use by
     * multiple threads.
     */
    public final boolean deterministic() {
      return this.deterministic;
    }

  }

}
