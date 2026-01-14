package com.jusconnect.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jusconnect.backend.dtos.*;
import com.jusconnect.backend.enums.StatusSolicitacao;
import com.jusconnect.backend.repositories.AdvogadoRepository;
import com.jusconnect.backend.repositories.ClienteRepository;
import com.jusconnect.backend.repositories.SolicitacaoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Teste de Integração - Fluxo Completo Baseado em Casos de Aceitação")
class FluxoCompletoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AdvogadoRepository advogadoRepository;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    private static String tokenCliente;
    private static String tokenAdvogado;
    private static Long clienteId;
    private static Long advogadoId;
    private static Long solicitacaoId;

    @BeforeEach
    void setUp() {
        solicitacaoRepository.deleteAll();
        clienteRepository.deleteAll();
        advogadoRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("TC09 + TC01: Cadastro de Cliente e Advogado")
    void deveCadastrarClienteEAdvogado() throws Exception {
        // TC09: Cadastro de cliente
        // Scenario: Criação de conta bem sucedida
        ClienteRequestDTO clienteRequest = ClienteRequestDTO.builder()
                .nome("Artur Sousa")
                .cpf("12345678909")
                .email("usuario@mailclient.xyz")
                .telefone("83912341234")
                .senha("123456")
                .build();

        MvcResult clienteResult = mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Artur Sousa"))
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andReturn();

        ClienteResponseDTO clienteResponse = objectMapper.readValue(
                clienteResult.getResponse().getContentAsString(),
                ClienteResponseDTO.class
        );
        clienteId = clienteResponse.getId();

        // TC01: Cadastro de advogado
        // Scenario: Cadastro bem sucedido
        AdvogadoRequestDTO advogadoRequest = AdvogadoRequestDTO.builder()
                .nome("João")
                .cpf("12345678912")
                .email("advogado@mailclient.xyz")
                .telefone("83988061717")
                .area_de_atuacao("Direito criminal")
                .autodescricao("Sou advogado em Campina Grande há mais de 28 anos")
                .senha("123456")
                .build();

        MvcResult advogadoResult = mockMvc.perform(post("/advogados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(advogadoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.area_de_atuacao").value("Direito criminal"))
                .andReturn();

        AdvogadoResponseDTO advogadoResponse = objectMapper.readValue(
                advogadoResult.getResponse().getContentAsString(),
                AdvogadoResponseDTO.class
        );
        advogadoId = advogadoResponse.getId();
    }

    @Test
    @Order(2)
    @DisplayName("TC18 + TC05: Login de Cliente e Advogado")
    void deveRealizarLoginClienteEAdvogado() throws Exception {
        deveCadastrarClienteEAdvogado();

        // TC18: Login do cliente
        // Scenario: Login bem-sucedido
        LoginRequestDTO loginCliente = LoginRequestDTO.builder()
                .cpf("12345678909")
                .senha("123456")
                .build();

        MvcResult loginClienteResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginCliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("CLIENTE"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        LoginResponseDTO loginClienteResponse = objectMapper.readValue(
                loginClienteResult.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );
        tokenCliente = loginClienteResponse.getToken();

        // TC05: Login advogado
        // Scenario: Login bem-sucedido
        LoginRequestDTO loginAdvogado = LoginRequestDTO.builder()
                .cpf("12345678912")
                .senha("123456")
                .build();

        MvcResult loginAdvogadoResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAdvogado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADVOGADO"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        LoginResponseDTO loginAdvogadoResponse = objectMapper.readValue(
                loginAdvogadoResult.getResponse().getContentAsString(),
                LoginResponseDTO.class
        );
        tokenAdvogado = loginAdvogadoResponse.getToken();
    }

    @Test
    @Order(3)
    @DisplayName("TC10 + TC04: Visualização de Perfil")
    void deveVisualizarPerfilClienteEAdvogado() throws Exception {
        deveRealizarLoginClienteEAdvogado();

        // TC10: Visualização de perfil do cliente
        // Scenario: Usuário consegue visualizar suas informações
        mockMvc.perform(get("/clientes/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Artur Sousa"))
                .andExpect(jsonPath("$.cpf").value("12345678909"))
                .andExpect(jsonPath("$.email").value("usuario@mailclient.xyz"))
                .andExpect(jsonPath("$.telefone").value("83912341234"));

        // TC04: Visualização de perfil do advogado
        // Scenario: Usuário consegue visualizar suas informações
        mockMvc.perform(get("/advogados/me")
                        .header("Authorization", "Bearer " + tokenAdvogado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João"))
                .andExpect(jsonPath("$.cpf").value("12345678912"))
                .andExpect(jsonPath("$.email").value("advogado@mailclient.xyz"))
                .andExpect(jsonPath("$.telefone").value("83988061717"));
    }

    @Test
    @Order(4)
    @DisplayName("TC13: Cliente solicitar serviços de um advogado")
    void devePermitirClienteSolicitarAdvogado() throws Exception {
        deveRealizarLoginClienteEAdvogado();

        // TC13: Feature - Cliente solicitar serviços de um advogado
        // Scenario: Cliente solicita advogado
        SolicitacaoRequestDTO solicitacaoRequest = SolicitacaoRequestDTO.builder()
                .advogadoId(advogadoId)
                .descricao("Preciso de ajuda com processo trabalhista")
                .publica(false)
                .build();
        solicitacaoRequest.setClienteId(clienteId);

        MvcResult solicitacaoResult = mockMvc.perform(post("/solicitacoes")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.descricao").value("Preciso de ajuda com processo trabalhista"))
                .andReturn();

        SolicitacaoResponseDTO solicitacaoResponse = objectMapper.readValue(
                solicitacaoResult.getResponse().getContentAsString(),
                SolicitacaoResponseDTO.class
        );
        solicitacaoId = solicitacaoResponse.getId();
    }

    @Test
    @Order(5)
    @DisplayName("TC14: Cliente consultar solicitações")
    void devePermitirClienteConsultarSolicitacoes() throws Exception {
        devePermitirClienteSolicitarAdvogado();

        // TC14: Feature - Cliente consultar solicitações
        // Scenario: Usuário visualiza as solicitações
        mockMvc.perform(get("/solicitacoes/minhas")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(solicitacaoId))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].descricao").value("Preciso de ajuda com processo trabalhista"));
    }

    @Test
    @Order(6)
    @DisplayName("TC06: Advogado visualizar solicitações")
    void devePermitirAdvogadoVisualizarSolicitacoes() throws Exception {
        devePermitirClienteSolicitarAdvogado();

        // TC06: Feature - Advogado pode visualizar uma solicitação
        // Scenario: Visualizar as solicitações
        mockMvc.perform(get("/solicitacoes/para-mim")
                        .header("Authorization", "Bearer " + tokenAdvogado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(solicitacaoId))
                .andExpect(jsonPath("$[0].advogadoId").value(advogadoId));
    }

    @Test
    @Order(7)
    @DisplayName("TC07: Advogado aceitar solicitação")
    void devePermitirAdvogadoAceitarSolicitacao() throws Exception {
        devePermitirClienteSolicitarAdvogado();

        // TC07: Feature - Advogado aceitar ou recusar uma solicitação
        // Scenario: Aceitar solicitação
        SolicitacaoUpdateDTO updateRequest = SolicitacaoUpdateDTO.builder()
                .status(StatusSolicitacao.ACEITA)
                .build();

        mockMvc.perform(put("/solicitacoes/" + solicitacaoId + "/responder")
                        .header("Authorization", "Bearer " + tokenAdvogado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"))
                .andExpect(jsonPath("$.clienteEmail").value("usuario@mailclient.xyz"))
                .andExpect(jsonPath("$.clienteTelefone").value("83912341234"));
    }

    @Test
    @Order(8)
    @DisplayName("TC11 + TC02: Atualização de Perfil")
    void devePermitirAtualizarPerfil() throws Exception {
        deveRealizarLoginClienteEAdvogado();

        // TC11: Atualização de dados do cliente
        // Scenario: Atualização de dados bem-sucedida
        ClienteUpdateDTO clienteUpdate = ClienteUpdateDTO.builder()
                .telefone("83981810909")
                .email("novo@mailclient.xyz")
                .build();

        mockMvc.perform(put("/clientes/me")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("83981810909"))
                .andExpect(jsonPath("$.email").value("novo@mailclient.xyz"));

        // TC02: Atualização de dados do advogado
        // Scenario: Atualização de dados bem-sucedida
        AdvogadoUpdateDTO advogadoUpdate = AdvogadoUpdateDTO.builder()
                .telefone("83981810909")
                .autodescricao("Nova descrição atualizada")
                .email("advogado2@mailclient.xyz")
                .build();

        mockMvc.perform(put("/advogados/me")
                        .header("Authorization", "Bearer " + tokenAdvogado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(advogadoUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("83981810909"))
                .andExpect(jsonPath("$.email").value("advogado2@mailclient.xyz"));
    }

    @Test
    @Order(9)
    @DisplayName("TC17: Cancelamento de solicitação")
    void devePermitirClienteCancelarSolicitacao() throws Exception {
        devePermitirClienteSolicitarAdvogado();

        // TC17: Feature - Cancelamento de usuário
        // Scenario: Usuário cancela uma solicitação
        mockMvc.perform(delete("/solicitacoes/" + solicitacaoId)
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("cancelada com sucesso")));
    }

    @Test
    @Order(10)
    @DisplayName("TC12 + TC03: Deletar perfil sem solicitações aceitas")
    void devePermitirDeletarPerfilSemSolicitacoesAceitas() throws Exception {
        deveRealizarLoginClienteEAdvogado();

        // TC12: Deletar perfil do cliente
        // Scenario: Remoção de perfil bem-sucedida
        mockMvc.perform(delete("/clientes/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deletado com sucesso")));

        // TC03: Deletar perfil do advogado
        // Scenario: Remoção de perfil bem-sucedida
        mockMvc.perform(delete("/advogados/me")
                        .header("Authorization", "Bearer " + tokenAdvogado))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("deletado com sucesso")));
    }

    @Test
    @Order(11)
    @DisplayName("Fluxo: Não deve permitir deletar perfil com solicitação aceita")
    void naoDevePermitirDeletarPerfilComSolicitacaoAceita() throws Exception {
        devePermitirAdvogadoAceitarSolicitacao();

        // Cliente não deve conseguir deletar com solicitação aceita
        mockMvc.perform(delete("/clientes/me")
                        .header("Authorization", "Bearer " + tokenCliente))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("solicitações aceitas")));

        // Advogado não deve conseguir deletar com solicitação aceita
        mockMvc.perform(delete("/advogados/me")
                        .header("Authorization", "Bearer " + tokenAdvogado))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("solicitações aceitas")));
    }
}