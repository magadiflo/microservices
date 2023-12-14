# [Creando una Aplicación Escalable - Microservicios con Spring Boot](https://www.youtube.com/playlist?list=PLlYjHWCxjWmAt5hE3OEaemlWkZBZa7w4e)

Tomado del canal de youtube **Uncle Dave's Code**

## Panorama general de la arquitectura a construir

![aplicación](./assets/01.aplicación.png)

## Microservicios Base

![microservicios base](./assets/02.microservicios-negocio.png)

## Docker Compose: Creando Bases de Datos en contenedores Docker

Como vamos a hacer uso de varias bases de datos, optaremos por usar contenedores docker, de esa forma evitaremos usar
la base de datos de nuestra pc local y usar las bases de datos que estarán en los contenedores.

Empezaremos creando el archivo `compose.yml` en la raíz del proyecto y agregando los siguientes servicios de compose:

````yaml
services:
  ### PostgreSQL database for inventory
  db-inventory:
    container_name: db-inventory
    image: postgres:15.2-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_inventory_service
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - 5433:5432
    expose:
      - 5433
  ### MySQL database for orders
  db-orders:
    container_name: db-orders
    image: mysql:8.0.33
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: db_orders_service
      MYSQL_USER: magadiflo
      MYSQL_PASSWORD: magadiflo
      MYSQL_ROOT_PASSWORD: magadiflo
    ports:
      - 3307:3306
    expose:
      - 3307
  ### PostgreSQL database for products
  db-products:
    container_name: db-products
    image: postgres:15.2-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_products_service
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - 5434:5432
    expose:
      - 5434
````

Ejecutamos nuestro archivo `compose.yml` con el siguiente comando para levantar todos los servicios definidos y en
modo `dettached (-d)`:

````bash
$ docker compose up -d
````

![docker compose - databases](./assets/03.docker-compose-databases.png)

Verificamos los contenedores que han sido creados:

````bash
$ docker container ls -a
CONTAINER ID   IMAGE                  COMMAND                  CREATED         STATUS         PORTS                                         NAMES
81a4e40e8c5b   mysql:8.0.33           "docker-entrypoint.s…"   4 minutes ago   Up 3 minutes   3307/tcp, 33060/tcp, 0.0.0.0:3307->3306/tcp   db-orders
443366d4c477   postgres:15.2-alpine   "docker-entrypoint.s…"   4 minutes ago   Up 3 minutes   5434/tcp, 0.0.0.0:5434->5432/tcp              db-products
f63bcaa04b2f   postgres:15.2-alpine   "docker-entrypoint.s…"   4 minutes ago   Up 3 minutes   5433/tcp, 0.0.0.0:5433->5432/tcp              db-inventory
````

¡Listo!, hasta este punto ya podemos conectarnos a las bases de datos de los contenedores usando alguna herramienta
como `DBeaver`.

---

# Eureka Netflix y Spring Cloud Gateway

---

En esta sección del curso abordaremos dos componentes más de la arquitectura de microservicios: `Eureka Netflix` y
`Spring Cloud Gateway`:

![Eureka Server y Spring Cloud Gateway](./assets/04.eureka-y-gateway.png)

---

# Spring Security, Keycloak y Resilience4j

En esta sección abordaremos tres temas importantes, entre las cuales está el uso de `keycloak` dentro de nuestra
arquitectura de microservicios.

`Keycloack` es una plataforma de gestión de identidad y acceso de código abierto para agregar características avanzadas
de seguridad como la gestión de usuarios y el flujo de `OAuth 2`.

![integración keycloak](./assets/05.keycloak-integration.png)

## [Levantando contenedor Keycloak](https://www.keycloak.org/getting-started/getting-started-docker)

Crearemos dos nuevos servicios en nuestro archivo `compose.yml` uno para Keycloack y otro para la base de datos Postgres
que usará dicha plataforma:

````yaml
services:
  # Other services

  ### PostgreSQL database for Keycloak
  db-keycloak:
    container_name: db-keycloak
    image: postgres:15.2-alpine
    restart: unless-stopped
    environment:
      POSTGRES_DB: db_keycloak
      POSTGRES_USER: magadiflo
      POSTGRES_PASSWORD: magadiflo
    ports:
      - 5435:5432
    expose:
      - 5435
  ### Keycloak
  keycloak:
    container_name: keycloak
    image: quay.io/keycloak/keycloak:21.0.2
    restart: unless-stopped
    command: [ "start-dev" ]
    ports:
      - 8181:8080
    expose:
      - 8181
    depends_on:
      - db-keycloak
    environment:
      DB_VENDOR: POSTGRES
      DB_ADDR: db-keycloak
      DB_DATABASE: db_keycloak
      DB_PORT: 5435
      DB_USER: magadiflo
      DB_PASSWORD: magadiflo
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_HTTP_PORT: 8080
````

Levantamos todos los contenedores con `docker compose`:

````bash
$ docker compose up -d
[+] Building 0.0s (0/0)
[+] Running 5/5
✔ Container db-keycloak   Started
✔ Container db-products   Started
✔ Container db-inventory  Started
✔ Container db-orders     Started
✔ Container keycloak      Started

$ docker container ls -a
CONTAINER ID   IMAGE                              COMMAND                  CREATED          STATUS              PORTS                                         NAMES
3916c67848b6   quay.io/keycloak/keycloak:21.0.2   "/opt/keycloak/bin/k…"   15 minutes ago   Up About a minute   8181/tcp, 8443/tcp, 0.0.0.0:8181->8080/tcp    keycloak
cad2c035fb75   postgres:15.2-alpine               "docker-entrypoint.s…"   18 minutes ago   Up About a minute   5435/tcp, 0.0.0.0:5435->5432/tcp              db-keycloak
4cde30018af0   postgres:15.2-alpine               "docker-entrypoint.s…"   4 days ago       Up About a minute   5434/tcp, 0.0.0.0:5434->5432/tcp              db-products
180c531d27e9   postgres:15.2-alpine               "docker-entrypoint.s…"   4 days ago       Up About a minute   5433/tcp, 0.0.0.0:5433->5432/tcp              db-inventory
d6714776dc2e   mysql:8.0.33                       "docker-entrypoint.s…"   4 days ago       Up About a minute   3307/tcp, 33060/tcp, 0.0.0.0:3307->3306/tcp   db-orders
````

Accedemos a `keycloack` a través del navegador web `http://localhost:8181/` y nos logeuamos con las credenciales de
`Keycloak`:

![keycloak](./assets/06.keycloak.png)

## Configurando Keycloak

Dentro del dashboard de `keycloak` empezaremos creando un `realm`. En keycloak un real es una especie de contenedor
lógico para agrupar usuarios, aplicaciones y configuraciones relacionadas con la seguridad permitiendo una gestión y
separación efectiva de los diferentes componentes del sistema de autenticación y autorización. Un `dominio (realm)`
gestiona un conjunto de usuarios, credenciales, roles y grupos. Un usuario pertenece a un `dominio` y se registra en él.
Los `dominios` están aislados entre sí y solo pueden gestionar y autenticar a los usuarios que controlan.

![creando un realm](./assets/07.creando-realm.png)

Ahora definiremos usuarios, roles y configuraremos una aplicación cliente:

### Roles

![realm-roles](./assets/08.realm-roles.png)

### Usuario Admin y Basic

![admin user](./assets/09.admin-user.png)

![basic user](./assets/10.basic-user.png)

### Aplicación Cliente

![client](./assets/11.client.png)

## Agregando nuevas dependencias

Recordemos que estamos trabajando con un proyecto multi-módulo de maven, por lo tanto las dependencias están
organizadas. Pero, por tema de documentación mostraré aquí las dependencias que se agregaron en cada microservicio:

- Microservicio `api-gateway`:

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
</dependencies>
````

- Microservicio `discovery-server`:

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
````

- A todos los microservicios de dominio:

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
</dependencies>
````

## Configurando usuario y password en discovery-server

Agregaremos las configuraciones en el microservicio `discovery-server` como el usuario y contraseña para un usuario de
spring security. Estas credenciales deberán ser enviadas por los demás microservicios que intenten comunicarse con
`Eureka Server`.

````yml
spring:
  security:
    user:
      name: eureka
      password: password

logging:
  level:
    org.springframework.web: debug
    org.springframework.security: debug
````

## Agregando configuraciones en los microservicios

A continuación se muestran solo las configuraciones agregadas o modificadas de cada microservicios:

- Microservicio `discovery-server`:

````yml
spring:
  security:
    user:
      name: eureka
      password: password

logging:
  level:
    org.springframework.web: debug
    org.springframework.security: debug
````

- Microservicio `api-gateway`:

````yaml
spring:
  cloud:
    gateway:
      routes:
        # Other routes

        # Discovery Server routes
        - id: discovery-service-route
          uri: http://localhost:8761
          predicates:
            - Path=/eureka/web
          filters:
            - SetPath=/

        # Discovery Server Static routes
        - id: discovery-service-static-route
          uri: http://localhost:8761
          predicates:
            - Path=/eureka/**
      # Filtros para definir el comportamiento de enrutamiento y
      # manipulación de sesiones y tokens de autenticación
      default-filters:
        - SaveSession
        - TokenRelay

  # Keycloak
  # El issuer-uri, se utiliza para verificar la validez de los tokens emitidos por
  # keycloak y garantiza que venga de una fuente confiable
  security:
    oauth2:
      client:
        registration:
          keycloak:
            provider: keycloak
            scope: openid
            client-id: microservices_client
            client-secret: HAVz0radwfOEsapxT5e8GdvckcOlFgD6
            authorization-grant-type: authorization_code
            redirect-uri: http://localhost:8080/login/oauth2/code/keycloak
        provider:
          keycloak:
            issuer-uri: http://localhost:8181/realms/microservices-realm

# Eureka client
eureka:
  instance:
  client:
    service-url:
      defaultZone: http://eureka:password@localhost:8761/eureka/ #<-- Enviamos usuario y password que espera recibir el servidor de eureka
````

- Microservicios de dominio: products, orders, inventory

````yaml
spring:
  # Keycloak
  # jwk-set-uri, usada por el servidor de recursos para validar la autenticidad e integridad de los tokens
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8181/realms/microservices-realm/protocol/openid-connect/certs
````

## Agregando filtro de seguridad personalizada en los microservicios

Vamos a agregar configuraciones de seguridad a todos los microservicios:

- Microservicio `api-gateway`

````java

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorize -> authorize.anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults());
        return http.build();
    }
}
````

- Microservicio `discovery-server`:

````java

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
````

- La siguiente configuración será agregada a los microservicios de
  dominio: `products-service, orders-service, inventory-service`:

````java

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/**").authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(configure -> configure.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));

        return http.build();
    }

    //Convertir los roles de keycloak en una representación compatible con spring security
    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());

        return converter;
    }
}

@SuppressWarnings("unchecked")
class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt.getClaims() == null) {
            return List.of();
        }
        final Map<String, List<String>> realmAccess = (Map<String, List<String>>) jwt.getClaims().get("realm_access");
        return realmAccess.get("roles").stream()
                .map(roleName -> "ROLE_" + roleName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
````

## Probando seguridad de microservicios

Accedemos a la url `http://localhost:8080/api/v1/products` para ver la lista de productos pero como realizamos las
configuraciones de seguridad en el apartado anterior seremos redireccionados al `login de keycloak`:

![login keycloak](./assets/12.login-keycloak.png)

Nos loguearemos con las credenciales del usuario que registramos en keycloak:

![credentials](./assets/13.login-credentials.png)

Finalmente, como las credenciales son correctas somos redirigidos al end point de productos:

![login success](./assets/14.login-success.png)

## Probando seguridad de microservicios con Postman

En este apartado realizaremos una petición al endpoint `http://localhost:8080/api/v1/products` desde postman, pero
como nuestro backend ya está segurizado necesitamos realizar ciertas configuraciones al postman

![postman config](./assets/15.postman-config.png)

![postman config](./assets/16.postman-config-2.png)

**DONDE**

- `Token Name`, le damos un nombre cualquiera.
- `Callback URL` es `http://localhost:8080/login/oauth2/code/keycloak`
- `Auth URL` es `http://localhost:8181/realms/microservices-realm/protocol/openid-connect/auth`
- `Access Token URL` es `http://localhost:8181/realms/microservices-realm/protocol/openid-connect/token`

Las url mostradas las obtuve del mismo keycloak, para eso debemos ir dentro del keycloak y en el apartado de
`Realm settings` podemos encontrar un enlace llamado `OpenID Endpoint Configuration` que nos llevará a la siguiente url
`http://localhost:8181/realms/microservices-realm/.well-known/openid-configuration`.

Ahora, en Postman, en la parte inferior derecha hay una opción llamada `Cookies`, allí la abrimos y agregamos en
Manage Cookies un dominio `localhost`

![cookie postman](./assets/17.cookies-postman.png)

En la pestaña `Sync Cookies` nos pedirá que instalemos una extensión llamada `interceptor`. Lo instalamos en el
navegador y damos check en `Allow in InPrivate`:

![interceptor](./assets/18.interceptor.png)

Luego, regresamos a postman y damos click en el botón `Start Syncing`:

![sync-cookies](./assets/19.sync-cookies.png)

Listos, ahora sí cerramos la ventana de las coockies y damos click en el botón `Get New Access Token`. Se nos abrirá una
ventana del postman con el login de keycloak para loguearnos:

![login postman](./assets/20.login-postman.png)

Le damos click en `Use Token` y ahora sí podemos hacer la petición al microservicios ya que fuimos logueados
correctamente con keycloak:

![success](./assets/22.success.png)

## Propagando Bearer Token entre Microservicios

Recordemos que cuando registramos una orden en nuestro microservicio `orders-service`, internamente hace una petición al
microservicio `inventory-service` para ver si hay stock de los productos que se desean registrar. Ahora, como el
microservicio de  `inventory-service` también está segurizado necesitamos enviarle el `Token` de autenticación sino la
petición interna va a fallar.

Como estamos usando `RestClient`, una forma de propagar el token es usando inteceptores. Esta forma de propagar lo
obtuve de la documentación
oficial [RestTemplate support](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/bearer-tokens.html)
y lo adapté al RestClient:

````java

@Configuration
public class RestClientConfig {
    @LoadBalanced
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder().requestInterceptors(clientHttpRequestInterceptors -> {
            clientHttpRequestInterceptors.add((request, body, execution) -> {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                    return execution.execute(request, body);
                }

                if (!(authentication.getCredentials() instanceof AbstractOAuth2Token)) {
                    return execution.execute(request, body);
                }

                AbstractOAuth2Token token = (AbstractOAuth2Token) authentication.getCredentials();
                request.getHeaders().setBearerAuth(token.getTokenValue());
                return execution.execute(request, body);
            });
        });
    }
}
````

## Verificando propagación de token

Para verificar que el token se está propagando entre los microservicios vamos a realizar una petición desde postman
para poder registrar una orden, obviamente debemos primero generar el token usando keycloak. Observamos cómo es que la
petición llega al microservicio `inventory-service`, eso significa que la autenticación fue exitosa y luego completó el
flujo correctamente.

![propagando token](./assets/23.propagando-token.png)

## Limitando el acceso a recursos basado en roles

Para utilizar los roles en los endpoints necesitamos habilitar el uso de la anotación `@PreAuthorize` eso lo conseguimos
agregando la anotación `@EnableMethodSecurity` en el SecurityContext del microservicio `products-service`:

````java

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    /* other codes */
}
````

Luego agregamos la anotación `@PreAuthorize` en los endpoints a segurizar:

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/products")
public class ProductController {

    private final IProductService productService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(this.productService.getAllProducts());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addProduct(@RequestBody ProductRequest productRequest) {
        this.productService.addProduct(productRequest);
    }

}
````

Listo, de esa manera solo los usuarios con rol `ADMIN` podrán registrar un producto y los usuarios con rol `USER` podrán
listar los productos.

---

# Resiliencia y tolerancia a fallos

Para mostrar el funcionamiento de la tolerancia a fallos con `resilience4j` vamos a trabajar con el microservicio de
`orders-service`, con su endpoint para realizar una orden. Trabajaremos con ese método porque se comunica internamente
con el microservicio `inventory-service` para verificar si hay stock de los productos que se desean agregar. Entonces,
el punto clave está en `¿qué pasa si el microservicio inventory-service está caído?` tenemos que ver cómo tolerar ese
fallo, es decir, saber responder ante la caída de un microservicio y es ahí donde entra en juego el `circuit-breaker` de
`Resilience4j`.

Para poder trabajar con `resilience4j` necesitamos agregar la dependencia en el `pom.xml` del microservicio principal,
es decir, el microservicio que hace la llamada interna a otro microservicio, en nuestro caso es el `orders-service`.

````xml

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
    </dependency>
</dependencies>
````

Luego modificaremos el servicio para retornar un `OrderResponse`:

````java

@Service
public class OrderServiceImpl implements IOrderService {
    /* other metods */
    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest) {
        // Check for inventory
        BaseResponse response = this.restClient.post()
                .uri("/in-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .body(orderRequest.items())
                .retrieve()
                .body(BaseResponse.class);

        if (response == null || response.hasErrors()) {
            throw new IllegalArgumentException("Some of products are not in stock");
        }

        Order order = OrderMapper.mapToOrder(orderRequest);
        return OrderMapper.mapToOrderResponse(this.orderRepository.save(order));
    }
}
````

Como observamos en el método anterior, ese es el que hace la llamada al microservicio `inventory-service`, precisamente
esa llamada nos permitirá observar el comportamiento del `Circuit Breaker` cuando la llamada falle.

También modificamos el controlador `OrderController` quien no solamente retornará un `OrderResponse` sino al que lo
anotaremos con `@CircuitBreaker(...)` de `resilience4j`. Esto nos permitirá manejar los errores que se produzcan en ese
endpoint.

````java

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderController {
    /* others codes */
    @CircuitBreaker(name = "orders-service", fallbackMethod = "placeOrderFallback")
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = this.orderService.placeOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    private ResponseEntity<OrderResponse> placeOrderFallback(OrderRequest orderRequest, Throwable throwable) {
        log.info("Llamando al fallbackMethod placeOrderFallback(...)");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
````

Como se observa, tenemos nuestro endpoint `placeOrder` que tiene la anotación `@CircuitBreaker`, el
`name = "orders-service"` es el nombre de la propiedad que definiremos más adelante en el `application.yml`, mientras
que la propiedad `fallbackMethod = "placeOrderFallback"` hace referencia al método `placeOrderFallback(...)` que
definimos en la parte inferior. Este último método se llamará cuando el endpoint falle, entonces como requisito el
método a llamar debe tener la misma firma que el método que está anotado con `@CircuitBreaker`.

Finalmente, en el `application.yml` del microservicio `orders-service` agregamos la configuración del `Actuator` y del
`Resilience4j`. (NOTA: El Actuator nos permitirá ver detalles de nuestro microservicio como la salud, el estado, etc.)

````yaml
# Other configurations
#
# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health
      base-path: /actuator/orders
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true

# Resilience4j - Circuit Breaker
resilience4j.circuitbreaker:
  instances:
    orders-service:
      register-health-indicator: true
      sliding-window-size: 5
      sliding-window-type: count_based
      failure-rate-threshold: 50
      wait-duration-in-open-state: 10s
      automatic-transition-from-open-to-half-open-enabled: true
      permitted-number-of-calls-in-half-open-state: 3
````

Aprovechamos y agregamos la configuración de `Actuator` al `application.properties` de los microservicios de productos e
inventarios:

- Microservicio `products-service`:

````yaml
# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health
      base-path: /actuator/products
````

- Microservicio `inventory-service`:

````yaml
# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health
      base-path: /actuator/inventory
````

Finalmente, en el `application.yml` del microservicio `api-gateway` debemos configurar las rutas a los endpoints del
actuator que está en cada microservicio y por supuesto el propio actuator del api gateway:

````yml
spring:
  cloud:
    gateway:
      routes:
        # Others routes
        #
        # Inventory actuator routes
        - id: inventory-service-actuator-route
          uri: lb://inventory-service/actuator/inventory/**
          predicates:
            - Path=/actuator/inventory/**

        # Orders actuator routes
        - id: orders-service-actuator-route
          uri: lb://orders-service/actuator/orders/**
          predicates:
            - Path=/actuator/orders/**

        # Products actuator routes
        - id: products-service-actuator-route
          uri: lb://products-service/actuator/products/**
          predicates:
            - Path=/actuator/products/**

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health
      base-path: /actuator
````
