/*
 * Copyright (c) 2012-2022 Snowplow Analytics Ltd. All rights reserved.
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

import org.scalacheck.{Gen, Prop, Properties}
import cats.Id
import cats.implicits._

class LruMapSpecification extends Properties("LruMap") {
  property("Single put get") = Prop.forAll { (k: String, v: Int) =>
    val map: Id[LruMap[Id, String, Int]] = CreateLruMap[Id, String, Int].create(1)
    val result = map.flatMap[Option[Int]](m => m.put(k, v).productR(m.get(k)))
    (result: Option[Int]).contains(v)
  }

  property("Put get empty") = Prop.forAll { (k1: String, k2: String, v: Int) =>
    val map: Id[LruMap[Id, String, Int]] = CreateLruMap[Id, String, Int].create(2)
    val result = map.flatMap[Option[Int]](m => m.put(k1, v).productR(m.get(k2)))
    k1 == k2 ^ result.isEmpty
  }

  property("Fill lru") = Prop.forAll(Gen.choose(1, 10000)) { size =>
    val map: Id[LruMap[Id, Int, Int]] = CreateLruMap[Id, Int, Int].create(size)
    val result = map.flatMap[Option[Int]](m =>
      (1 to size).toList.traverse[Id, Unit](n => m.put(n, n)).productR(m.get(0))
    )
    result.isEmpty
  }

  property("Last put") =
    Prop.forAll(Gen.listOf(Gen.identifier).suchThat(list => list.distinct == list)) { list =>
      val map: Id[LruMap[Id, String, String]] =
        CreateLruMap[Id, String, String].create(list.length * 4)
      val result = map
        .flatMap[List[Option[String]]] { m =>
          list.traverse[Id, Unit](w => m.put(w, w)).productR {
            list.traverse[Id, Option[String]](w => m.get(w))
          }
        }

      val res = result == list.map(x => Some(x))
      if (!res) { println(s"list $list and result $result and len ${list.length}") }
      res
    }

  property("Evict lru") = Prop.forAll(Gen.choose(1, 1000)) { size =>
    val map: Id[LruMap[Id, Int, Int]] = CreateLruMap[Id, Int, Int].create(size)
    val result = (1 until size).reverse.toList.traverse[Id, Unit](i => map.put(i, i)).productR {
      (1 until size).toList.traverse[Id, Option[Int]](i => map.get(i)).productR {
        map.put(-1, -1).productR { // 0 should be evicted
          map.get(0)
        }
      }
    }
    result.isEmpty
  }
}
