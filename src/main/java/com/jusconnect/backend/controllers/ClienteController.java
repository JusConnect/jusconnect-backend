package com.jusconnect.backend.controllers;

import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;
import com.jusconnect.backend.services.interfaces.ClienteServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Cliente", description = "Endpoints para operações de Cliente")
public class ClienteController {

	private final ClienteServiceInterface clienteService;

	@Operation(
		summary = "Cadastrar novo cliente",
		description = "Cadastra um novo cliente no sistema.",
		responses = {
			@ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso"),
			@ApiResponse(responseCode = "400", description = "Dados inválidos ou campos obrigatórios não preenchidos"),
			@ApiResponse(responseCode = "500", description = "Erro interno do servidor")
		}
	)
	@PostMapping
	public ResponseEntity<?> cadastrar(@Valid @RequestBody ClienteRequestDTO request) {
		try {
			ClienteResponseDTO response = clienteService.cadastrarCliente(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
		}
	}
}
