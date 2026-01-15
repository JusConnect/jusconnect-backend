package com.jusconnect.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.AdvogadoRequestDTO;
import com.jusconnect.backend.dtos.AdvogadoResponseDTO;
import com.jusconnect.backend.dtos.AdvogadoUpdateDTO;
import com.jusconnect.backend.services.interfaces.AdvogadoServiceInterface;
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
@DisplayName("TC01-TC08: Testes de Advogado")
class AdvogadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdvogadoServiceInterface advogadoService;

    @Autowired
    private JwtUtil jwtUtil;

    private String tokenAdvogado;
    private AdvogadoResponseDTO advogadoResponse;

    @BeforeEach
    void setUp() {
        tokenAdvogado = jwtUtil.generateTokenForAdvogado(1L, "advogado@mailclient.xyz");

        advogadoResponse = AdvogadoResponseDTO.builder()
                .id(1L)
                .nome("João")
                .cpf("12345678912")
                .email("advogado@mailclient.xyz")
                .telefone("83988061717")
                .autodescricao("Sou advogado em Campina Grande há mais de 28 anos")
                .area_de_atuacao("Direito criminal")
                .build();
    }

    @Nested
    @DisplayName("TC01: Feature - Cadastro de advogado")
    class CadastroAdvogado {

        @Test
        @DisplayName("Scenario: Cadastro bem sucedido")
        void deveCadastrarAdvogadoComSucesso() throws Exception {
            // Given que o usuário não está logado no sistema
            // And acessa página de criar conta
            // And escolhe a opção "Advogado"
            
            // When o usuário informa todos os dados válidos
            AdvogadoRequestDTO request = AdvogadoRequestDTO.builder()
                    .nome("João")
                    .cpf("12345678912")
                    .email("advogado@mailclient.xyz")
                    .telefone("83988061717")
                    .area_de_atuacao("Direito criminal")
                    .autodescricao("Sou advogado em Campina Grande há mais de 28 anos")
                    .senha("123456")
                    .build();

            when(advogadoService.cadastrarAdvogado(any(AdvogadoRequestDTO.class)))
                    .thenReturn(advogadoResponse);

            // Then o sistema deve exibir uma mensagem "Conta criada com sucesso"
            mockMvc.perform(post("/advogados")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nome").value("João"))
                    .andExpect(jsonPath("$.cpf").value("12345678912"))
                    .andExpect(jsonPath("$.area_de_atuacao").value("Direito criminal"));
        }

        @Test
        @DisplayName("Scenario: Tentativa de cadastro com dados inválidos")
        void deveRetornarErroQuandoDadosInvalidos() throws Exception {
            // When usuário informa o campo "cpf", "email" ou "numero de telefone" invalido
            AdvogadoRequestDTO request = AdvogadoRequestDTO.builder()
                    .nome("João")
                    .cpf("123") // CPF inválido
                    .email("email-invalido") // Email inválido
                    .telefone("83988061717")
                    .area_de_atuacao("Direito criminal")
                    .autodescricao("Descrição")
                    .senha("123456")
                    .build();

            // Then o sistema deve exibir uma mensagem "Dados fornecidos inválidos"
            mockMvc.perform(post("/advogados")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Scenario: Tentativa de cadastro com campos obrigatórios vazios")
        void deveRetornarErroQuandoCamposObrigatoriosVazios() throws Exception {
            // When o usuário deixa campos obrigatórios em branco
            AdvogadoRequestDTO request = AdvogadoRequestDTO.builder()
                    .nome("") // Nome vazio
                    .cpf("")  // CPF vazio
                    .email("advogado@mailclient.xyz")
                    .telefone("")
                    .area_de_atuacao("Direito criminal")
                    .autodescricao("Descrição")
                    .senha("")
                    .build();

            // Then o sistema deve exibir uma mensagem "Campos obrigatórios devem ser preenchidos"
            mockMvc.perform(post("/advogados")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Scenario: Tentativa de cadastro com CPF já existente no Sistema")
        void deveRetornarErroQuandoCpfJaExiste() throws Exception {
            // When o usuário informa um cpf válido que já está cadastrado
            AdvogadoRequestDTO request = AdvogadoRequestDTO.builder()
                    .nome("João")
                    .cpf("12345678912")
                    .email("advogado@mailclient.xyz")
                    .telefone("83988061717")
                    .area_de_atuacao("Direito criminal")
                    .autodescricao("Descrição")
                    .senha("123456")
                    .build();

            when(advogadoService.cadastrarAdvogado(any(AdvogadoRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException("CPF já cadastrado"));

            // Then o sistema deve exibir uma mensagem "CPF já cadastrado"
            mockMvc.perform(post("/advogados")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("CPF já cadastrado"));
        }
    }

    @Nested
    @DisplayName("TC02: Feature - Atualização de dados do advogado")
    class AtualizacaoAdvogado {

        @Test
        @DisplayName("Scenario: Atualização de dados bem-sucedida")
        void deveAtualizarDadosComSucesso() throws Exception {
            // Given que o usuário está logado no sistema
            // And clica na opção "Editar perfil"
            
            // When o usuário atualiza os dados
            AdvogadoUpdateDTO updateDTO = AdvogadoUpdateDTO.builder()
                    .telefone("83981810909")
                    .autodescricao("Nova descrição atualizada")
                    .email("advogado2@mailclient.xyz")
                    .build();

            AdvogadoResponseDTO updatedResponse = AdvogadoResponseDTO.builder()
                    .id(1L)
                    .nome("João")
                    .cpf("12345678912")
                    .email("advogado2@mailclient.xyz")
                    .telefone("83981810909")
                    .autodescricao("Nova descrição atualizada")
                    .area_de_atuacao("Direito criminal")
                    .build();

            when(advogadoService.atualizarPerfil(eq(1L), any(AdvogadoUpdateDTO.class)))
                    .thenReturn(updatedResponse);

            // Then o sistema deve exibir uma mensagem "Informações atualizadas"
            mockMvc.perform(put("/advogados/me")
                            .header("Authorization", "Bearer " + tokenAdvogado)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.telefone").value("83981810909"))
                    .andExpect(jsonPath("$.email").value("advogado2@mailclient.xyz"));
        }
    }

    @Nested
    @DisplayName("TC03: Feature - Deletar perfil do advogado")
    class DeletarPerfilAdvogado {

        @Test
        @DisplayName("Scenario: Remoção de perfil bem-sucedida")
        void deveDeletarPerfilComSucesso() throws Exception {
            // Given que o usuário está logado no sistema
            // And clica na opção "Deletar perfil"
            // When usuário clica em "Tem certeza?"
            
            doNothing().when(advogadoService).deletarPerfil(1L);

            // Then o sistema apaga os dados do advogado
            // And exibe uma mensagem "Conta deletada"
            mockMvc.perform(delete("/advogados/me")
                            .header("Authorization", "Bearer " + tokenAdvogado))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("deletado com sucesso")));

            verify(advogadoService, times(1)).deletarPerfil(1L);
        }

        @Test
        @DisplayName("Scenario: Não deve deletar com solicitações aceitas")
        void naoDeveDeletarComSolicitacoesAceitas() throws Exception {
            doThrow(new IllegalStateException("Não é possível deletar o perfil. Você possui solicitações aceitas em andamento."))
                    .when(advogadoService).deletarPerfil(1L);

            mockMvc.perform(delete("/advogados/me")
                            .header("Authorization", "Bearer " + tokenAdvogado))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("solicitações aceitas")));
        }
    }

    @Nested
    @DisplayName("TC04: Feature - Visualização de perfil do advogado")
    class VisualizacaoPerfilAdvogado {

        @Test
        @DisplayName("Scenario: Usuário consegue visualizar suas informações")
        void deveVisualizarPerfilComSucesso() throws Exception {
            // Given usuário está logado
            // Given usuário entra na página de perfil
            
            when(advogadoService.visualizarPerfil(1L)).thenReturn(advogadoResponse);

            // Then Usuário visualiza nome, cpf, email e número de telefone
            mockMvc.perform(get("/advogados/me")
                            .header("Authorization", "Bearer " + tokenAdvogado))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("João"))
                    .andExpect(jsonPath("$.cpf").value("12345678912"))
                    .andExpect(jsonPath("$.email").value("advogado@mailclient.xyz"))
                    .andExpect(jsonPath("$.telefone").value("83988061717"));
        }

        @Test
        @DisplayName("Scenario: Deve retornar 401 sem token de autenticação")
        void deveRetornar401SemToken() throws Exception {
            mockMvc.perform(get("/advogados/me"))
					.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Scenario: Deve retornar 404 quando advogado não encontrado")
        void deveRetornar404QuandoNaoEncontrado() throws Exception {
            when(advogadoService.visualizarPerfil(1L))
                    .thenThrow(new EntityNotFoundException("Advogado não encontrado"));

            mockMvc.perform(get("/advogados/me")
                            .header("Authorization", "Bearer " + tokenAdvogado))
                    .andExpect(status().isNotFound());
        }
    }
}