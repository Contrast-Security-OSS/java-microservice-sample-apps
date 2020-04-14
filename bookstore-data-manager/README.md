# To run the bookstore-data-manager in Docker:

```
$ docker build -t bookstore-data-manager .
$ docker run -p 8001:8001 bookstore-data-manager
```

To verify it is up:

```
$ curl http://localhost:8001/
```
