package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.repository.FiltroUnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.OrdenacaoUnidade;
import br.com.sample.service.organizationalunits.domain.repository.PaginaUnidades;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarUnidadesOrganizacionaisUseCase {
    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of(
            "codigo", "nome", "sigla", "tipo", "ativa", "criadoEm", "atualizadoEm");
    private final UnidadeOrganizacionalRepository repository;

    public ListarUnidadesOrganizacionaisUseCase(UnidadeOrganizacionalRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public PaginaUnidades executar(ConsultaUnidadesOrganizacionais consulta) {
        if (consulta.pagina() < 0 || consulta.tamanho() < 1 || consulta.tamanho() > 100) {
            throw new ConsultaUnidadeInvalidaException("Parâmetros de paginação inválidos");
        }
        return repository.buscar(new FiltroUnidadeOrganizacional(normalizarMaiusculo(consulta.codigo()),
                normalizar(consulta.nome()), normalizarMaiusculo(consulta.sigla()), consulta.tipo(), consulta.ativa(),
                consulta.unidadePaiId(), consulta.raiz(), consulta.pagina(), consulta.tamanho(), ordenar(consulta.sort())));
    }

    private static List<OrdenacaoUnidade> ordenar(String sort) {
        var termos = sort == null || sort.isBlank() ? new String[] {"nome", "codigo"} : sort.split(",");
        return java.util.Arrays.stream(termos).map(String::trim).map(ListarUnidadesOrganizacionaisUseCase::ordenacao)
                .toList();
    }

    private static OrdenacaoUnidade ordenacao(String termo) {
        var descendente = termo.startsWith("-");
        var campo = descendente ? termo.substring(1) : termo;
        if (!CAMPOS_ORDENAVEIS.contains(campo)) {
            throw new ConsultaUnidadeInvalidaException("Campo de ordenação não permitido: " + campo);
        }
        return new OrdenacaoUnidade(campo, descendente);
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String normalizarMaiusculo(String valor) {
        var normalizado = normalizar(valor);
        return normalizado == null ? null : normalizado.toUpperCase(java.util.Locale.ROOT);
    }
}
