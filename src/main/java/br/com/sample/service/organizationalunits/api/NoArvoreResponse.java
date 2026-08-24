package br.com.sample.service.organizationalunits.api;

import java.util.List;

public record NoArvoreResponse(UnidadeResponse unidade, List<NoArvoreResponse> filhas) {
}
