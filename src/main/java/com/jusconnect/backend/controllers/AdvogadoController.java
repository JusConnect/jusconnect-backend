package com.jusconnect.backend.controllers;

import com.jusconnect.backend.dtos.AdvogadoRequestDTO;
import com.jusconnect.backend.dtos.AdvogadoResponseDTO;
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

@RestController
@RequestMapping("/advogados")
@RequiredArgsConstructor
@Tag(name = "Advogado", description = "Endpoints para operações de Advogado")
public class AdvogadoController {

    private final AdvogadoServiceInterface advogadoService;

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
        summary = "Visualizar perfil do advogado",
        description = "Retorna os dados do advogado pelo ID.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Perfil encontrado"),
            @ApiResponse(responseCode = "404", description = "Advogado não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> visualizarPerfil(@PathVariable Long id) {
        try {
            AdvogadoResponseDTO response = advogadoService.visualizarPerfil(id);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
        }
    }
}