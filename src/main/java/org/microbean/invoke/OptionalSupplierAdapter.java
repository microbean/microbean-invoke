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

final class OptionalSupplierAdapter<T> implements OptionalSupplier<T> {

  private final Determinism determinism;

  private final Supplier<? extends T> supplier;

  OptionalSupplierAdapter() {
    this(Determinism.ABSENT, Absence.instance());
  }

  OptionalSupplierAdapter(final Supplier<? extends T> supplier) {
    this(Determinism.NON_DETERMINISTIC, supplier);
  }

  OptionalSupplierAdapter(final Determinism determinism, final Supplier<? extends T> supplier) {
    super();
    if (supplier == null) {
      this.determinism = Determinism.ABSENT;
      this.supplier = Absence.instance();
    } else if (supplier instanceof OptionalSupplier<? extends T> os) {
      if (Objects.requireNonNull(determinism, "determinism") == os.determinism() ||
          determinism == Determinism.NON_DETERMINISTIC) {
        this.determinism = os.determinism();
        this.supplier = supplier;
      } else {
        throw new IllegalArgumentException("determinism: " + determinism + "; supplier: " + supplier);
      }
    } else {
      this.determinism = Objects.requireNonNull(determinism, "determinism");
      this.supplier = supplier;
    }
  }

  @Override
  public final Determinism determinism() {
    return this.determinism;
  }

  @Override
  public final T get() {
    return this.supplier.get();
  }

}
