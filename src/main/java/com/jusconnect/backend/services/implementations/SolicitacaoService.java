package com.jusconnect.backend.services.implementations;

import com.jusconnect.backend.dtos.SolicitacaoRequestDTO;
import com.jusconnect.backend.dtos.SolicitacaoResponseDTO;
import com.jusconnect.backend.dtos.SolicitacaoUpdateDTO;
import com.jusconnect.backend.enums.StatusSolicitacao;
import com.jusconnect.backend.models.Advogado;
import com.jusconnect.backend.models.Cliente;
import com.jusconnect.backend.models.Solicitacao;
import com.jusconnect.backend.repositories.AdvogadoRepository;
import com.jusconnect.backend.repositories.ClienteRepository;
import com.jusconnect.backend.repositories.SolicitacaoRepository;
import com.jusconnect.backend.services.interfaces.SolicitacaoServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitacaoService implements SolicitacaoServiceInterface {

    private final SolicitacaoRepository solicitacaoRepository;
    private final ClienteRepository clienteRepository;
    private final AdvogadoRepository advogadoRepository;

    @Override
    public SolicitacaoResponseDTO criarSolicitacao(SolicitacaoRequestDTO request) {
        // Validar cliente
        Cliente cliente = clienteRepository.findById(request.getClienteId())
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        Advogado advogado = null;

        // Se não for pública, validar advogado
        if (!request.getPublica() && request.getAdvogadoId() != null) {
            advogado = advogadoRepository.findById(request.getAdvogadoId())
                .orElseThrow(() -> new EntityNotFoundException("Advogado não encontrado"));

            // Verificar se cliente já solicitou este advogado (evitar duplicação)
            if (solicitacaoRepository.existsByClienteIdAndAdvogadoIdAndStatus(
                    request.getClienteId(), 
                    request.getAdvogadoId(), 
                    StatusSolicitacao.PENDENTE)) {
                throw new IllegalArgumentException("Você já possui uma solicitação pendente para este advogado");
            }
        }

        Solicitacao solicitacao = Solicitacao.builder()
                .descricao(request.getDescricao())
                .status(StatusSolicitacao.PENDENTE)
                .publica(request.getPublica())
                .cliente(cliente)
                .advogado(advogado)
                .build();

        Solicitacao savedSolicitacao = solicitacaoRepository.save(solicitacao);

        return buildResponseDTO(savedSolicitacao, false);
    }

    @Override
    public List<SolicitacaoResponseDTO> listarSolicitacoesCliente(Long clienteId) {
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByClienteId(clienteId);
        return solicitacoes.stream()
                .map(s -> buildResponseDTO(s, true)) // Cliente pode ver seus próprios dados
                .collect(Collectors.toList());
    }

    @Override
    public void cancelarSolicitacao(Long solicitacaoId, Long clienteId) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        // Verificar se a solicitação pertence ao cliente
        if (!solicitacao.getCliente().getId().equals(clienteId)) {
            throw new IllegalArgumentException("Você não tem permissão para cancelar esta solicitação");
        }

        // Apenas solicitações pendentes podem ser canceladas
        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Apenas solicitações pendentes podem ser canceladas");
        }

        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        solicitacao.setDataResposta(LocalDateTime.now());
        solicitacaoRepository.save(solicitacao);
    }

    @Override
    public List<SolicitacaoResponseDTO> listarSolicitacoesAdvogado(Long advogadoId) {
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByAdvogadoId(advogadoId);
        return solicitacoes.stream()
                .map(s -> buildResponseDTO(s, s.getStatus() == StatusSolicitacao.ACEITA)) // Só mostra contato se aceita
                .collect(Collectors.toList());
    }

    @Override
    public List<SolicitacaoResponseDTO> listarSolicitacoesPublicas() {
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByPublicaTrueAndStatus(StatusSolicitacao.PENDENTE);
        return solicitacoes.stream()
                .map(s -> buildResponseDTO(s, false)) // Não mostra dados de contato em públicas
                .collect(Collectors.toList());
    }

    @Override
    public SolicitacaoResponseDTO responderSolicitacao(Long solicitacaoId, Long advogadoId, SolicitacaoUpdateDTO request) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        // Verificar se a solicitação é para este advogado
        if (solicitacao.getAdvogado() != null && !solicitacao.getAdvogado().getId().equals(advogadoId)) {
            throw new IllegalArgumentException("Esta solicitação não foi direcionada para você");
        }

        // Verificar se ainda está pendente
        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalArgumentException("Esta solicitação já foi respondida");
        }

        // Validar status (só pode ser ACEITA ou RECUSADA)
        if (request.getStatus() != StatusSolicitacao.ACEITA && 
            request.getStatus() != StatusSolicitacao.RECUSADA) {
            throw new IllegalArgumentException("Status inválido. Use ACEITA ou RECUSADA");
        }

        // Se for solicitação pública, atribuir o advogado
        if (solicitacao.getAdvogado() == null) {
            Advogado advogado = advogadoRepository.findById(advogadoId)
                .orElseThrow(() -> new EntityNotFoundException("Advogado não encontrado"));
            solicitacao.setAdvogado(advogado);
        }

        solicitacao.setStatus(request.getStatus());
        solicitacao.setDataResposta(LocalDateTime.now());
        
        Solicitacao updatedSolicitacao = solicitacaoRepository.save(solicitacao);

        return buildResponseDTO(updatedSolicitacao, request.getStatus() == StatusSolicitacao.ACEITA);
    }

    @Override
    public SolicitacaoResponseDTO visualizarSolicitacaoCliente(Long solicitacaoId, Long clienteId) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        // Cliente só pode ver suas próprias solicitações
        if (!solicitacao.getCliente().getId().equals(clienteId)) {
            throw new IllegalArgumentException("Você não tem permissão para visualizar esta solicitação");
        }

        // Cliente sempre vê seus próprios dados de contato
        return buildResponseDTO(solicitacao, true);
    }

     @Override
    public SolicitacaoResponseDTO visualizarSolicitacaoAdvogado(Long solicitacaoId, Long advogadoId) {
        Solicitacao solicitacao = solicitacaoRepository.findById(solicitacaoId)
            .orElseThrow(() -> new EntityNotFoundException("Solicitação não encontrada"));

        // Advogado pode ver se foi direcionada a ele ou se é pública
        boolean isDirecionada = solicitacao.getAdvogado() != null && 
                               solicitacao.getAdvogado().getId().equals(advogadoId);
        boolean isPublica = solicitacao.getPublica();
        
        if (!isDirecionada && !isPublica) {
            throw new IllegalArgumentException("Você não tem permissão para visualizar esta solicitação");
        }
        
        // Só mostra contato do cliente se a solicitação foi aceita
        boolean mostrarContato = solicitacao.getStatus() == StatusSolicitacao.ACEITA;
        return buildResponseDTO(solicitacao, mostrarContato);
    }

    // Método auxiliar para construir o DTO de resposta
    private SolicitacaoResponseDTO buildResponseDTO(Solicitacao solicitacao, boolean incluirDadosContato) {
        SolicitacaoResponseDTO.SolicitacaoResponseDTOBuilder builder = SolicitacaoResponseDTO.builder()
                .id(solicitacao.getId())
                .descricao(solicitacao.getDescricao())
                .status(solicitacao.getStatus())
                .publica(solicitacao.getPublica())
                .dataCriacao(solicitacao.getDataCriacao())
                .dataResposta(solicitacao.getDataResposta())
                .clienteId(solicitacao.getCliente().getId())
                .clienteNome(solicitacao.getCliente().getNome());

        // Adicionar dados de contato apenas se permitido
        if (incluirDadosContato) {
            builder.clienteEmail(solicitacao.getCliente().getEmail())
                   .clienteTelefone(solicitacao.getCliente().getTelefone());
        }

        // Adicionar dados do advogado se existir
        if (solicitacao.getAdvogado() != null) {
            builder.advogadoId(solicitacao.getAdvogado().getId())
                   .advogadoNome(solicitacao.getAdvogado().getNome());
        }

        return builder.build();
    }
}