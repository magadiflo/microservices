# Inventory Service

## Probando endpoints

Verificamos si existe stock en inventario para el producto `000001`:

````bash
$ curl -v http://localhost:8083/api/v1/inventories/000001 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
true
````

Verificamos nuevamente si existe stock en inventario para un producto que estamos seguros tiene 0 elementos (este
resultado será similar cuando consultemos por un producto que no existe):

````bash
$ curl -v http://localhost:8083/api/v1/inventories/000004 | jq

>
< HTTP/1.1 200
< Content-Type: application/json
<
false
````

