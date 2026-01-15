package com.jusconnect.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.SolicitacaoRequestDTO;
import com.jusconnect.backend.dtos.SolicitacaoResponseDTO;
import com.jusconnect.backend.dtos.SolicitacaoUpdateDTO;
import com.jusconnect.backend.enums.StatusSolicitacao;
import com.jusconnect.backend.services.interfaces.SolicitacaoServiceInterface;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TC06, TC07, TC13, TC14, TC17: Testes de Solicitação")
class SolicitacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SolicitacaoServiceInterface solicitacaoService;

    @Autowired
    private JwtUtil jwtUtil;

    private String tokenCliente;
    private String tokenAdvogado;
    private SolicitacaoResponseDTO solicitacaoResponse;

    @BeforeEach
    void setUp() {
        tokenCliente = jwtUtil.generateTokenForCliente(1L, "cliente@mail.com");
        tokenAdvogado = jwtUtil.generateTokenForAdvogado(2L, "advogado@mail.com");

        solicitacaoResponse = SolicitacaoResponseDTO.builder()
                .id(1L)
                .descricao("Preciso de ajuda com processo trabalhista")
                .status(StatusSolicitacao.PENDENTE)
                .publica(false)
                .dataCriacao(LocalDateTime.now())
                .clienteId(1L)
                .clienteNome("Artur Sousa")
                .advogadoId(2L)
                .advogadoNome("João Advogado")
                .build();
    }

    @Nested
    @DisplayName("TC13: Feature - Cliente solicitar serviços de um advogado")
    class ClienteSolicitarServicos {

        @Test
        @DisplayName("Scenario: Cliente solicita advogado")
        void devePermitirClienteSolicitarAdvogado() throws Exception {
            // Given que o usuário está logado
            // And usuário entra na página de perfil de um advogado
            
            // When usuário clica em "Solicitar serviços"
            // And usuário informa descrição do serviço
            // And usuário clica em "confirmar"
            SolicitacaoRequestDTO request = SolicitacaoRequestDTO.builder()
                    .advogadoId(2L)
                    .descricao("Preciso de ajuda com processo trabalhista")
                    .publica(false)
                    .build();
            request.setClienteId(1L);

            when(solicitacaoService.criarSolicitacao(any(SolicitacaoRequestDTO.class)))
                    .thenReturn(solicitacaoResponse);

            // Then o Sistema deve emitir uma solicitação ao advogado e exibe "Advogado solicitado"
            mockMvc.perform(post("/solicitacoes")
                            .header("Authorization", "Bearer " + tokenCliente)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("PENDENTE"))
                    .andExpect(jsonPath("$.descricao").value("Preciso de ajuda com processo trabalhista"));
        }

        @Test
        @DisplayName("Scenario: Cliente já solicitou o advogado")
        void deveImpedirSolicitacaoDuplicada() throws Exception {
            // When usuário clicar em "Solicitar serviços"
            // And usuário já solicitou este advogado
            SolicitacaoRequestDTO request = SolicitacaoRequestDTO.builder()
                    .advogadoId(2L)
                    .descricao("Preciso de ajuda")
                    .publica(false)
                    .build();
            request.setClienteId(1L);

            when(solicitacaoService.criarSolicitacao(any(SolicitacaoRequestDTO.class)))
                    .thenThrow(new IllegalArgumentException("Você já possui uma solicitação pendente para este advogado"));

            // Then o Sistema exibe "Advogado já solicitado aguarde a resposta da solicitação"
            mockMvc.perform(post("/solicitacoes")
                            .header("Authorization", "Bearer " + tokenCliente)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("já possui uma solicitação pendente")));
        }

        @Test
        @DisplayName("Deve impedir advogado de criar solicitação")
        void deveImpedirAdvogadoCriarSolicitacao() throws Exception {
            SolicitacaoRequestDTO request = SolicitacaoRequestDTO.builder()
                    .advogadoId(null)
                    .descricao("Teste")
                    .publica(true)
                    .build();

            mockMvc.perform(post("/solicitacoes")
                            .header("Authorization", "Bearer " + tokenAdvogado)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("TC14: Feature - Cliente consultar solicitações")
    class ClienteConsultarSolicitacoes {

        @Test
        @DisplayName("Scenario: Usuário visualiza as solicitações")
        void devePermitirClienteVisualizarSuasSolicitacoes() throws Exception {
            // Given usuário está logado
            // And usuário entra na página de "Minhas Solicitações"
            
            // Given usuário já fez solicitações para advogados
            List<SolicitacaoResponseDTO> solicitacoes = Arrays.asList(
                    solicitacaoResponse,
                    SolicitacaoResponseDTO.builder()
                            .id(2L)
                            .descricao("Outra solicitação")
                            .status(StatusSolicitacao.ACEITA)
                            .publica(false)
                            .dataCriacao(LocalDateTime.now())
                            .clienteId(1L)
                            .clienteNome("Artur Sousa")
                            .build()
            );

            when(solicitacaoService.listarSolicitacoesCliente(1L)).thenReturn(solicitacoes);

            // Then o Sistema exibe as solicitações prévias e seus status
            mockMvc.perform(get("/solicitacoes/minhas")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].status").value("ACEITA"));
        }
    }

    @Nested
    @DisplayName("TC06: Feature - Advogado pode visualizar uma solicitação")
    class AdvogadoVisualizarSolicitacao {

        @Test
        @DisplayName("Scenario: Visualizar as solicitações")
        void devePermitirAdvogadoVisualizarSolicitacoes() throws Exception {
            // Given usuário está logado
            // When o usuário estiver na sua home
            
            List<SolicitacaoResponseDTO> solicitacoes = Arrays.asList(solicitacaoResponse);
            when(solicitacaoService.listarSolicitacoesAdvogado(2L)).thenReturn(solicitacoes);

            // Then o sistema deve exibir as solicitações enviadas pra ele
            mockMvc.perform(get("/solicitacoes/para-mim")
                            .header("Authorization", "Bearer " + tokenAdvogado))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].advogadoId").value(2));
        }

        @Test
        @DisplayName("Deve permitir visualizar solicitações públicas")
        void devePermitirAdvogadoVisualizarSolicitacoesPublicas() throws Exception {
            SolicitacaoResponseDTO solPublica = SolicitacaoResponseDTO.builder()
                    .id(3L)
                    .descricao("Solicitação pública")
                    .status(StatusSolicitacao.PENDENTE)
                    .publica(true)
                    .dataCriacao(LocalDateTime.now())
                    .clienteId(1L)
                    .clienteNome("Artur Sousa")
                    .build();

            when(solicitacaoService.listarSolicitacoesPublicas()).thenReturn(Arrays.asList(solPublica));

            mockMvc.perform(get("/solicitacoes/publicas")
                            .header("Authorization", "Bearer " + tokenAdvogado))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].publica").value(true));
        }

        @Test
        @DisplayName("Deve impedir cliente de ver solicitações públicas")
        void deveImpedirClienteVerSolicitacoesPublicas() throws Exception {
            mockMvc.perform(get("/solicitacoes/publicas")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("TC07: Feature - Advogado aceitar ou recusar uma solicitação")
    class AdvogadoResponderSolicitacao {

        @Test
        @DisplayName("Scenario: Aceitar solicitação")
        void devePermitirAdvogadoAceitarSolicitacao() throws Exception {
            // Given usuário está logado
            // And clica em uma solicitação
            
            // When usuário clica em "Aceitar"
            SolicitacaoUpdateDTO updateRequest = SolicitacaoUpdateDTO.builder()
                    .status(StatusSolicitacao.ACEITA)
                    .build();

            SolicitacaoResponseDTO aceitaResponse = SolicitacaoResponseDTO.builder()
                    .id(1L)
                    .descricao("Preciso de ajuda com processo trabalhista")
                    .status(StatusSolicitacao.ACEITA)
                    .publica(false)
                    .dataCriacao(LocalDateTime.now())
                    .dataResposta(LocalDateTime.now())
                    .clienteId(1L)
                    .clienteNome("Artur Sousa")
                    .clienteEmail("cliente@mail.com")
                    .clienteTelefone("83912341234")
                    .advogadoId(2L)
                    .advogadoNome("João Advogado")
                    .build();

            when(solicitacaoService.responderSolicitacao(eq(1L), eq(2L), any(SolicitacaoUpdateDTO.class)))
                    .thenReturn(aceitaResponse);

            // Then o sistema vai mudar o status da solicitação
            mockMvc.perform(put("/solicitacoes/1/responder")
                            .header("Authorization", "Bearer " + tokenAdvogado)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACEITA"))
                    .andExpect(jsonPath("$.clienteEmail").value("cliente@mail.com"))
                    .andExpect(jsonPath("$.clienteTelefone").value("83912341234"));
        }

        @Test
        @DisplayName("Scenario: Rejeitar solicitação")
        void devePermitirAdvogadoRejeitarSolicitacao() throws Exception {
            // When usuário clica em "Rejeitar"
            SolicitacaoUpdateDTO updateRequest = SolicitacaoUpdateDTO.builder()
                    .status(StatusSolicitacao.RECUSADA)
                    .build();

            SolicitacaoResponseDTO recusadaResponse = SolicitacaoResponseDTO.builder()
                    .id(1L)
                    .descricao("Preciso de ajuda com processo trabalhista")
                    .status(StatusSolicitacao.RECUSADA)
                    .publica(false)
                    .dataCriacao(LocalDateTime.now())
                    .dataResposta(LocalDateTime.now())
                    .clienteId(1L)
                    .clienteNome("Artur Sousa")
                    .advogadoId(2L)
                    .advogadoNome("João Advogado")
                    .build();

            when(solicitacaoService.responderSolicitacao(eq(1L), eq(2L), any(SolicitacaoUpdateDTO.class)))
                    .thenReturn(recusadaResponse);

            // Then o sistema não vai mudar o status da solicitação (para ACEITA)
            mockMvc.perform(put("/solicitacoes/1/responder")
                            .header("Authorization", "Bearer " + tokenAdvogado)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RECUSADA"));
        }
    }

    @Nested
    @DisplayName("TC17: Feature - Cancelamento de solicitação")
    class CancelamentoSolicitacao {

        @Test
        @DisplayName("Scenario: Usuário cancela uma solicitação")
        void devePermitirClienteCancelarSolicitacao() throws Exception {
            // Given o usuário está logado
            // And o usuário entra na página de solicitações
            // Given o usuário clica em "Cancelar solicitação"
            // And usuário clica em "Confirmar"
            
            doNothing().when(solicitacaoService).cancelarSolicitacao(1L, 1L);

            // Then o sistema deve apagar a solicitação
            mockMvc.perform(delete("/solicitacoes/1")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("cancelada com sucesso")));

            verify(solicitacaoService, times(1)).cancelarSolicitacao(1L, 1L);
        }

        @Test
        @DisplayName("Não deve permitir cancelar solicitação de outro cliente")
        void naoDevePermitirCancelarSolicitacaoDeOutroCliente() throws Exception {
            doThrow(new IllegalArgumentException("Você não tem permissão para cancelar esta solicitação"))
                    .when(solicitacaoService).cancelarSolicitacao(1L, 1L);

            mockMvc.perform(delete("/solicitacoes/1")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("não tem permissão")));
        }

        @Test
        @DisplayName("Não deve permitir cancelar solicitação já aceita")
        void naoDevePermitirCancelarSolicitacaoAceita() throws Exception {
            doThrow(new IllegalArgumentException("Apenas solicitações pendentes podem ser canceladas"))
                    .when(solicitacaoService).cancelarSolicitacao(1L, 1L);

            mockMvc.perform(delete("/solicitacoes/1")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("pendentes podem ser canceladas")));
        }
    }

    @Nested
    @DisplayName("Testes de Autorização")
    class TestesAutorizacao {

        @Test
        @DisplayName("Deve retornar 401 sem token")
        void deveRetornar401SemToken() throws Exception {
            mockMvc.perform(get("/solicitacoes/minhas"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve retornar 404 quando solicitação não encontrada")
        void deveRetornar404QuandoNaoEncontrada() throws Exception {
            when(solicitacaoService.visualizarSolicitacaoCliente(999L, 1L))
                    .thenThrow(new EntityNotFoundException("Solicitação não encontrada"));

            mockMvc.perform(get("/solicitacoes/999")
                            .header("Authorization", "Bearer " + tokenCliente))
                    .andExpect(status().isNotFound());
        }
    }
}