package br.com.sample.service.organizationalunits.application;

import java.util.UUID;

public record EventoAdministrativoUnidade(String acao, UUID unidadeId, String responsavel) {
}
