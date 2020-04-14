# To run the bookstore-frontend in Docker:

```
$ docker build -t bookstore-frontend .
$ docker run -p 8000:8000 bookstore-frontend
```

Note that you may see an error about invalid instructions, bytecode, or something else, and you can ignore it.

To verify it is up:

```
$ curl http://localhost:8000/health
```
