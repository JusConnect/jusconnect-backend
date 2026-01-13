package com.jusconnect.backend.controllers;

import com.jusconnect.backend.dtos.SolicitacaoRequestDTO;
import com.jusconnect.backend.dtos.SolicitacaoResponseDTO;
import com.jusconnect.backend.dtos.SolicitacaoUpdateDTO;
import com.jusconnect.backend.services.interfaces.SolicitacaoServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
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
    public ResponseEntity<?> criarSolicitacao(@Valid @RequestBody SolicitacaoRequestDTO request) {
        try {
            SolicitacaoResponseDTO response = solicitacaoService.criarSolicitacao(request);
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
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> listarSolicitacoesCliente(@PathVariable Long clienteId) {
        try {
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
    @DeleteMapping("/{solicitacaoId}/cliente/{clienteId}")
    public ResponseEntity<?> cancelarSolicitacao(
            @PathVariable Long solicitacaoId,
            @PathVariable Long clienteId) {
        try {
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
    @GetMapping("/advogado/{advogadoId}")
    public ResponseEntity<?> listarSolicitacoesAdvogado(@PathVariable Long advogadoId) {
        try {
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
    public ResponseEntity<?> listarSolicitacoesPublicas() {
        try {
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
    @PutMapping("/{solicitacaoId}/advogado/{advogadoId}")
    public ResponseEntity<?> responderSolicitacao(
            @PathVariable Long solicitacaoId,
            @PathVariable Long advogadoId,
            @Valid @RequestBody SolicitacaoUpdateDTO request) {
        try {
            SolicitacaoResponseDTO response = solicitacaoService.responderSolicitacao(solicitacaoId, advogadoId, request);
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
    @GetMapping("/{solicitacaoId}/cliente/{clienteId}/detalhes")
    public ResponseEntity<?> visualizarSolicitacaoCliente(
            @PathVariable Long solicitacaoId,
            @PathVariable Long clienteId) {
        try {
            SolicitacaoResponseDTO response = solicitacaoService.visualizarSolicitacaoCliente(solicitacaoId, clienteId);
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