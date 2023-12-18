# Products Service

Luego de haber creado el endpoint para `listar` y `guardar` productos los probamos:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"sku\": \"000003\", \"name\": \"Pc gamer\", \"description\": \"Pc gamer de ultimaa generacion\", \"price\": 3500, \"status\": true}" http://localhost:8081/api/v1/products | jq

>
< HTTP/1.1 201
<
````

Listamos los productos que hasta ahora tenemos registrados:

````bash
$ curl -v http://localhost:8081/api/v1/products | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
[
  {
    "id": 1,
    "sku": "000001",
    "name": "Pc gamer",
    "description": "Pc gamer de ultimaa generacion",
    "price": 1800.5,
    "status": true
  },
  {
    "id": 2,
    "sku": "000002",
    "name": "Pc gamer",
    "description": "Pc gamer de ultimaa generacion",
    "price": 3500,
    "status": true
  },
  {
    "id": 3,
    "sku": "000003",
    "name": "Pc gamer",
    "description": "Pc gamer de ultimaa generacion",
    "price": 3500,
    "status": true
  }
]
````
