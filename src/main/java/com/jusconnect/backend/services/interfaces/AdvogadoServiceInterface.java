package com.jusconnect.backend.services.interfaces;

import java.util.List;

import com.jusconnect.backend.dtos.*;

public interface AdvogadoServiceInterface {

    public AdvogadoResponseDTO cadastrarAdvogado(AdvogadoRequestDTO request);
    AdvogadoResponseDTO visualizarPerfil(Long id);
    AdvogadoResponseDTO atualizarPerfil(Long id, AdvogadoUpdateDTO request);
    void deletarPerfil(Long advogadoId);

    List<AdvogadoResponseDTO> listarAdvogadosOrdenadosPorNome();

    List<AdvogadoResponseDTO> buscarAdvogados(String areaAtuacao, Integer tempoMinMeses);

}
