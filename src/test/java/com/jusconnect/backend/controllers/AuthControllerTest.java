package com.jusconnect.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.LoginRequestDTO;
import com.jusconnect.backend.models.Advogado;
import com.jusconnect.backend.models.Cliente;
import com.jusconnect.backend.repositories.AdvogadoRepository;
import com.jusconnect.backend.repositories.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TC05 e TC18: Testes de Autenticação")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClienteRepository clienteRepository;

    @MockitoBean
    private AdvogadoRepository advogadoRepository;

    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Cliente cliente;
    private Advogado advogado;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .id(1L)
                .nome("Artur Sousa")
                .cpf("12345678909")
                .senha("$2a$10$encodedPassword")
                .email("usuario@mailclient.xyz")
                .telefone("83912341234")
                .build();

        advogado = Advogado.builder()
                .id(1L)
                .nome("João")
                .cpf("12345678912")
                .senha("$2a$10$encodedPassword")
                .email("advogado@mailclient.xyz")
                .telefone("83988061717")
                .autodescricao("Advogado experiente")
                .area_de_atuacao("Direito criminal")
                .build();
    }

    @Nested
    @DisplayName("TC05: Feature - Login advogado")
    class LoginAdvogado {

        @Test
        @DisplayName("Scenario: Login bem-sucedido")
        void deveAutenticarAdvogadoComSucesso() throws Exception {
            // Given que o usuário não está logado
            // And acessa página de login
            
            // When o usuário informa CPF já cadastrado no sistema
            // And o usuário informa sua senha
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("12345678912")
                    .senha("123456")
                    .build();

            when(advogadoRepository.findByCpf("12345678912")).thenReturn(Optional.of(advogado));
            when(clienteRepository.findByCpf("12345678912")).thenReturn(Optional.empty());
            when(passwordEncoder.matches("123456", advogado.getSenha())).thenReturn(true);

            // Then o sistema autentica sua sessão e o usuário está logado
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADVOGADO"))
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("Scenario: Login mal-sucedido por senha errada")
        void deveRetornarErroQuandoSenhaErrada() throws Exception {
            // When o usuário informa CPF já cadastrado
            // And informa uma senha diferente da sua
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("12345678912")
                    .senha("senhaErrada")
                    .build();

            when(advogadoRepository.findByCpf("12345678912")).thenReturn(Optional.of(advogado));
            when(clienteRepository.findByCpf("12345678912")).thenReturn(Optional.empty());
            when(passwordEncoder.matches("senhaErrada", advogado.getSenha())).thenReturn(false);

            // Then o sistema exibe "Senha errada"
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("CPF ou senha inválidos")));
        }

        @Test
        @DisplayName("Scenario: Login mal-sucedido por CPF inexistente no sistema")
        void deveRetornarErroQuandoCpfNaoExiste() throws Exception {
            // When o usuário informa um CPF não cadastrado
            // And informa uma senha
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("99999999999")
                    .senha("123456")
                    .build();

            when(advogadoRepository.findByCpf("99999999999")).thenReturn(Optional.empty());
            when(clienteRepository.findByCpf("99999999999")).thenReturn(Optional.empty());

            // Then o sistema exibe "CPF não cadastrado"
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("CPF ou senha inválidos")));
        }
    }

    @Nested
    @DisplayName("TC18: Feature - Login do cliente")
    class LoginCliente {

        @Test
        @DisplayName("Scenario: Login bem-sucedido")
        void deveAutenticarClienteComSucesso() throws Exception {
            // Given que o usuário não está logado
            // And acessa página de login
            
            // When o usuário informa CPF já cadastrado no sistema
            // And o usuário informa sua senha já cadastrada
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("12345678909")
                    .senha("123456")
                    .build();

            when(clienteRepository.findByCpf("12345678909")).thenReturn(Optional.of(cliente));
            when(advogadoRepository.findByCpf("12345678909")).thenReturn(Optional.empty());
            when(passwordEncoder.matches("123456", cliente.getSenha())).thenReturn(true);

            // Then o sistema autentica sua sessão e o usuário está logado
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("CLIENTE"))
                    .andExpect(jsonPath("$.token").exists());
        }

        @Test
        @DisplayName("Scenario: Login mal-sucedido por senha errada")
        void deveRetornarErroQuandoSenhaErrada() throws Exception {
            // When o usuário informa CPF já cadastrado
            // And informa uma senha diferente da sua
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("12345678909")
                    .senha("senhaErrada")
                    .build();

            when(clienteRepository.findByCpf("12345678909")).thenReturn(Optional.of(cliente));
            when(advogadoRepository.findByCpf("12345678909")).thenReturn(Optional.empty());
            when(passwordEncoder.matches("senhaErrada", cliente.getSenha())).thenReturn(false);

            // Then o sistema exibe "Senha errada"
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("CPF ou senha inválidos")));
        }

        @Test
        @DisplayName("Scenario: Login mal-sucedido por CPF inexistente no sistema")
        void deveRetornarErroQuandoCpfNaoExiste() throws Exception {
            // When o usuário informa um CPF não cadastrado
            // And informa uma senha
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("99999999999")
                    .senha("123456")
                    .build();

            when(clienteRepository.findByCpf("99999999999")).thenReturn(Optional.empty());
            when(advogadoRepository.findByCpf("99999999999")).thenReturn(Optional.empty());

            // Then o sistema exibe "CPF não cadastrado"
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("CPF ou senha inválidos")));
        }
    }

    @Nested
    @DisplayName("Testes de Validação")
    class ValidacaoCampos {

        @Test
        @DisplayName("Deve retornar 400 quando CPF não informado")
        void deveRetornar400QuandoCpfVazio() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("")
                    .senha("123456")
                    .build();

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando senha não informada")
        void deveRetornar400QuandoSenhaVazia() throws Exception {
            LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                    .cpf("12345678909")
                    .senha("")
                    .build();

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}