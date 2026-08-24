package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import java.util.List;

public record NoArvoreUnidade(UnidadeOrganizacional unidade, List<NoArvoreUnidade> filhas) {
}
