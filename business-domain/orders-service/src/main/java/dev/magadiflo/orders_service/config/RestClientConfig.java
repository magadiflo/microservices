package dev.magadiflo.orders_service.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @LoadBalanced
    @Bean
    public RestClient.Builder restClientBuilder(ObservationRegistry observationRegistry) {
        return RestClient.builder()
                // Para propagar token a otros microservicios
                .requestInterceptors(clientHttpRequestInterceptors -> {
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
                })
                // Para ver el registro completo de las solicitudes (en zipkin) cuando hay comunicación entre varios microservicios
                .observationRegistry(observationRegistry)
                .observationConvention(new DefaultClientRequestObservationConvention());
    }
}
