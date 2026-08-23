package br.com.sample.service.organizationalunits.domain.repository;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import java.util.List;

public record PaginaUnidades(List<UnidadeOrganizacional> conteudo, int pagina, int tamanho,
        long totalElementos, int totalPaginas) {
}
