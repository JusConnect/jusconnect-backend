package com.jusconnect.backend.enums;

public enum StatusSolicitacao {
    PENDENTE("Aguardando resposta do advogado"),
    ACEITA("Solicitação aceita pelo advogado"),
    RECUSADA("Solicitação recusada pelo advogado"),
    CANCELADA("Solicitação cancelada pelo cliente");

    private final String descricao;

    StatusSolicitacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
