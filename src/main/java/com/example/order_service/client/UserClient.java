
package com.example.order_service.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserClient {

    private final RestClient restClient;

    public UserClient(
            @LoadBalanced RestClient.Builder builder) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);

        this.restClient = builder
                .baseUrl("http://user-service")
                .requestFactory(factory)
                .build();
    }

    public void validateUser(Long userId) {
        try {
            restClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot place order: user not found",
                        ex
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "User Service returned an unexpected response",
                    ex
            );

        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to contact User Service",
                    ex
            );
        }
    }
}
