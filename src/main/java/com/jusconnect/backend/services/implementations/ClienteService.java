package com.jusconnect.backend.services.implementations;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;
import com.jusconnect.backend.dtos.ClienteUpdateDTO;
import com.jusconnect.backend.enums.StatusSolicitacao;
import com.jusconnect.backend.models.Cliente;
import com.jusconnect.backend.repositories.ClienteRepository;
import com.jusconnect.backend.repositories.SolicitacaoRepository;
import com.jusconnect.backend.services.interfaces.ClienteServiceInterface;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService implements ClienteServiceInterface {

    private final ClienteRepository clienteRepository;
    private final SolicitacaoRepository solicitacaoRepository;
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
    
    @Override
    public ClienteResponseDTO atualizarPerfil(Long id, ClienteUpdateDTO request) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        // Atualiza apenas os campos que foram fornecidos (não nulos)
        if (request.getNome() != null && !request.getNome().isBlank()) {
            cliente.setNome(request.getNome());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            cliente.setEmail(request.getEmail());
        }

        if (request.getTelefone() != null && !request.getTelefone().isBlank()) {
            cliente.setTelefone(request.getTelefone());
        }

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            String hashedSenha = passwordEncoder.encode(request.getSenha());
            cliente.setSenha(hashedSenha);
        }

        Cliente updatedCliente = clienteRepository.save(cliente);

        return ClienteResponseDTO.builder()
                .id(updatedCliente.getId())
                .nome(updatedCliente.getNome())
                .cpf(updatedCliente.getCpf())
                .email(updatedCliente.getEmail())
                .telefone(updatedCliente.getTelefone())
                .build();
    }

    @Override
    @Transactional
    public void deletarPerfil(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        // Verificar se há solicitações aceitas (impedimento para deletar)
        boolean temSolicitacoesAceitas = solicitacaoRepository
            .existsByClienteIdAndStatus(clienteId, StatusSolicitacao.ACEITA);

        if (temSolicitacoesAceitas) {
            throw new IllegalStateException(
                "Não é possível deletar o perfil. Você possui solicitações aceitas em andamento. " +
                "Por favor, finalize ou cancele-as antes de deletar sua conta."
            );
        }

        // Deletar todas as solicitações do cliente (cascata)
        // O JPA irá cuidar disso se configurado corretamente, mas podemos fazer manualmente
        solicitacaoRepository.deleteByClienteId(clienteId);

        // Deletar o cliente
        clienteRepository.delete(cliente);
    }

}
