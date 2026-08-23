package br.com.sample.service.organizationalunits.api;

import java.util.List;

public record PaginaUnidadesResponse(List<UnidadeResponse> conteudo, int pn, int ps,
        long totalElementos, int totalPaginas) {
}
