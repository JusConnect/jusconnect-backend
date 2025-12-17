package com.jusconnect.backend.services.interfaces;

import com.jusconnect.backend.dtos.ClienteRequestDTO;
import com.jusconnect.backend.dtos.ClienteResponseDTO;

public interface ClienteServiceInterface {
    
    public ClienteResponseDTO cadastrarCliente (ClienteRequestDTO request);

}
