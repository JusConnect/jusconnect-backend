package com.jusconnect.backend.controllers;
import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.SolicitacaoRequestDTO;
import com.jusconnect.backend.dtos.SolicitacaoResponseDTO;
import com.jusconnect.backend.dtos.SolicitacaoUpdateDTO;
import com.jusconnect.backend.services.interfaces.SolicitacaoServiceInterface;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitacoes")
@RequiredArgsConstructor
@Tag(name = "Solicitação", description = "Endpoints para operações de Solicitação entre Clientes e Advogados")
public class SolicitacaoController {

    private final SolicitacaoServiceInterface solicitacaoService;
    private final JwtUtil jwtUtil;

    @Operation(
        summary = "Criar nova solicitação",
        description = "Cliente cria uma solicitação de serviço jurídico para um advogado específico ou pública.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou advogado já solicitado"),
            @ApiResponse(responseCode = "404", description = "Cliente ou Advogado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @PostMapping
    public ResponseEntity<?> criarSolicitacao(
            HttpServletRequest request,
            @Valid @RequestBody SolicitacaoRequestDTO requestDTO) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long clienteId = claims.get("cid", Long.class);
            String role = claims.get("role", String.class);

            if (clienteId == null || !"CLIENTE".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas clientes podem criar solicitações");
            }

            // Sobrescrever o clienteId do DTO com o do token (segurança)
            requestDTO.setClienteId(clienteId);

            SolicitacaoResponseDTO response = solicitacaoService.criarSolicitacao(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Listar solicitações do cliente",
        description = "Retorna todas as solicitações feitas por um cliente específico.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações retornada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping("/minhas")
    public ResponseEntity<?> listarMinhasSolicitacoes(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long clienteId = claims.get("cid", Long.class);
            String role = claims.get("role", String.class);

            if (clienteId == null || !"CLIENTE".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
            }

            List<SolicitacaoResponseDTO> response = solicitacaoService.listarSolicitacoesCliente(clienteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Cancelar solicitação",
        description = "Cliente cancela uma solicitação pendente.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Solicitação cancelada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Solicitação não pode ser cancelada"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @DeleteMapping("/{solicitacaoId}")
    public ResponseEntity<?> cancelarSolicitacao(
            HttpServletRequest request,
            @PathVariable Long solicitacaoId) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long clienteId = claims.get("cid", Long.class);
            String role = claims.get("role", String.class);

            if (clienteId == null || !"CLIENTE".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
            }

            solicitacaoService.cancelarSolicitacao(solicitacaoId, clienteId);
            return ResponseEntity.ok("Solicitação cancelada com sucesso");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Listar solicitações do advogado",
        description = "Retorna todas as solicitações direcionadas a um advogado específico.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações retornada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping("/para-mim")
    public ResponseEntity<?> listarSolicitacoesParaMim(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long advogadoId = claims.get("aid", Long.class);
            String role = claims.get("role", String.class);

            if (advogadoId == null || !"ADVOGADO".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
            }

            List<SolicitacaoResponseDTO> response = solicitacaoService.listarSolicitacoesAdvogado(advogadoId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Listar solicitações públicas",
        description = "Retorna todas as solicitações públicas pendentes visíveis para todos os advogados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações públicas retornada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping("/publicas")
    public ResponseEntity<?> listarSolicitacoesPublicas(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            String role = claims.get("role", String.class);

            if (!"ADVOGADO".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas advogados podem visualizar solicitações públicas");
            }

            List<SolicitacaoResponseDTO> response = solicitacaoService.listarSolicitacoesPublicas();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }

    @Operation(
        summary = "Responder solicitação",
        description = "Advogado aceita ou recusa uma solicitação.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Solicitação respondida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido ou solicitação já respondida"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @PutMapping("/{solicitacaoId}/responder")
    public ResponseEntity<?> responderSolicitacao(
            HttpServletRequest request,
            @PathVariable Long solicitacaoId,
            @Valid @RequestBody SolicitacaoUpdateDTO updateRequest) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long advogadoId = claims.get("aid", Long.class);
            String role = claims.get("role", String.class);

            if (advogadoId == null || !"ADVOGADO".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas advogados podem responder solicitações");
            }

            SolicitacaoResponseDTO response = solicitacaoService.responderSolicitacao(solicitacaoId, advogadoId, updateRequest);
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
        summary = "Visualizar solicitação específica - Cliente",
        description = "Cliente visualiza detalhes de uma solicitação que ele criou.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
            @ApiResponse(responseCode = "400", description = "Sem permissão para visualizar"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
   @GetMapping("/{solicitacaoId}")
    public ResponseEntity<?> visualizarSolicitacao(
            HttpServletRequest request,
            @PathVariable Long solicitacaoId) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }

            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            String role = claims.get("role", String.class);
            Long clienteId = claims.get("cid", Long.class);
            Long advogadoId = claims.get("aid", Long.class);

            SolicitacaoResponseDTO response;
            
            if ("CLIENTE".equals(role) && clienteId != null) {
                response = solicitacaoService.visualizarSolicitacaoCliente(solicitacaoId, clienteId);
            } else if ("ADVOGADO".equals(role) && advogadoId != null) {
                response = solicitacaoService.visualizarSolicitacaoAdvogado(solicitacaoId, advogadoId);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
            }

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
        summary = "Visualizar solicitação específica - Advogado",
        description = "Advogado visualiza detalhes de uma solicitação direcionada a ele ou pública.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Solicitação encontrada"),
            @ApiResponse(responseCode = "400", description = "Sem permissão para visualizar"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping("/{solicitacaoId}/advogado/{advogadoId}/detalhes")
    public ResponseEntity<?> visualizarSolicitacaoAdvogado(
            @PathVariable Long solicitacaoId,
            @PathVariable Long advogadoId) {
        try {
            SolicitacaoResponseDTO response = solicitacaoService.visualizarSolicitacaoAdvogado(solicitacaoId, advogadoId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }
}