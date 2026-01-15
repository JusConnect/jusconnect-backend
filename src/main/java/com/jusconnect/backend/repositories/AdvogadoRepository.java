package com.jusconnect.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jusconnect.backend.models.Advogado;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvogadoRepository extends JpaRepository<Advogado, Long> {

    Optional<Advogado> findByCpf(String cpf);
    boolean existsByCpf(String cpf);

    List<Advogado> findAllByOrderByNomeAsc();

}