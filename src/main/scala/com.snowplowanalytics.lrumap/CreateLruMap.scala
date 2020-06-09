/*
 * Copyright (c) 2012-2020 Snowplow Analytics Ltd. All rights reserved.
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

import cats.{ Eval, Id }
import cats.syntax.functor._
import cats.effect.Sync

/** `CreateLruMap` provides an ability to initialize the cache,
  * which effect will `F`
  *
  * *WARNING*: Due to support of non-`Sync`/eager algebras,
  * users should be super-careful about creating *values* of `F[_]`,
  * as they won't have RT/lazy semantics and will be memoized
  *
  * This is NOT a type class, as it does not have the coherence
  * requirement, but can be passed implicitly
  */
trait CreateLruMap[F[_], K, V] extends Serializable {
  /** Create an LruMap within `F` effect
    *
    * @param size Max size before evicting
    */
  def create(size: Int): F[LruMap[F, K, V]]
}

object CreateLruMap {

  /** Summoner */
  def apply[F[_], K, V](implicit ev: CreateLruMap[F, K, V]): CreateLruMap[F, K, V] = ev

  // Based on com.twitter.util.LruMap
  // https://github.com/twitter/util/blob/develop/util-collection/src/main/scala/com/twitter/util/LruMap.scala

  /** Eager instance */
  implicit def idInitCache[K, V]: CreateLruMap[Id, K, V] = new CreateLruMap[Id, K, V] {
    def create(size: Int): Id[LruMap[Id, K, V]] = new LruMap[Id, K, V] {
      private val underlying = makeUnderlying[K, V](size)
      def get(key: K): Id[Option[V]] = Option(underlying.get(key))
      def put(key: K, value: V): Id[Unit] = {
        val _ = underlying.put(key, value)
        ()
      }
    }
  }

  implicit def evalInitCache[K, V]: CreateLruMap[Eval, K, V] = new CreateLruMap[Eval, K, V] {
    def create(size: Int): Eval[LruMap[Eval, K, V]] = Eval.later(new LruMap[Eval, K, V] {
      private val underlying = makeUnderlying[K, V](size)
      def get(key: K): Eval[Option[V]] = Eval.later(Option(underlying.get(key)))
      def put(key: K, value: V): Eval[Unit] = Eval.later {
        underlying.put(key, value)
        ()
      }
    })
  }


  /** Referentially-transparent instance */
  implicit def syncInitCache[F[_], K, V](implicit S: Sync[F]): CreateLruMap[F, K, V] = new CreateLruMap[F, K, V] {
    def create(size: Int): F[LruMap[F, K, V]] =
      for {
        underlying <- S.delay(makeUnderlying[K, V](size))
        result = new LruMap[F, K, V] {
          def get(key: K): F[Option[V]] = S.delay(Option(underlying.get(key)))
          def put(key: K, value: V): F[Unit] = S.delay {
            underlying.put(key, value)
            ()
          }
        }
      } yield result
  }

  // initial capacity and load factor are the normal defaults for LinkedHashMap
  private def makeUnderlying[K, V](maxSize: Int): ImpureLruMap[K, V] =
    new ImpureLruMap[K, V](maxSize, 16, 0.75f)

  /**
    * Impure LruMap which underlies the pure map
    */
  private[lrumap] class ImpureLruMap[K, V](maxSize: Int, ic: Int, lf: Float)
    extends java.util.LinkedHashMap[K, V](ic, lf, true) {
    override protected def removeEldestEntry(eldest: java.util.Map.Entry[K, V]): Boolean =
      this.size() > maxSize
  }
}
