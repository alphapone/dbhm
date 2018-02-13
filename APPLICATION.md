APPLICATION
=====

Dbhm: disk based hash map with fixed size of allocated space
-----
This object cache may be used as embedded JAR library and as standalone server application provides RESTful service for client

## Run RESTful service

To run RESTful service you should build application using `mvn package` command and run java class `org.alphapone.dbhm.Server`.
You can use `mvn exec:java` command to start server with configured classpath.

## Use Server application

You may send POST queries to store some objects in the cache

### Resource name

Default resource name is http://localhost:8000/dbhm

### Put object in the cache

To put object in the store you should send a POST  query contains object with fields `key` and `value`. Object from `value` fiel will be stored in `key` field marked cell
e.g: `curl -X POST -H 'Content-Type: application/json' -d '{"key": "abra12", "value":{"a":902}}' http://localhost:8000/dbhm`

### Get object from cache

To get object fromo the cache you should send the POST
e.g: `curl -X POST -H 'Content-Type: application/json' -d '{"key": "abra12"}' http://localhost:8000/dbhm`

### Remove object from cache

To remove object you should POST a quey contains field "command":"remove"
e.g: `curl -X POST -H 'Content-Type: application/json' -d '{"key": "abra12", "command":"remove"}' http://localhost:8000/dbhm`


### Configuration options

To configure this application you should pass -D parameter `org.alphapone.dbhm.O.configure`
contains a path to configuration properties file.
In this file you can specify following properties:
          cellSize=256
          dbhmSize=1000000000
          crs=replace
          serverPort=8000

`dbhmSize` is a fixed size of allocated space for disk based hash map.

`crs` may accept values `keep` and `replace`

`serverPort` is a port for server application (8000 by default)



