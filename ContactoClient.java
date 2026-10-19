package com.trans.service.clients;

import com.trans.service.dto.ContactoResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ContactoClient {

    private final RestClient restClient;

    public ContactoClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8083")
                .build();
    }

    public ContactoResponseDto validarContacto(Long idContacto, String tokenJwt) {
        try {
            return restClient.get()
                    .uri("/api/contactos/{id}", idContacto)
                    .header("Authorization", tokenJwt)
                    .retrieve()
                    .body(ContactoResponseDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}