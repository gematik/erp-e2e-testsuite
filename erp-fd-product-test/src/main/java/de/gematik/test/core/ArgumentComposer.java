/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.core;

import de.gematik.test.core.exceptions.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import org.junit.jupiter.params.provider.*;

public class ArgumentComposer {

  private List<List<Object>> argumentLists;

  private ArgumentComposer() {
    this.argumentLists = new ArrayList<>();
  }

  public static ArgumentComposer composeWith() {
    return new ArgumentComposer();
  }

  public static <T extends Enum<?>> ArgumentComposer composeWith(Class<T> enumeration) {
    return composeWith(enumeration, List.of());
  }

  public static <T extends Enum<?>> ArgumentComposer composeWith(
      Class<T> enumeration, T... exclude) {
    return composeWith(enumeration, Arrays.asList(exclude));
  }

  public static <T extends Enum<?>> ArgumentComposer composeWith(
      Class<T> enumeration, List<T> exclude) {
    val list =
        Arrays.stream(enumeration.getEnumConstants())
            .filter(e -> !exclude.contains(e))
            .map(Object.class::cast)
            .toList();
    return composeWith(list);
  }

  public static ArgumentComposer composeWith(List<?> initial) {
    val builder = composeWith();
    initial.forEach(builder::arguments);
    return builder;
  }

  public static ArgumentComposer composeWith(Stream<Arguments> argumentsStream) {
    val builder = composeWith();
    for (var args : argumentsStream.map(Arguments::get).toList()) {
      val row = new ArrayList<>(Arrays.asList(args));
      builder.arguments(row);
    }

    return builder;
  }

  public ArgumentComposer arguments(List<Object> params) {
    argumentLists.add(params);
    return this;
  }

  public ArgumentComposer arguments(Object... params) {
    return arguments(List.of(params));
  }

  public <T extends Enum<?>> ArgumentComposer multiplyAppend(Class<T> enumeration) {
    return multiplyAppend(enumeration, List.of());
  }

  public <T extends Enum<?>> ArgumentComposer multiply(Class<T> enumeration) {
    return multiply(enumeration, List.of());
  }

  public <T extends Enum<?>> ArgumentComposer multiplyAppend(Class<T> enumeration, T... exclude) {
    val index = ensureEqualInnerSizes();
    return multiply(index, enumeration, exclude);
  }

  public <T extends Enum<?>> ArgumentComposer multiply(Class<T> enumeration, T... exclude) {
    return multiply(0, enumeration, exclude);
  }

  public <T extends Enum<?>> ArgumentComposer multiply(
      int index, Class<T> enumeration, T... exclude) {
    return multiply(index, enumeration, Arrays.asList(exclude));
  }

  public <T extends Enum<?>> ArgumentComposer multiplyAppend(
      Class<T> enumeration, List<T> exclude) {
    val index = ensureEqualInnerSizes();
    return multiply(index, enumeration, exclude);
  }

  public <T extends Enum<?>> ArgumentComposer multiply(Class<T> enumeration, List<T> exclude) {
    return multiply(0, enumeration, exclude);
  }

  public <T extends Enum<?>> ArgumentComposer multiply(
      int index, Class<T> enumeration, List<T> exclude) {
    val l =
        Arrays.stream(enumeration.getEnumConstants())
            .filter(e -> !exclude.contains(e))
            .map(Object.class::cast)
            .toList();
    return multiply(index, l);
  }

  public ArgumentComposer multiply(List<?> multiplier) {
    return multiply(0, multiplier);
  }

  public ArgumentComposer multiplyAppend(List<?> multiplier) {
    val index = ensureEqualInnerSizes();
    return multiply(index, multiplier);
  }

  public ArgumentComposer multiply(int index, List<?> multiplier) {
    if (multiplier == null || multiplier.isEmpty()) {
      throw new ArgumentBuilderException("Multiplying with an empty list is not allowed");
    }

    List<List<Object>> cartesian = new ArrayList<>(argumentLists.size() * multiplier.size());
    for (val mul : multiplier) {
      val inner = this.cloneInternalList();
      inner.forEach(row -> row.add(index, mul));
      cartesian.addAll(inner);
    }

    this.argumentLists = cartesian;
    return this;
  }

  public final Stream<Arguments> rotated() {
    ensureEqualInnerListSizes();
    val numOfArguments = ensureEqualInnerSizes();
    return IntStream.range(0, numOfArguments)
        .mapToObj(
            idx ->
                Arguments.arguments(argumentLists.stream().map(inner -> inner.get(idx)).toArray()));
  }

  public final Stream<Arguments> create() {
    ensureEqualInnerSizes();
    ensureEqualInnerListSizes();
    return argumentLists.stream().map(inner -> Arguments.arguments(inner.toArray()));
  }

  /**
   * This method will prepend each single row with the given value
   *
   * @param param is value to be prepended on each single row
   * @return this ExtensionBuilder
   */
  public ArgumentComposer prepend(Object... param) {
    argumentLists.forEach(row -> row.addAll(0, Arrays.asList(param)));
    return this;
  }

  public ArgumentComposer add(int index, Object... param) {
    argumentLists.forEach(row -> row.addAll(index, Arrays.asList(param)));
    return this;
  }

  /**
   * This method will append each single row the given value
   *
   * @param param is the value to appended at the end of each single row
   * @return this ExtensionBuilder
   */
  public ArgumentComposer append(Object... param) {
    argumentLists.forEach(row -> row.addAll(Arrays.asList(param)));
    return this;
  }

  private void ensureEqualInnerListSizes() {
    if (argumentLists.stream().map(List::size).distinct().count() != 1) {
      throw new ArgumentBuilderException("Inner Parameter Lists must be all of equal sizes");
    }
  }

  private int ensureEqualInnerSizes() {
    val numOfArguments =
        argumentLists.stream()
            .map(List::size)
            .findFirst()
            .orElseThrow(
                () ->
                    new ArgumentBuilderException(
                        "Could not determine the number of arguments: probably the List is empty"));

    if (numOfArguments == 0) {
      throw new ArgumentBuilderException(
          "Could not determine the number of arguments: probably the inner Lists are empty");
    }

    return numOfArguments;
  }

  private List<List<Object>> cloneInternalList() {
    return cloneInternalList(argumentLists.size());
  }

  /**
   * This will clone the internal argumentsList
   *
   * @param size is the initial size of the clone
   * @return the clone
   */
  private List<List<Object>> cloneInternalList(int size) {
    List<List<Object>> clone = new ArrayList<>(size);
    for (List<Object> origRow : argumentLists) {
      clone.add(new LinkedList<>(origRow));
    }

    return clone;
  }
}
