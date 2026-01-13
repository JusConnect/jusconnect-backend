package com.jusconnect.backend.controllers;

import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;
import com.jusconnect.backend.dtos.ClienteUpdateDTO;
import com.jusconnect.backend.services.interfaces.ClienteServiceInterface;
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

import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Cliente", description = "Endpoints para operações de Cliente")
public class ClienteController {

	private final ClienteServiceInterface clienteService;
	private final JwtUtil jwtUtil;

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

	@Operation(
		summary = "Visualizar meu perfil (cliente logado)",
		description = "Retorna os dados do cliente logado a partir do token JWT.",
		responses = {
			@ApiResponse(responseCode = "200", description = "Perfil encontrado"),
			@ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
			@ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
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
			Long clienteId = claims.get("cid", Long.class);

			if (clienteId == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não contém identificador de cliente");
			}

			ClienteResponseDTO response = clienteService.visualizarPerfil(clienteId);
			return ResponseEntity.ok(response);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno do servidor");
		}
	}
	
	@Operation(
        summary = "Atualizar perfil do cliente",
        description = "Atualiza os dados do perfil do cliente autenticado. Apenas os campos fornecidos serão atualizados.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token não informado ou inválido"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
        }
    )
    @PutMapping("/me")
    public ResponseEntity<?> atualizarMeuPerfil(
            HttpServletRequest request,
            @Valid @RequestBody ClienteUpdateDTO updateRequest) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não informado ou inválido");
            }
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseAllClaims(token);
            Long clienteId = claims.get("cid", Long.class);
            if (clienteId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token não contém identificador de cliente");
            }
            ClienteResponseDTO response = clienteService.atualizarPerfil(clienteId, updateRequest);
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
