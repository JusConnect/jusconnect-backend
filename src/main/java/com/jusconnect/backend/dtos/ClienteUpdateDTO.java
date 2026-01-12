package com.jusconnect.backend.dtos;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteUpdateDTO {

    private String nome;

    @Email(message = "Email deve ser válido")
    private String email;

    private String telefone;
    
    private String senha;
}