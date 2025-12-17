package com.jusconnect.backend.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
}