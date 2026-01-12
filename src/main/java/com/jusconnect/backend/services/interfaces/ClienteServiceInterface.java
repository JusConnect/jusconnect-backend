package com.jusconnect.backend.services.interfaces;

import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;
import com.jusconnect.backend.dtos.ClienteUpdateDTO;

public interface ClienteServiceInterface {
    
    public ClienteResponseDTO cadastrarCliente (ClienteRequestDTO request);
    ClienteResponseDTO visualizarPerfil(Long id);
    ClienteResponseDTO atualizarPerfil(Long id, ClienteUpdateDTO request);

}