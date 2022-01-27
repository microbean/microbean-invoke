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

import java.lang.constant.ClassDesc;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Useful constant bootstrap methods and methods that make sense to
 * invoke from {@link
 * java.lang.invoke.ConstantBootstraps#invoke(Lookup, String, Class,
 * MethodHandle, Object...)}.
 *
 * @author <a href="https://about.me/lairdnelson" target="_parent">Laird Nelson</a>
 *
 * @see <a
 * href="https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/invoke/package-summary.html"
 * target="_parent"><code>java.lang.invoke</code></a>
 */
public final class BootstrapMethods {


  /*
   * Constructors.
   */


  private BootstrapMethods() {
    super();
  }


  /*
   * public static methods.
   */


  public static final ConstantCallSite findStaticSetterCallSite(final Lookup lookup,
                                                                final String fieldName,
                                                                final MethodType methodType,
                                                                final Class<?> targetClass)
    throws Throwable {
    return new ConstantCallSite(findStaticSetterMethodHandle(lookup, fieldName, methodType.parameterType(0), targetClass));
  }

  public static final ConstantCallSite findStaticCallSiteAsSpreader(final Lookup lookup,
                                                                    final String methodName,
                                                                    final MethodType methodType,
                                                                    final Class<?> targetClass)
    throws Throwable {
    return new ConstantCallSite(findStaticMethodHandle(lookup, methodName, methodType, targetClass)
                                .asSpreader(Object[].class, methodType.parameterCount()));
  }

  public static final ConstantCallSite findSetterCallSite(final Lookup lookup,
                                                          final String fieldName,
                                                          final MethodType methodType,
                                                          final Class<?> targetClass)
    throws Throwable {
    return new ConstantCallSite(findSetterMethodHandle(lookup, fieldName, methodType.parameterType(0), targetClass));
  }

  public static final ConstantCallSite findVirtualCallSite(final Lookup lookup,
                                                           final String methodName,
                                                           final MethodType methodType,
                                                           final Class<?> targetClass)
    throws Throwable {
    return new ConstantCallSite(findVirtualMethodHandle(lookup, methodName, methodType, targetClass));
  }

  public static final ConstantCallSite findConstructorCallSite(final Lookup lookup,
                                                               final String ignoredMethodName,
                                                               final MethodType methodType,
                                                               final Class<?> targetClass)
    throws Throwable {
    return new ConstantCallSite(findConstructorMethodHandle(lookup, methodType, targetClass));
  }

  @SuppressWarnings("unchecked")
  public static final <K, V> SortedMap<K, V> immutableSortedMapOf(final Map<? extends K, ? extends V> map,
                                                                  final Comparator<? super K> comparator) {
    final SortedMap<K, V> mutableSortedMap;
    if (comparator == null) {
      if (map instanceof SortedMap) {
        mutableSortedMap = (SortedMap<K, V>)map;
      } else {
        mutableSortedMap = new TreeMap<>(map);
      }
    } else {
      mutableSortedMap = new TreeMap<>(comparator);
      mutableSortedMap.putAll(map);
    }
    return Collections.unmodifiableSortedMap(mutableSortedMap);
  }

  @SuppressWarnings("unchecked")
  public static final <E> SortedSet<E> immutableSortedSetOf(final Collection<? extends E> set,
                                                            final Comparator<? super E> comparator) {
    final SortedSet<E> mutableSortedSet;
    if (comparator == null) {
      if (set instanceof SortedSet) {
        mutableSortedSet = (SortedSet<E>)set;
      } else {
        mutableSortedSet = new TreeSet<>(set);
      }
    } else {
      mutableSortedSet = new TreeSet<>(comparator);
      mutableSortedSet.addAll(set);
    }
    return Collections.unmodifiableSortedSet(mutableSortedSet);
  }


  /*
   * private static methods.
   */


  private static final MethodHandle findStaticMethodHandle(final Lookup lookup,
                                                           final String methodName,
                                                           final MethodType methodType,
                                                           final Class<?> targetClass)
    throws Throwable {
    return MethodHandles.privateLookupIn(targetClass, lookup).findStatic(targetClass, methodName, methodType);
  }

  private static final MethodHandle findStaticSetterMethodHandle(final Lookup lookup,
                                                                 final String fieldName,
                                                                 final Class<?> fieldType,
                                                                 final Class<?> targetClass)
    throws Throwable {
    return
      MethodHandles.privateLookupIn(targetClass, lookup).findStaticSetter(targetClass, fieldName, fieldType);
  }

  private static final MethodHandle findSetterMethodHandle(final Lookup lookup,
                                                           final String fieldName,
                                                           final Class<?> fieldType,
                                                           final Class<?> targetClass)
    throws Throwable {
    return
      MethodHandles.privateLookupIn(targetClass, lookup).findSetter(targetClass, fieldName, fieldType);
  }

  private static final MethodHandle findVirtualMethodHandle(final Lookup lookup,
                                                            final String methodName,
                                                            final MethodType methodType,
                                                            final Class<?> targetClass)
    throws Throwable {
    return MethodHandles.privateLookupIn(targetClass, lookup).findVirtual(targetClass, methodName, methodType);
  }

  private static final MethodHandle findConstructorMethodHandle(final Lookup lookup,
                                                                final MethodType methodType,
                                                                final Class<?> targetClass)
    throws Throwable {
    if (void.class != methodType.returnType()) {
      throw new IllegalArgumentException("void.class != methodType.returnType(); methodType: " + methodType);
    }
    return MethodHandles.privateLookupIn(targetClass, lookup).findConstructor(targetClass, methodType);
  }


}
