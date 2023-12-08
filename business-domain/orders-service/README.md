# Orders Service

Implementamos el microservicio Orders con sus OrderItems. Este microservicio hará peticiones al microservicio de
inventarios. Para realizar las peticiones utilizamos el nuevo cliente creado a partir de **Spring Boot 3.2**
`RestClient`:

## RestClient (Spring Boot 3.2)

Como alternativa a `RestTemplate` y al uso de `WebClient` (de forma síncrona), es que a partir de `Spring Boot 3.2`
está disponible un nuevo cliente `RestClient`.

En nuestro caso lo usaremos para hacer peticiones desde el servicio `OrderServiceImpl` hacia el microservicio de
inventarios.

````java
/* other imports */

import org.springframework.web.client.RestClient;

@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final RestClient restClient;

    public OrderServiceImpl(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8083/api/v1/inventories")
                .build();
    }

    @Override
    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        // Check for inventory
        BaseResponse response = this.restClient.post()
                .uri("in-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .body(orderRequest.items())
                .retrieve()
                .body(BaseResponse.class);

        if (response == null || response.hasErrors()) {
            throw new IllegalArgumentException("Some of products are not in stock");
        }

        Order order = OrderMapper.mapToOrder(orderRequest);
        this.orderRepository.save(order);
    }
}
````

## Probando endpoints

Probamos el endpoint para crear una orden:

````bash
$  curl -v -X POST -H "Content-Type: application/json" -d "{\"items\": [{\"sku\": \"000001\", \"price\": 500, \"quantity\": 2}]}" http://localhost:8082/api/v1/orders

>
< HTTP/1.1 201
< Content-Type: text/plain;charset=UTF-8
<
Order placed successfully* Connection
````

Tratamos de registrar una orden con un producto que no existe:

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"items\": [{\"sku\": \"000009\", \"price\": 500, \"quantity\": 2}]}" http://localhost:8082/api/v1/orders

>
< HTTP/1.1 500
< Content-Type: application/json
<
{
  "timestamp":"2023-12-08T22:49:10.610+00:00",
  "status":500,
  "error":"Internal Server Error",
  "path":"/api/v1/orders"
}
````

El error mostrado anteriormente se debe a que dentro del código validamos que exista el producto que se está pasando
en la orden, si el producto no existe lanzamos la exception
`throw new IllegalArgumentException("Some of products are not in stock")`, produciéndose el error interno `500`.

Listamos todas las órdenes registradas:

````bash
$  curl -v http://localhost:8082/api/v1/orders | jq

>
< HTTP/1.1 200
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
  {
    "id": 5,
    "orderNumber": "4ad678c4-5f54-4163-90e2-3b14103c8e71",
    "items": [
      {
        "id": 5,
        "sku": "000002",
        "price": 230,
        "quantity": 14
      }
    ]
  },
  {...}
]
````
