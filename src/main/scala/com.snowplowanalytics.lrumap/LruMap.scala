/*
 * Copyright (c) 2012-2023 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.snowplowanalytics.lrumap

/** Pure cache interface with `F` effect produced by interactions with cache */
trait LruMap[F[_], K, V] {

  /**
   * Associates the key with the specified value
   */
  def put(key: K, value: V): F[Unit]

  /**
   * Returns the value associated with the key, unless the key has been evicted
   */
  def get(key: K): F[Option[V]]
}
