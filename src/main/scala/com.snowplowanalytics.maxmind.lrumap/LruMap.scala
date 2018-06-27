/*
 * Copyright (c) 2012-2018 Snowplow Analytics Ltd. All rights reserved.
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

import cats.effect.Sync
import java.util.{LinkedHashMap, Map}
import scala.collection.JavaConverters._

// Based on com.twitter.util.LruMap
// https://github.com/twitter/util/blob/develop/util-collection/src/main/scala/com/twitter/util/LruMap.scala

/** Companion Object to constructor */
object LruMap {

  /**
   * Create an LruMap
   *
   * @param size Max size before evicting
   */
  def create[F[_]: Sync, K, V](size: Int): F[LruMap[F, K, V]] = Sync[F].delay {
    new LruMap[F, K, V](makeUnderlying(size))
  }

  /* Alternative to lruMap.put */
  def put[F[_]: Sync, K, V](lruMap: LruMap[F, K, V])(k: K, v: V): F[Unit] =
    lruMap.put(k, v)

  /* Alternative to lruMap.get */
  def get[F[_]: Sync, K, V](lruMap: LruMap[F, K, V])(k: K): F[Option[V]] =
    lruMap.get(k)

  // initial capacity and load factor are the normal defaults for LinkedHashMap
  private def makeUnderlying[K, V](maxSize: Int): ImpureLruMap[K, V] =
    new ImpureLruMap[K, V](maxSize, 16, 0.75f)
}

/**
 * A pure LRU `map` backed by [[java.util.LinkedHashMap]].
 */
class LruMap[F[_]: Sync, K, V] private (underlying: ImpureLruMap[K, V]) {

  /**
   * Associates the key with the specified value
   */
  def put(key: K, value: V): F[Unit] = Sync[F].delay {
    underlying.put(key, value)
  }

  /**
   * Returns the value associated with the key, unless the key has been evicted
   */
  def get(key: K): F[Option[V]] = Sync[F].delay {
    Option(underlying.get(key))
  }
}

/**
 * Impure LruMap which underlies the pure map
 */
private[lrumap] class ImpureLruMap[K, V](maxSize: Int, ic: Int, lf: Float)
    extends LinkedHashMap[K, V](ic, lf, true) {
  override protected def removeEldestEntry(eldest: Map.Entry[K, V]): Boolean =
    this.size() > maxSize
}
