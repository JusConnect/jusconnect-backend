package com.jusconnect.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;
import com.jusconnect.backend.dtos.ClienteUpdateDTO;
import com.jusconnect.backend.services.interfaces.ClienteServiceInterface;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TC09-TC12: Testes de Cliente")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteServiceInterface clienteService;

    @Autowired
    private JwtUtil jwtUtil;

    private String tokenCliente;
    private ClienteResponseDTO clienteResponse;

    @BeforeEach
    void setUp() {
        tokenCliente = jwtUtil.generateTokenForCliente(1L, "usuario@mailclient.xyz");

        clienteResponse = ClienteResponseDTO.builder()
                .id(1L)
                .nome("Artur Sousa")
                .cpf("12345678909")
                .email("usuario@mailclient.xyz")
                .telefone("83912341234")
                .build();
    }

    @Nested
    @DisplayName("TC09: Feature - Cadastro de cliente")
    class CadastroCliente {

        @Test
        @DisplayName("Scenario: Criação de conta bem sucedida")
        void deveCadastrarClienteComSucesso() throws Exception {
            // Given o usuário não está logado no sistema
            // And acessa página de criar conta
            // And escolhe opção cliente
            
            // When usuário informa todos os dados válidos
            ClienteRequestDTO request = ClienteRequestDTO.builder()
                    .nome("Artur Sousa")
                    .cpf("12345678909")
                    .email("usuario@mailclient.xyz")
                    .telefone("83912341234")
                    .senha("123456")
                    .build();

            when(clienteService.cadastrarCliente(any(ClienteRequestDTO.class)))
                    .thenReturn(clienteResponse);

            // Then o sistema deve exibir uma mensagem de "Conta criada com sucesso"
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("Artur Sousa"))
                    .andExpect(jsonPath("$.cpf").value("12345678909"))
                    .andExpect(jsonPath("$.email").value("usuario@mailclient.xyz"))
                    .andExpect(jsonPath("$.telefone").value("83912341234"));
        }

        @Test
        @DisplayName("Scenario: Tentativa de cadastro com dados inválidos")
        void deveRetornarErroQuandoDadosInvalidos() throws Exception {
            // When usuário informa o campo "cpf", "email" ou "número de telefone" inválido
            ClienteRequestDTO request = ClienteRequestDTO.builder()
                    .nome("Artur Sousa")
                    .cpf("123") // CPF inválido
                    .email("email-invalido") // Email inválido
                    .telefone("83912341234")
                    .senha("123456")
                    .build();

            // Then o sistema deve exibir uma mensagem "Dados fornecidos inválidos"
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Scenario: Tentativa de cadastro com CPF já existente no Sistema")
        void deveRetornarErroQuandoCpfJaExiste() throws Exception {
            // When o usuário informa um cpf válido que já está cadastrado
            ClienteRequestDTO request = ClienteRequestDTO.builder()
                    .nome("Artur Sousa")
                    .cpf("12345678909")
                    .email("usuario@mailclient.xyz")
                    .telefone("83912341234")
                    .senha("123456")
                    .build();

            when(clienteService.cadastrarCliente(any(ClienteRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException("CPF já cadastrado"));

            // Then o sistema deve exibir uma mensagem "CPF já cadastrado"
            mockMvc.perform(post("/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("CPF já cadastrado"));
        }
    }

    @Nested
    @DisplayName("TC10: Feature - Visualização de perfil do cliente")
    class VisualizacaoPerfilCliente {

        @Test
        @DisplayName("Scenario: Usuário consegue visualizar suas informações")
        void deveVisualizarPerfilComSucesso() throws Exception {
            // Given usuário está logado
            // Given usuário entra na página de perfil
            
            when(clienteService.visualizarPerfil(1L)).thenReturn(clienteResponse);

            // Then Usuário visualiza nome, cpf, email e número de telefone
            mockMvc.perform(get("/clientes/me")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Artur Sousa"))
                    .andExpect(jsonPath("$.cpf").value("12345678909"))
                    .andExpect(jsonPath("$.email").value("usuario@mailclient.xyz"))
                    .andExpect(jsonPath("$.telefone").value("83912341234"));
        }

        @Test
        @DisplayName("Scenario: Deve retornar 401 sem token de autenticação")
        void deveRetornar401SemToken() throws Exception {
            mockMvc.perform(get("/clientes/me"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Scenario: Deve retornar 404 quando cliente não encontrado")
        void deveRetornar404QuandoNaoEncontrado() throws Exception {
            when(clienteService.visualizarPerfil(1L))
                    .thenThrow(new EntityNotFoundException("Cliente não encontrado"));

            mockMvc.perform(get("/clientes/me")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("TC11: Feature - Atualização de dados do cliente")
    class AtualizacaoCliente {

        @Test
        @DisplayName("Scenario: Atualização de dados bem-sucedida")
        void deveAtualizarDadosComSucesso() throws Exception {
            // Given que o usuário está logado no sistema
            // And clica na opção "Editar perfil"
            
            // When o usuário atualiza os dados
            ClienteUpdateDTO updateDTO = ClienteUpdateDTO.builder()
                    .telefone("83981810909")
                    .email("advogado2@mailclient.xyz")
                    .build();

            ClienteResponseDTO updatedResponse = ClienteResponseDTO.builder()
                    .id(1L)
                    .nome("Artur Sousa")
                    .cpf("12345678909")
                    .email("advogado2@mailclient.xyz")
                    .telefone("83981810909")
                    .build();

            when(clienteService.atualizarPerfil(eq(1L), any(ClienteUpdateDTO.class)))
                    .thenReturn(updatedResponse);

            // Then o sistema deve exibir uma mensagem "Informações atualizadas"
            mockMvc.perform(put("/clientes/me")
                            .header("Authorization", "Bearer " + tokenCliente)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.telefone").value("83981810909"))
                    .andExpect(jsonPath("$.email").value("advogado2@mailclient.xyz"));
        }
    }

    @Nested
    @DisplayName("TC12: Feature - Deletar perfil do cliente")
    class DeletarPerfilCliente {

        @Test
        @DisplayName("Scenario: Remoção de perfil bem-sucedida")
        void deveDeletarPerfilComSucesso() throws Exception {
            // Given que o usuário está logado no sistema
            // And clica na opção "Deletar perfil"
            // When usuário clica em "Tem certeza?"
            
            doNothing().when(clienteService).deletarPerfil(1L);

            // Then o sistema apaga os dados do cliente
            // And exibe uma mensagem "Conta deletada"
            mockMvc.perform(delete("/clientes/me")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("deletado com sucesso")));

            verify(clienteService, times(1)).deletarPerfil(1L);
        }

        @Test
        @DisplayName("Scenario: Não deve deletar com solicitações aceitas")
        void naoDeveDeletarComSolicitacoesAceitas() throws Exception {
            doThrow(new IllegalStateException("Não é possível deletar o perfil. Você possui solicitações aceitas em andamento."))
                    .when(clienteService).deletarPerfil(1L);

            mockMvc.perform(delete("/clientes/me")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("solicitações aceitas")));
        }
    }
}