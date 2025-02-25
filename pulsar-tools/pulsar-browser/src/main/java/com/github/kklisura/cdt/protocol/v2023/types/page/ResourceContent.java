package com.github.kklisura.cdt.protocol.v2023.types.page;

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

public class ResourceContent {

  private String content;

  private Boolean base64Encoded;

  /** Resource content. */
  public String getContent() {
    return content;
  }

  /** Resource content. */
  public void setContent(String content) {
    this.content = content;
  }

  /** True, if content was served as base64. */
  public Boolean getBase64Encoded() {
    return base64Encoded;
  }

  /** True, if content was served as base64. */
  public void setBase64Encoded(Boolean base64Encoded) {
    this.base64Encoded = base64Encoded;
  }
}
