package com.jusconnect.backend.repositories;

import com.jusconnect.backend.enums.StatusSolicitacao;
import com.jusconnect.backend.models.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {

    // Buscar solicitações de um cliente específico
    List<Solicitacao> findByClienteId(Long clienteId);

    // Buscar solicitações direcionadas a um advogado específico
    List<Solicitacao> findByAdvogadoId(Long advogadoId);

    // Buscar solicitações públicas com status pendente
    List<Solicitacao> findByPublicaTrueAndStatus(StatusSolicitacao status);

    // Buscar todas as solicitações públicas
    List<Solicitacao> findByPublicaTrue();

    // Verificar se cliente já solicitou um advogado específico
    boolean existsByClienteIdAndAdvogadoIdAndStatus(Long clienteId, Long advogadoId, StatusSolicitacao status);
}