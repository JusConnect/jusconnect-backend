package com.jusconnect.backend.dtos;

import jakarta.validation.constraints.Email;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvogadoUpdateDTO {

    private String nome;

    @Email(message = "Email deve ser válido")
    private String email;

    private String telefone;
    
    private String senha;
    
    private String autodescricao;
    
    private String area_de_atuacao;
}
