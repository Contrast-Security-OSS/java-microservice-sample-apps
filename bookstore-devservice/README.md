# To run the bookstore-devservice in Docker:

```
$ docker build -t bookstore-devservice .
$ docker run -p 8002:8002 bookstore-devservice
```

To verify it is up:

```
$ curl http://localhost:8002/application/ping
```
