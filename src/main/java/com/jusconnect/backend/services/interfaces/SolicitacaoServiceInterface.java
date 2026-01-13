package com.jusconnect.backend.services.interfaces;

import com.jusconnect.backend.dtos.SolicitacaoRequestDTO;
import com.jusconnect.backend.dtos.SolicitacaoResponseDTO;
import com.jusconnect.backend.dtos.SolicitacaoUpdateDTO;

import java.util.List;

public interface SolicitacaoServiceInterface {

    // Cliente cria uma solicitação
    SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO request);

    // Cliente visualiza suas próprias solicitações
    List<SolicitacaoResponseDTO> listarSolicitacoesCliente(Long clienteId);

    // Cliente cancela sua solicitação
    void cancelarSolicitacao(Long solicitacaoId, Long clienteId);

    // Advogado visualiza solicitações direcionadas a ele
    List<SolicitacaoResponseDTO> listarSolicitacoesAdvogado(Long advogadoId);

    // Advogado visualiza solicitações públicas
    List<SolicitacaoResponseDTO> listarSolicitacoesPublicas();

    // Advogado aceita ou recusa uma solicitação
    SolicitacaoResponseDTO responderSolicitacao(Long solicitacaoId, Long advogadoId, SolicitacaoUpdateDTO request);

    // Visualizar uma solicitação específica - Cliente
    SolicitacaoResponseDTO visualizarSolicitacaoCliente(Long solicitacaoId, Long clienteId);
    
    // Visualizar uma solicitação específica - Advogado
    SolicitacaoResponseDTO visualizarSolicitacaoAdvogado(Long solicitacaoId, Long advogadoId);
}