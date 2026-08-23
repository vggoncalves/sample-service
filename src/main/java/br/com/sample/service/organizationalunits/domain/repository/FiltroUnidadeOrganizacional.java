package br.com.sample.service.organizationalunits.domain.repository;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import java.util.List;
import java.util.UUID;

public record FiltroUnidadeOrganizacional(String codigo, String nome, String sigla, TipoUnidade tipo,
        Boolean ativa, UUID unidadePaiId, Boolean raiz, int pagina, int tamanho, List<OrdenacaoUnidade> ordenacoes) {
}
