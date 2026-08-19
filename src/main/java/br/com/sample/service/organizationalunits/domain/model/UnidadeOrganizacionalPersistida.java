package br.com.sample.service.organizationalunits.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Estado persistido do módulo. As invariantes comportamentais serão introduzidas
 * no agregado de domínio na etapa seguinte.
 */
public record UnidadeOrganizacionalPersistida(
        UUID id,
        String codigo,
        String nome,
        String sigla,
        String descricao,
        TipoUnidade tipo,
        UUID unidadePaiId,
        String emailContato,
        String telefone,
        boolean ativa,
        Instant criadoEm,
        String criadoPor,
        Instant atualizadoEm,
        String atualizadoPor,
        long versao) {
}
