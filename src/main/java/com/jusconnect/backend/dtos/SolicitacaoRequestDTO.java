package com.jusconnect.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoRequestDTO {

    @NotNull(message = "ID do cliente é obrigatório")
    private Long clienteId;

    private Long advogadoId; // Opcional - se null, é uma solicitação pública

    @NotBlank(message = "Descrição da demanda é obrigatória")
    private String descricao;

    @NotNull(message = "Informe se a solicitação é pública")
    private Boolean publica; // true = broadcast para todos, false = direcionada
}