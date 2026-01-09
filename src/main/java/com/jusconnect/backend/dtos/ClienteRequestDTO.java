package com.jusconnect.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Size(min = 11, max = 11, message = "CPF deve ter exatamente 11 dígitos")
    @NotBlank(message = "CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;
}
