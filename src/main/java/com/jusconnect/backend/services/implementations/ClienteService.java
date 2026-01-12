package com.jusconnect.backend.services.implementations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;
import com.jusconnect.backend.models.Cliente;
import com.jusconnect.backend.repositories.ClienteRepository;
import com.jusconnect.backend.services.interfaces.ClienteServiceInterface;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService implements ClienteServiceInterface {

    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public ClienteResponseDTO cadastrarCliente(ClienteRequestDTO request) {
        
        if (clienteRepository.existsByCpf(request.getCpf())) {
                throw new IllegalArgumentException("CPF já cadastrado");
                }
        
         // Hash the password
        String hashedSenha = passwordEncoder.encode(request.getSenha());

        Cliente cliente = Cliente.builder() 
                .nome(request.getNome())
                .cpf(request.getCpf())
                .senha(hashedSenha)
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .build();

            Cliente savedCliente = clienteRepository.save(cliente);

        return ClienteResponseDTO.builder()
                .id(savedCliente.getId())
                .nome(savedCliente.getNome())
                .cpf(savedCliente.getCpf())
                .email(savedCliente.getEmail())
                .telefone(savedCliente.getTelefone())
                .build();
    }

    @Override
    public ClienteResponseDTO visualizarPerfil(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nome(cliente.getNome())
                .cpf(cliente.getCpf())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .build();
    }

}
