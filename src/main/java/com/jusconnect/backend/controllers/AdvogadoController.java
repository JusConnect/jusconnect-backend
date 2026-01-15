package com.jusconnect.backend.controllers;

import java.util.List;

import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.AdvogadoRequestDTO;
import com.jusconnect.backend.dtos.AdvogadoResponseDTO;
import com.jusconnect.backend.dtos.AdvogadoUpdateDTO;
import com.jusconnect.backend.services.interfaces.AdvogadoServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/advogados")
@RequiredArgsConstructor
@Tag(name = "Advogado", description = "Endpoints para operações de Advogado")
public class AdvogadoController {

    private final AdvogadoServiceInterface advogadoService;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "Listar advogados para busca (cliente)",
        description = "Retorna todos os perfis de advogados cadastrados em ordem alfabética para o cliente logado.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de advogados retornada"),
            @ApiResponse(responseCode = "401", description = "Token não informado ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso permitido apenas para clientes"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping
    public ResponseEntity<?> listarAdvogadosParaBusca(
            HttpServletRequest request,
            @RequestParam(value = "areaAtuacao", required = false) String areaAtuacao,
            @RequestParam(value = "tempoMinMeses", required = false) Integer tempoMinMeses) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            String role = claims.get("role", String.class);

            if (!"CLIENTE".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas clientes podem listar advogados para busca");
            }

            List<AdvogadoResponseDTO> advogados = advogadoService.buscarAdvogados(areaAtuacao, tempoMinMeses);
            return ResponseEntity.ok(advogados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Cadastrar novo advogado",
        description = "Cadastra um novo advogado no sistema.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Advogado cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou campos obrigatórios não preenchidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody AdvogadoRequestDTO request) {
        try {
            AdvogadoResponseDTO response = advogadoService.cadastrarAdvogado(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Visualizar meu perfil (advogado logado)",
        description = "Retorna os dados do advogado logado a partir do token JWT.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Advogado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping("/me")
    public ResponseEntity<?> visualizarMeuPerfil(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long advogadoId = claims.get("aid", Long.class);

            if (advogadoId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não contém identificador de advogado");
            }

            AdvogadoResponseDTO response = advogadoService.visualizarPerfil(advogadoId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Atualizar perfil do advogado autenticado",
        description = "Atualiza os dados do perfil do advogado autenticado. Apenas os campos fornecidos serão atualizados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token não informado ou inválido"),
            @ApiResponse(responseCode = "404", description = "Advogado não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @PutMapping("/me")
    public ResponseEntity<?> atualizarMeuPerfil(
            HttpServletRequest request,
            @Valid @RequestBody AdvogadoUpdateDTO updateRequest) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long advogadoId = claims.get("aid", Long.class);
            if (advogadoId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não contém identificador de advogado");
            }
            AdvogadoResponseDTO response = advogadoService.atualizarPerfil(advogadoId, updateRequest);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Deletar meu perfil",
        description = "Advogado deleta sua conta permanentemente. Não é possível deletar se houver solicitações aceitas em andamento.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Perfil deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token não informado ou inválido"),
            @ApiResponse(responseCode = "404", description = "Advogado não encontrado"),
            @ApiResponse(responseCode = "400", description = "Impossível deletar - há solicitações aceitas"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @DeleteMapping("/me")
    public ResponseEntity<?> deletarMeuPerfil(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long advogadoId = claims.get("aid", Long.class);

            if (advogadoId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não contém identificador de advogado");
            }

            advogadoService.deletarPerfil(advogadoId);
            return ResponseEntity.ok("Perfil deletado com sucesso. Sentiremos sua falta!");
            
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

}