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

import org.specs2.mutable.Specification
import org.scalacheck.{Gen, Prop, Properties}
import cats.implicits._
import cats.syntax.either._
import cats.syntax.option._
import cats.effect.IO

class LruMapSpecification extends Properties("LruMap") {
  property("Single put get") = Prop.forAll { (k: String, v: Int) =>
    (for {
      lruMap <- LruMap.create[IO, String, Int](1)
      _      <- LruMap.put(lruMap)(k, v)
      result <- LruMap.get(lruMap)(k)
    } yield result == Some(v)).unsafeRunSync()
  }

  property("Put get empty") = Prop.forAll { (k1: String, k2: String, v: Int) =>
    (for {
      lruMap <- LruMap.create[IO, String, Int](2)
      _      <- lruMap.put(k1, v)
      result <- lruMap.get(k2)
    } yield k1 == k2 ^ result == None).unsafeRunSync()
  }

  property("Fill lru") = Prop.forAll(Gen.choose(0, 10000)) { size =>
    (for {
      lruMap <- LruMap.create[IO, Int, Int](size)
      _      <- (0 to size).toList.traverse(n => lruMap.put(n, n))
      result <- lruMap.get(0)
    } yield result == None).unsafeRunSync()
  }

  property("Last put") = Prop.forAll(Gen.listOf(Gen.alphaStr)) { list =>
    (for {
      lruMap <- LruMap.create[IO, String, String](list.length)
      _      <- list.traverse(w => lruMap.put(w, w))
      result <- list.traverse(w => lruMap.get(w))
    } yield result == list.map(Some(_))).unsafeRunSync()
  }

  property("Evict lru") = Prop.forAll(Gen.choose(0, 1000)) { size =>
    (for {
      lruMap <- LruMap.create[IO, Int, Int](size)
      _      <- (0 until size).reverse.toList.traverse(i => lruMap.put(i, i))
      _      <- (0 until size).toList.traverse(i => lruMap.get(i))
      _      <- lruMap.put(-1, -1) // 0 should be evicted
      result <- lruMap.get(0)
    } yield result == None).unsafeRunSync()
  }
}
