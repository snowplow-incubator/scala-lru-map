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

class LruMapTest extends Specification {

  "Eldest used should be removed after maxSize is exceeded" should {
    for (maxSize <- Seq(100, 1000, 5000)) {
      s"LruMap.create[IO, Int, Int](maxSize = ${maxSize})" in {
        (for {
          lruMap <- LruMap.create[IO, Int, Int](maxSize)
          _      <- (0 to maxSize).toList.traverse(lruMap.put(_, 0))
          result <- lruMap.get(0)
        } yield (result must_== None)).unsafeRunSync
      }
    }

    s"LruMap.create[IO, Int, Int](maxSize = 0)" in {
      (for {
        lruMap <- LruMap.create[IO, Int, Int](0)
        _      <- (0 to 3).toList.traverse(lruMap.put(_, 0))
        result <- (0 to 3).toList.traverse(lruMap.get(_))
      } yield (result must_== List(None, None, None, None))).unsafeRunSync
    }
  }

  "If maxSize is not exceeded, get should return last put" in {
    val keyvalues = List("Zero", "One", "Two", "Three", "Four").zipWithIndex

    (for {
      lruMap <- LruMap.create[IO, String, Int](100)
      _      <- keyvalues.traverse(w => lruMap.put(w._1, w._2))
      result <- keyvalues.traverse(w => lruMap.get(w._1))
    } yield (result must_== keyvalues.map(w => Some(w._2)))).unsafeRunSync
  }

  "Eldest should be evicted based on use order" in {
    (for {
      lruMap <- LruMap.create[IO, Int, Int](2)
      _      <- lruMap.put(0, 0)
      _      <- lruMap.put(1, 1)
      _      <- lruMap.get(0)
      _      <- lruMap.put(2, 2) // 1 should be evicted
      result <- lruMap.get(1)
    } yield (result must_== None)).unsafeRunSync
  }
}

class LruMapSpecification extends Properties("LruMap") {
  property("Single put get") = Prop.forAll { (k: String, v: Int) =>
    (for {
      lruMap <- LruMap.create[IO, String, Int](1)
      _      <- lruMap.put(k, v)
      result <- lruMap.get(k)
    } yield result == Some(v)).unsafeRunSync
  }

  property("Put get empty") = Prop.forAll { (k1: String, k2: String, v: Int) =>
    (for {
      lruMap <- LruMap.create[IO, String, Int](2)
      _      <- lruMap.put(k1, v)
      result <- lruMap.get(k2)
    } yield k1 == k2 ^ result == None).unsafeRunSync
  }

  property("Fill lru") = Prop.forAll(Gen.choose(0, 10000)) { size =>
    (for {
      lruMap <- LruMap.create[IO, Int, Int](size)
      _      <- (0 to size).toList.traverse(n => lruMap.put(n, n))
      result <- lruMap.get(0)
    } yield result == None).unsafeRunSync
  }
}
