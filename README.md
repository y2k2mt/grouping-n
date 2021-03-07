# Grouping-N

A tiny API: Just shuffles the given names.

|project | description|
|--------|------------|
|tagless | Sample project of [http4s](https://http4s.org/) + [doobie](https://github.com/tpolecat/doobie) + [monix](https://monix.io/) with [Tagless Final](http://homes.sice.indiana.edu/ccshan/tagless/jfp.pdf)|
|cake    | Sample project of [Akka http](https://doc.akka.io/docs/akka-http/current/index.html) + [Akka actor + persistence](https://doc.akka.io/docs/akka/current/typed/actors.html) with [Cake Pattern](https://www.baeldung.com/scala/cake-pattern)|

Enjoy!

### Setting up the project

1. [Install Java 8+](https://jdk.java.net/)
1. Run postgresql database server (e.g. `docker run --rm -d -p 5432:5432 -e POSTGRES_HOST_AUTH_METHOD=trust postgres:13`)
1. Run `./sbt run` to start the app
1. Request 
```
curl localhost:8080/grouping -H 'Content-Type: application/json' -d '{"n": 2,"members": ["1","2","3","4","5","6","7","8"]}'
```
5. And response
```
< HTTP/1.1 200 OK
< Content-Type: application/json
< Content-Length: 126
{
  "group": {
    "groups": [
      {
        "members": [
          "8",
          "7",
          "3",
          "2"
        ]
      },
      {
        "members": [
          "5",
          "1",
          "4",
          "6"
        ]
      }
    ]
  },
  "id": "08d6277f-89aa-4da2-b2d6-627cd91fc749"
}

```
