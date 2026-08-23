package br.com.sample.service.organizationalunits.api;

import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.PaginaUnidades;
import org.springframework.stereotype.Component;

@Component
public class UnidadeOrganizacionalMapper {
    public DadosBasicosUnidade paraDadosBasicos(CriarUnidadeRequest request) {
        return new DadosBasicosUnidade(request.nome(), request.sigla(), request.descricao(), request.tipo(),
                request.unidadePaiId(), request.emailContato(), request.telefone());
    }

    public UnidadeResponse paraResponse(UnidadeOrganizacional unidade) {
        return new UnidadeResponse(unidade.id(), unidade.codigo(), unidade.nome(), unidade.sigla(), unidade.descricao(),
                unidade.tipo(), unidade.unidadePaiId(), unidade.emailContato(), unidade.telefone(), unidade.ativa(),
                unidade.criadoEm(), unidade.criadoPor(), unidade.atualizadoEm(), unidade.atualizadoPor(), unidade.versao());
    }

    public PaginaUnidadesResponse paraResponse(PaginaUnidades pagina) {
        return new PaginaUnidadesResponse(pagina.conteudo().stream().map(this::paraResponse).toList(), pagina.pagina(),
                pagina.tamanho(), pagina.totalElementos(), pagina.totalPaginas());
    }
}
