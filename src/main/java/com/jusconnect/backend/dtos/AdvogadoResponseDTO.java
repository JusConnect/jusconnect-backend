package com.jusconnect.backend.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvogadoResponseDTO {

    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String autodescricao;
    private String area_de_atuacao;
}