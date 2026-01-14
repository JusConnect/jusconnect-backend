package com.jusconnect.backend.dtos;

import com.jusconnect.backend.enums.StatusSolicitacao;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoUpdateDTO {

    @NotNull(message = "Status é obrigatório")
    private StatusSolicitacao status; // ACEITA, RECUSADA, CANCELADA
}