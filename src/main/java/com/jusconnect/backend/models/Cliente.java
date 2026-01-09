package com.jusconnect.backend.models;

import lombok.*;
import jakarta.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "clientes")

public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String telefone;

}