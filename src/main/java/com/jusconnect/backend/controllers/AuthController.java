package com.jusconnect.backend.controllers;

import com.jusconnect.backend.config.JwtUtil;
import com.jusconnect.backend.dtos.LoginRequestDTO;
import com.jusconnect.backend.dtos.LoginResponseDTO;
import com.jusconnect.backend.models.Advogado;
import com.jusconnect.backend.models.Cliente;
import com.jusconnect.backend.repositories.AdvogadoRepository;
import com.jusconnect.backend.repositories.ClienteRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

// ...existing code...

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AdvogadoRepository advogadoRepository;
    private final ClienteRepository clienteRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        // Tenta autenticar como Advogado
        Advogado advogado = advogadoRepository.findByCpf(request.getCpf()).orElse(null);
        if (advogado != null && passwordMatches(request.getSenha(), advogado.getSenha())) {
                String token = jwtUtil.generateTokenForAdvogado(advogado.getId(), advogado.getEmail());
                return ResponseEntity.ok(LoginResponseDTO.builder()
                    .role("ADVOGADO")
                    .token(token)
                    .build());
        }

        // Tenta autenticar como Cliente
        Cliente cliente = clienteRepository.findByCpf(request.getCpf()).orElse(null);
        if (cliente != null && passwordMatches(request.getSenha(), cliente.getSenha())) {
                String token = jwtUtil.generateTokenForCliente(cliente.getId(), cliente.getEmail());
                return ResponseEntity.ok(LoginResponseDTO.builder()
                    .role("CLIENTE")
                    .token(token)
                    .build());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha inválidos");
    }

    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) return false;
        try {
            if (stored.startsWith("$2a") || stored.startsWith("$2b") || stored.startsWith("$2y")) {
                return passwordEncoder.matches(raw, stored);
            }
        } catch (Exception ignored) {}
        return stored.equals(raw);
    }
}
