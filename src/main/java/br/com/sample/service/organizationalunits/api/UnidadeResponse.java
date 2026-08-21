package br.com.sample.service.organizationalunits.api;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import java.time.Instant;
import java.util.UUID;

public record UnidadeResponse(UUID id, String codigo, String nome, String sigla, String descricao,
        TipoUnidade tipo, UUID unidadePaiId, String emailContato, String telefone, boolean ativa,
        Instant criadoEm, String criadoPor, Instant atualizadoEm, String atualizadoPor, long versao) {
}
