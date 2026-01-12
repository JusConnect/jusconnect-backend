package com.jusconnect.backend.services.implementations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jusconnect.backend.dtos.AdvogadoRequestDTO;
import com.jusconnect.backend.dtos.AdvogadoResponseDTO;
import com.jusconnect.backend.models.Advogado;
import com.jusconnect.backend.repositories.AdvogadoRepository;
import com.jusconnect.backend.services.interfaces.AdvogadoServiceInterface;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdvogadoService implements AdvogadoServiceInterface{

    private final AdvogadoRepository advogadoRepository;
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
}