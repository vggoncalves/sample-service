package br.com.sample.service.organizationalunits.application;

import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultarArvoreUnidadesUseCase {
    private static final int LIMITE_NOS = 1000;
    private final UnidadeOrganizacionalRepository repository;

    public ConsultarArvoreUnidadesUseCase(UnidadeOrganizacionalRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<NoArvoreUnidade> executar(UUID raizId, Boolean ativa, int profundidade) {
        if (profundidade < 1 || profundidade > 20) throw new ArvoreUnidadeInvalidaException();
        var visitados = new HashSet<UUID>();
        var raizes = raizId == null ? repository.buscarRaizes() : List.of(repository.buscarPorId(raizId)
                .orElseThrow(() -> new UnidadeOrganizacionalNaoEncontradaException(raizId)));
        return raizes.stream().filter(unidade -> ativa == null || unidade.ativa() == ativa)
                .map(unidade -> montar(unidade, ativa, profundidade, visitados)).toList();
    }

    private NoArvoreUnidade montar(UnidadeOrganizacional unidade, Boolean ativa, int profundidade,
            HashSet<UUID> visitados) {
        if (!visitados.add(unidade.id()) || visitados.size() > LIMITE_NOS) throw new LimiteArvoreExcedidoException();
        var filhas = profundidade == 1 ? List.<NoArvoreUnidade>of() : repository.buscarFilhasDiretas(unidade.id()).stream()
                .filter(filha -> ativa == null || filha.ativa() == ativa)
                .map(filha -> montar(filha, ativa, profundidade - 1, visitados)).toList();
        return new NoArvoreUnidade(unidade, filhas);
    }
}
