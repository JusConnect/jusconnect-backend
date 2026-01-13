package com.jusconnect.backend.dtos;

import com.jusconnect.backend.enums.StatusSolicitacao;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoResponseDTO {

    private Long id;
    private String descricao;
    private StatusSolicitacao status;
    private Boolean publica;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataResposta;

    // Dados do cliente (apenas após aceite ou para o próprio cliente)
    private Long clienteId;
    private String clienteNome;
    private String clienteEmail; // Só visível após aceite
    private String clienteTelefone; // Só visível após aceite

    // Dados do advogado
    private Long advogadoId;
    private String advogadoNome;
}