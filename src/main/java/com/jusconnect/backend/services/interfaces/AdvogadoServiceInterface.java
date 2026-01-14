package com.jusconnect.backend.services.interfaces;

import com.jusconnect.backend.dtos.*;

public interface AdvogadoServiceInterface {

    public AdvogadoResponseDTO cadastrarAdvogado(AdvogadoRequestDTO request);
    AdvogadoResponseDTO visualizarPerfil(Long id);
    AdvogadoResponseDTO atualizarPerfil(Long id, AdvogadoUpdateDTO request);
    void deletarPerfil(Long advogadoId);

}
