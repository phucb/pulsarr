package com.github.kklisura.cdt.protocol.v2023.events.storage;

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

/** The origin's IndexedDB database list has been modified. */
public class IndexedDBListUpdated {

  private String origin;

  private String storageKey;

  private String bucketId;

  /** Origin to update. */
  public String getOrigin() {
    return origin;
  }

  /** Origin to update. */
  public void setOrigin(String origin) {
    this.origin = origin;
  }

  /** Storage key to update. */
  public String getStorageKey() {
    return storageKey;
  }

  /** Storage key to update. */
  public void setStorageKey(String storageKey) {
    this.storageKey = storageKey;
  }

  /** Storage bucket to update. */
  public String getBucketId() {
    return bucketId;
  }

  /** Storage bucket to update. */
  public void setBucketId(String bucketId) {
    this.bucketId = bucketId;
  }
}
