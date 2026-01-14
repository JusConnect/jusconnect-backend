package com.jusconnect.backend.services.implementations;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jusconnect.backend.dtos.AdvogadoRequestDTO;
import com.jusconnect.backend.dtos.AdvogadoResponseDTO;
import com.jusconnect.backend.dtos.AdvogadoUpdateDTO;
import com.jusconnect.backend.enums.StatusSolicitacao;
import com.jusconnect.backend.models.Advogado;
import com.jusconnect.backend.repositories.AdvogadoRepository;
import com.jusconnect.backend.repositories.SolicitacaoRepository;
import com.jusconnect.backend.services.interfaces.AdvogadoServiceInterface;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdvogadoService implements AdvogadoServiceInterface{

    private final AdvogadoRepository advogadoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public AdvogadoResponseDTO cadastrarAdvogado(AdvogadoRequestDTO request) {

        if (advogadoRepository.existsByCpf(request.getCpf())) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        
        // Hash the password
        String hashedSenha = passwordEncoder.encode(request.getSenha());

        Advogado advogado = Advogado.builder()
                .nome(request.getNome())
                .cpf(request.getCpf())
                .senha(hashedSenha)
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .autodescricao(request.getAutodescricao())
                .area_de_atuacao(request.getArea_de_atuacao())
                .build();

        Advogado savedAdvogado = advogadoRepository.save(advogado);

        return AdvogadoResponseDTO.builder()
                .id(savedAdvogado.getId())
                .nome(savedAdvogado.getNome())
                .cpf(savedAdvogado.getCpf())
                .email(savedAdvogado.getEmail())
                .telefone(savedAdvogado.getTelefone())
                .autodescricao(savedAdvogado.getAutodescricao())
                .area_de_atuacao(savedAdvogado.getArea_de_atuacao())
                .build();
    }

    @Override
    public AdvogadoResponseDTO visualizarPerfil(Long id) {
        Advogado advogado = advogadoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Advogado não encontrado"));

        return AdvogadoResponseDTO.builder()
                .id(advogado.getId())
                .nome(advogado.getNome())
                .cpf(advogado.getCpf())
                .email(advogado.getEmail())
                .telefone(advogado.getTelefone())
                .autodescricao(advogado.getAutodescricao())
                .area_de_atuacao(advogado.getArea_de_atuacao())
                .build();
    }

    @Override
    public AdvogadoResponseDTO atualizarPerfil(Long id, AdvogadoUpdateDTO request) {
        Advogado advogado = advogadoRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Advogado não encontrado"));

        // Atualiza apenas os campos que foram fornecidos (não nulos)
        if (request.getNome() != null && !request.getNome().isBlank()) {
            advogado.setNome(request.getNome());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            advogado.setEmail(request.getEmail());
        }

        if (request.getTelefone() != null && !request.getTelefone().isBlank()) {
            advogado.setTelefone(request.getTelefone());
        }

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            String hashedSenha = passwordEncoder.encode(request.getSenha());
            advogado.setSenha(hashedSenha);
        }

        if (request.getAutodescricao() != null && !request.getAutodescricao().isBlank()) {
            advogado.setAutodescricao(request.getAutodescricao());
        }

        if (request.getArea_de_atuacao() != null && !request.getArea_de_atuacao().isBlank()) {
            advogado.setArea_de_atuacao(request.getArea_de_atuacao());
        }

        Advogado updatedAdvogado = advogadoRepository.save(advogado);

        return AdvogadoResponseDTO.builder()
                .id(updatedAdvogado.getId())
                .nome(updatedAdvogado.getNome())
                .cpf(updatedAdvogado.getCpf())
                .email(updatedAdvogado.getEmail())
                .telefone(updatedAdvogado.getTelefone())
                .autodescricao(updatedAdvogado.getAutodescricao())
                .area_de_atuacao(updatedAdvogado.getArea_de_atuacao())
                .build();
    }

    @Override
    @Transactional
    public void deletarPerfil(Long advogadoId) {
        Advogado advogado = advogadoRepository.findById(advogadoId)
            .orElseThrow(() -> new EntityNotFoundException("Advogado não encontrado"));

        // Verificar se há solicitações aceitas (impedimento para deletar)
        boolean temSolicitacoesAceitas = solicitacaoRepository
            .existsByAdvogadoIdAndStatus(advogadoId, StatusSolicitacao.ACEITA);

        if (temSolicitacoesAceitas) {
            throw new IllegalStateException(
                "Não é possível deletar o perfil. Você possui solicitações aceitas em andamento. " +
                "Por favor, finalize ou recuse-as antes de deletar sua conta."
            );
        }

        solicitacaoRepository.deleteByAdvogadoId(advogadoId);
        
        advogadoRepository.delete(advogado);
    }

    @Override
    public List<AdvogadoResponseDTO> listarAdvogadosOrdenadosPorNome() {
        List<Advogado> advogados = advogadoRepository.findAllByOrderByNomeAsc();

        return advogados.stream()
                .map(advogado -> AdvogadoResponseDTO.builder()
                        .id(advogado.getId())
                        .nome(advogado.getNome())
                        .cpf(advogado.getCpf())
                        .email(advogado.getEmail())
                        .telefone(advogado.getTelefone())
                        .autodescricao(advogado.getAutodescricao())
                        .area_de_atuacao(advogado.getArea_de_atuacao())
                        .build())
                .collect(Collectors.toList());
    }

                @Override
                public List<AdvogadoResponseDTO> buscarAdvogados(String areaAtuacao, Integer tempoMinMeses) {
                List<Advogado> advogados = advogadoRepository.findAllByOrderByNomeAsc();

                // Filtro por área de atuação (String normalizada)
                if (areaAtuacao != null && !areaAtuacao.isBlank()) {
                    String filtroNormalizado = areaAtuacao.trim().toLowerCase(Locale.ROOT);
                    advogados = advogados.stream()
                        .filter(a -> a.getArea_de_atuacao() != null
                            && a.getArea_de_atuacao().trim().toLowerCase(Locale.ROOT).equals(filtroNormalizado))
                        .collect(Collectors.toList());
                }

                // Filtro por tempo mínimo na plataforma em meses
                if (tempoMinMeses != null && tempoMinMeses > 0) {
                    LocalDateTime agora = LocalDateTime.now();
                    advogados = advogados.stream()
                        .filter(a -> a.getDataCadastro() != null
                            && ChronoUnit.MONTHS.between(a.getDataCadastro(), agora) >= tempoMinMeses)
                        .collect(Collectors.toList());
                }

                return advogados.stream()
                    .map(advogado -> AdvogadoResponseDTO.builder()
                        .id(advogado.getId())
                        .nome(advogado.getNome())
                        .cpf(advogado.getCpf())
                        .email(advogado.getEmail())
                        .telefone(advogado.getTelefone())
                        .autodescricao(advogado.getAutodescricao())
                        .area_de_atuacao(advogado.getArea_de_atuacao())
                        .build())
                    .collect(Collectors.toList());
                }

}