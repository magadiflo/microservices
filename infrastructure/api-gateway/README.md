# Spring Cloud Gateway

## Configurando Rutas

En el `application.yml` configuramos las rutas de nuestros microservicios a las que desde nuestro `api-gateway` serán
redireccionadas las peticiones que se hagan:

````yml
server:
  port: 8080

spring:
  application:
    name: api-gateway

  cloud:
    gateway:
      routes:
        # Inventory service routes
        - id: inventory-service-route
          uri: http://localhost:8083
          predicates:
            - Path=/api/v1/inventories/**

        # Order service routes
        - id: orders-service-route
          uri: http://localhost:8082
          predicates:
            - Path=/api/v1/orders/**

        # Product service routes
        - id: products-service-route
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/products/**
````

A continuación explico el significado de las rutas. Tomaré como ejemplo la primera ruta:

- `id: inventory-service-route`, le asignamos un identificador único a la ruta.
- `uri: http://localhost:8083`, definimos la uri a la cual se enrutarán las solicitudes. En este caso, estamos colocando
  la uri del microservicio `inventory-service`, porque es el microservicio al que queremos enrutar en esta primera
  configuración de rutas.
- `Path=/api/v1/inventories/**`, el`Path` es un tipo de predicado que verifica si la ruta de la solicitud coincide con
  el patrón proporcionado. `/api/v1/inventories/**`, sería nuestro patrón de ruta. El doble asterisco `**` es un comodín
  que coincide con cualquier ruta adicional después de patrón proporcionado.

**FUNCIONAMIENTO**

> Supongamos que se hace una petición a nuestro `api-gateway` cuyo puerto es el `8080` a través de la siguiente
> dirección `http://localhost:8080/api/v1/inventories`, vemos que el `/api/v1/inventories` de esa solicitud coincide con
> la primera ruta definida en el `application.yml`, por lo tanto, redireccionará el `request` al `uri` de esa primera
> ruta, es decir al `uri: http://localhost:8083` concatenándole el patrón `/api/v1/inventories`. Entonces, finalmente la
> solicitud que saldrá desde el `api-gateway` al microservicio `inventory-service` será enviada a través de la siguiente
> dirección `http://localhost:8083/api/v1/inventories`.

---
**NOTA**

En los cursos de Andrés Guzmán, cuando trabajámos con `Spring Cloud Gateway` definíamos en las rutas el `filtro`
`StripPrefix=1` o `StripPrefix=2` dependiendo de cómo hayamos definido el predicate `Path`.

Tomemos como ejemplo la configuración que hicimos en el proyecto `microservices-project` para el `spring cloud gateway`:

````yml
spring:
  cloud:
    gateway:
      routes:
        - id: ms-productos
          uri: lb://ms-productos
          predicates:
            - Path=/api-base/productos-base/**
          filters:
            - StripPrefix=2
````

Como vemos, en el predicate `Path` definimos un patrón base `/api-base/productos-base/**`, pero además definimos el
filtro `StripPrefix` con valor `2`. Con este filtro estamos indicando el número de partes de la ruta que debemos
eliminar de la petición antes de enviarla al flujo descendente.

Veamos un ejemplo para entender qué es lo que hace el `StripPrefix=2` teniendo en cuenta que el controlador del
microservicio productos tiene esta configuración `@RequestMapping(path = "/api/v1/productos")`. Ahora, supongamos que
hacemos una petición a ese gateway:

```bash
$ http://127.0.0.1:8090/api-base/productos-base/api/v1/productos
``` 

Observamos que la dirección anterior coincide con el partón del path `/api-base/productos-base/**`, por lo tanto,
el `Gateway` redireccionará la solicitud a la uri del microservicio de productos. Pero antes de continuar, como está
definido el filtro `StripPrefix=2`, lo que hace el `Gateway` es eliminar las dos primeras partes del path, es decir,
eliminará el `/api-base/productos-base/` y concatenará a la uri destino el path restante. Finalmente, la url completa
quedaría así `lb://ms-productos/api/v1/productos`.

**¿Qué pasa si no colocamos el filtro StripPrefix?**, pues no eliminará ningún path, hará la llamada con toda la url
completa `lb://ms-productos/api-base/productos-base/api/v1/productos` y eso daría un error, ya que el microservicio
de productos tiene definido el path `/api/v1/productos`.

Ahora, volviendo a nuestro proyecto original, por ejemplo en el de la ruta cuyo id es `inventory-service-route`
**¿Por qué ahí no definimos un filtro StripPrefix?** Eso es porque como predicado `Path` estamos definiendo la misma
ruta que le definimos al microservicio `inventory-service`.

---

## Realizando pruebas a los microservicios a partir del Gateway

Levantamos todos nuestros microservicios: ApiGateway, Inventory, Products y Orders y realizamos las peticiones a todos
ellos pero usando como dirección la del `api-gateway` cuyo puerto es el `8080`:

- **product-service**, creamos un producto:

````bash
$  curl -v -X POST -H "Content-Type: application/json" -d "{\"sku\": \"000006\", \"name\": \"Laptop Toshiba\", \"description\": \"Laptop Toshiba\", \"price\": 100, \"status\": true}" http://localhost:8080/api/v1/products

>
< HTTP/1.1 201 Created
< Content-Length: 0
< Date: Sun, 10 Dec 2023 05:23:53 GMT
<
````

- **product-service**, listamos todos los productos:

````bash
$ curl -v http://localhost:8080/api/v1/products | jq

>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json
<
[
  {
    "id": 1,
    "sku": "000001",
    "name": "Pc gamer",
    "description": "Pc gamer de ultimaa generacion",
    "price": 1800,
    "status": true
  },
  {...},
  {
    "id": 5,
    "sku": "000005",
    "name": "Florecente",
    "description": "Florecente Antiguo",
    "price": 70,
    "status": true
  }
]
````

- **orders-service**, creamos una nueva orden:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"items\": [{\"sku\": \"000001\", \"price\": 500, \"quantity\": 2}, {\"sku\": \"000002\", \"price\": 15, \"quantity\": 10}]}" http://localhost:8080/api/v1/orders

>
< HTTP/1.1 201 Created
< Content-Type: text/plain;charset=UTF-8
<
Order placed successfully
````

- **orders-service**, listamos las órdenes creadas:

````bash
$ curl -v http://localhost:8080/api/v1/orders | jq

>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json
<
[
  {
    "id": 2,
    "orderNumber": "1e3b15e9-c3e5-4438-a454-5462268f4a6c",
    "items": [
      {
        "id": 2,
        "sku": "000001",
        "price": 500,
        "quantity": 2
      }
    ]
  },
  {...},
  {
    "id": 8,
    "orderNumber": "9b191b1a-5533-4fdc-9daf-1d39b8849ed7",
    "items": [
      {
        "id": 9,
        "sku": "000001",
        "price": 500,
        "quantity": 2
      },
      {
        "id": 10,
        "sku": "000002",
        "price": 15,
        "quantity": 10
      }
    ]
  }
]
````

- **inventory-service**, verificamos si existe stock para el producto `000001`:

````bash
$ curl -v http://localhost:8080/api/v1/inventories/000001 | jq

>
< HTTP/1.1 200 OK
< transfer-encoding: chunked
< Content-Type: application/json
<
true
````

**CONCLUSIÓN**
> Desde ahora ya podemos hacer uso del `API-GATEWAY` para hacer peticiones a nuestros microservicios. Será el
> `API-GATEWAY` quien redireccionará las peticiones al microservicio correspondiente.