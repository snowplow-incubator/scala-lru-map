# Scala LruMap

[![Build Status][ci-image]][ci]
[![Maven Central][release-image]][releases]
[![Coverage Status][coveralls-image]][coveralls]
[![Join the chat at https://gitter.im/snowplow-incubator/scala-lru-map][chat-image]][chat]

A pure least recently used map based on [`Scaffeine`][scaffeine].

## API

Scala LruMap has "tagless final"-friendly API, allowing end-users to abstract over effects they use:

* `cats.effect.IO`, ZIO or similar lazy referentially-transparent implementation for most production use cases
* `cats.data.State` for testing purposes
* `Id` when you need an eager implementation

Generally, we highly recommend to use a referentially-transparent implementation,
but in some environments like Apache Spark or Apache Beam it is not possible to
use lazy implementation. In Spark and similar environments we recommend to use `Id`,
but everything needs to be tested in real environment.

Scala LruMap provides two tagless final "capabilities":

* `CreateLruMap[F[_], K, V]`, where `K` and `V` are type of key and values respectively and `F` is a type of effect. This capability tells its users that effect is capable of intializing a `LruMap` instance
* `LruMap[F[_], K, V]` is more like a classical interface and simply exposes API for `put` and `get` for an entity. It could be a wrapper for for remote cache or an object encapsulating a mutable reference

## Example Usage


```scala
import cats.effect.IO

class Fibonacci(lru: LruMap[IO, BigInt, BigInt]) {
  def calc(n: BigInt): IO[BigInt] = {
    if (n <= 0)
      IO.pure(0)
    else if (n == 1)
      IO.pure(1)
    else
      lru.get(n).flatMap(r => r match {
        case Some(r) => IO.pure(r)
        case None => for {
          m1 <- calc(n-1)
          m2 <- calc(n-2)
          _  <- lru.put(n, m1+m2)
        } yield m1+m2
      })
  }
}

val result = (for {
  // When the size of the map exceeds 500 the least recently used element is
  // removed
  lru    <- CreateLruMap[IO, BigInt, BigInt].create(500)
  result <- (new Fibonacci(lru)).calc(100)
} yield result).unsafeRunSync()

// Prints 354224848179261915075
println(result)
```

All impure methods and constructors are wrapped in [`cats.effect.Sync`][cats-sync].
When you want to use the result in an impure environment you can use
`unsafeRunSync` as shown above.

## Note on Thread Safety

Calls to `lruMap.get` and `LruMap.set` are not inherently thread-safe, so
concurrency concerns are left up to the choice of `Sync`.

## Copyright and license

Copyright 2012-2023 Snowplow Analytics Ltd.

Licensed under the [Apache License, Version 2.0][license] (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[scaffeine]: https://github.com/blemale/scaffeine
[cats-sync]: https://typelevel.org/cats-effect/docs/typeclasses/sync

[license]: http://www.apache.org/licenses/LICENSE-2.0

[ci]: https://github.com/snowplow-incubator/scala-lru-map/actions?query=workflow%3ACI
[ci-image]: https://github.com/snowplow-incubator/scala-lru-map/workflows/CI/badge.svg

[releases]: https://maven-badges.herokuapp.com/maven-central/com.snowplowanalytics/scala-lru-map_2.13
[release-image]: https://img.shields.io/maven-central/v/com.snowplowanalytics/scala-lru-map_2.13.svg

[coveralls]: https://coveralls.io/github/snowplow-incubator/scala-lru-map?branch=master
[coveralls-image]: https://coveralls.io/repos/github/snowplow-incubator/scala-lru-map/badge.svg?branch=master

[chat]: https://gitter.im/snowplow-incubator/scala-lru-map?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge
[chat-image]: https://badges.gitter.im/snowplow-incubator/scala-lru-map.svg
