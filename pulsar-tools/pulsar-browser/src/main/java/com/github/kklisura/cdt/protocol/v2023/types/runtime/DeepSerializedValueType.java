package com.github.kklisura.cdt.protocol.v2023.types.runtime;

/*-
 * #%L
 * cdt-java-client
 * %%
 * Copyright (C) 2018 - 2023 Kenan Klisura
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DeepSerializedValueType {
  @JsonProperty("undefined")
  UNDEFINED,
  @JsonProperty("null")
  NULL,
  @JsonProperty("string")
  STRING,
  @JsonProperty("number")
  NUMBER,
  @JsonProperty("boolean")
  BOOLEAN,
  @JsonProperty("bigint")
  BIGINT,
  @JsonProperty("regexp")
  REGEXP,
  @JsonProperty("date")
  DATE,
  @JsonProperty("symbol")
  SYMBOL,
  @JsonProperty("array")
  ARRAY,
  @JsonProperty("object")
  OBJECT,
  @JsonProperty("function")
  FUNCTION,
  @JsonProperty("map")
  MAP,
  @JsonProperty("set")
  SET,
  @JsonProperty("weakmap")
  WEAKMAP,
  @JsonProperty("weakset")
  WEAKSET,
  @JsonProperty("error")
  ERROR,
  @JsonProperty("proxy")
  PROXY,
  @JsonProperty("promise")
  PROMISE,
  @JsonProperty("typedarray")
  TYPEDARRAY,
  @JsonProperty("arraybuffer")
  ARRAYBUFFER,
  @JsonProperty("node")
  NODE,
  @JsonProperty("window")
  WINDOW
}
