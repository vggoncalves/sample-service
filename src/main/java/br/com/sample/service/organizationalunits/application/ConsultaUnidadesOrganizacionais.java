package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import java.util.UUID;

public record ConsultaUnidadesOrganizacionais(String codigo, String nome, String sigla, TipoUnidade tipo,
        Boolean ativa, UUID unidadePaiId, Boolean raiz, int pagina, int tamanho, String sort) {
}
