package br.com.sample.service.organizationalunits.domain.service;

import br.com.sample.service.organizationalunits.domain.exception.CicloHierarquicoException;
import br.com.sample.service.organizationalunits.domain.exception.DescendenteAtivoException;
import br.com.sample.service.organizationalunits.domain.exception.ReativacaoSobPaiInativoException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadePaiInativaException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadePaiInexistenteException;
import br.com.sample.service.organizationalunits.domain.model.DadosBasicosUnidade;
import br.com.sample.service.organizationalunits.domain.model.UnidadeOrganizacional;
import br.com.sample.service.organizationalunits.domain.repository.UnidadeOrganizacionalRepository;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.UUID;

public final class PoliticaHierarquiaUnidadeOrganizacional {
    private final UnidadeOrganizacionalRepository repository;

    public PoliticaHierarquiaUnidadeOrganizacional(UnidadeOrganizacionalRepository repository) {
        this.repository = repository;
    }

    public void validarCriacao(UnidadeOrganizacional unidade) {
        validarNovoPai(unidade.id(), unidade.unidadePaiId(), true, false);
    }

    public void alterarDadosBasicos(
            UnidadeOrganizacional unidade, DadosBasicosUnidade novosDados, Instant instante, String ator) {
        validarNovoPai(unidade.id(), novosDados.unidadePaiId(), unidade.ativa(), false);
        unidade.alterarDadosBasicos(novosDados, instante, ator);
    }

    public void reativar(UnidadeOrganizacional unidade, Instant instante, String ator) {
        if (!unidade.ativa()) {
            validarNovoPai(unidade.id(), unidade.unidadePaiId(), true, true);
            unidade.reativar(instante, ator);
        }
    }

    public void desativar(UnidadeOrganizacional unidade, Instant instante, String ator) {
        if (!unidade.ativa()) {
            return;
        }
        validarAusenciaDeDescendenteAtivo(unidade.id());
        unidade.desativar(instante, ator);
    }

    private void validarNovoPai(UUID unidadeId, UUID paiId, boolean filhaAtiva, boolean reativacao) {
        if (paiId == null) {
            return;
        }
        if (paiId.equals(unidadeId)) {
            throw new CicloHierarquicoException();
        }
        var visitados = new HashSet<UUID>();
        visitados.add(unidadeId);
        var ancestralId = paiId;
        var primeiro = true;
        while (ancestralId != null) {
            if (!visitados.add(ancestralId)) {
                throw new CicloHierarquicoException();
            }
            var idConsultado = ancestralId;
            var ancestral = repository.buscarPorId(idConsultado)
                    .orElseThrow(() -> new UnidadePaiInexistenteException(idConsultado));
            if (primeiro && filhaAtiva && !ancestral.ativa()) {
                if (reativacao) {
                    throw new ReativacaoSobPaiInativoException();
                }
                throw new UnidadePaiInativaException();
            }
            primeiro = false;
            ancestralId = ancestral.unidadePaiId();
        }
    }

    private void validarAusenciaDeDescendenteAtivo(UUID unidadeId) {
        var pendentes = new ArrayDeque<UUID>();
        var visitados = new HashSet<UUID>();
        pendentes.add(unidadeId);
        visitados.add(unidadeId);
        while (!pendentes.isEmpty()) {
            var paiId = pendentes.removeFirst();
            for (var filha : repository.buscarFilhasDiretas(paiId)) {
                if (!visitados.add(filha.id())) {
                    throw new CicloHierarquicoException();
                }
                if (filha.ativa()) {
                    throw new DescendenteAtivoException();
                }
                pendentes.addLast(filha.id());
            }
        }
    }
}
