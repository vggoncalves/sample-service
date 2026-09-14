package br.com.sample.service.organizationalunits.api;

import br.com.sample.service.organizationalunits.application.ConsultarUnidadeOrganizacionalUseCase;
import br.com.sample.service.organizationalunits.application.ConsultaUnidadesOrganizacionais;
import br.com.sample.service.organizationalunits.application.CriarUnidadeOrganizacionalUseCase;
import br.com.sample.service.organizationalunits.application.ListarUnidadesOrganizacionaisUseCase;
import br.com.sample.service.organizationalunits.application.GerenciarUnidadeOrganizacionalUseCase;
import br.com.sample.service.organizationalunits.application.ConsultarArvoreUnidadesUseCase;
import java.util.List;
import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import br.com.sample.service.security.Permissao;

@RestController
@RequestMapping("/api/unidadesOrganizacionais/v1.0.0/unidadesOrganizacionais")
@Tag(name = "Unidades Organizacionais")
public class UnidadeOrganizacionalController {
    private final CriarUnidadeOrganizacionalUseCase criar;
    private final ConsultarUnidadeOrganizacionalUseCase consultar;
    private final ListarUnidadesOrganizacionaisUseCase listar;
    private final GerenciarUnidadeOrganizacionalUseCase gerenciar;
    private final ConsultarArvoreUnidadesUseCase arvore;
    private final UnidadeOrganizacionalMapper mapper;

    public UnidadeOrganizacionalController(CriarUnidadeOrganizacionalUseCase criar,
            ConsultarUnidadeOrganizacionalUseCase consultar, ListarUnidadesOrganizacionaisUseCase listar,
            GerenciarUnidadeOrganizacionalUseCase gerenciar, ConsultarArvoreUnidadesUseCase arvore,
            UnidadeOrganizacionalMapper mapper) {
        this.criar = criar;
        this.consultar = consultar;
        this.listar = listar;
        this.gerenciar = gerenciar;
        this.arvore = arvore;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_CONSULTAR.name())")
    @Operation(summary = "Lista unidades organizacionais com filtros, paginação e ordenação")
    public PaginaUnidadesResponse listar(@RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome, @RequestParam(required = false) String sigla,
            @RequestParam(required = false) TipoUnidade tipo, @RequestParam(required = false) Boolean ativa,
            @RequestParam(required = false) UUID unidadePaiId, @RequestParam(required = false) Boolean raiz,
            @RequestParam(defaultValue = "0") int pn, @RequestParam(defaultValue = "20") int ps,
            @RequestParam(defaultValue = "nome,codigo") String sort) {
        return mapper.paraResponse(listar.executar(new ConsultaUnidadesOrganizacionais(codigo, nome, sigla, tipo,
                ativa, unidadePaiId, raiz, pn, ps, sort)));
    }

    @GetMapping("/capacidades")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_CONSULTAR.name())")
    @Operation(summary = "Consulta capacidades efetivas do usuário para unidades organizacionais")
    public CapacidadesUnidadeResponse capacidades(Authentication authentication) {
        var autoridades = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());
        return new CapacidadesUnidadeResponse(autoridades.contains(Permissao.UNIDADE_CRIAR.name()),
                autoridades.contains(Permissao.UNIDADE_ALTERAR.name()), autoridades.contains(Permissao.UNIDADE_DESATIVAR.name()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_CRIAR.name())")
    @Operation(summary = "Cria uma unidade organizacional ativa")
    public ResponseEntity<UnidadeResponse> criar(@Valid @RequestBody CriarUnidadeRequest request) {
        var unidade = criar.executar(request.codigo(), mapper.paraDadosBasicos(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(unidade.id()).toUri();
        return ResponseEntity.created(location).body(mapper.paraResponse(unidade));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_CONSULTAR.name())")
    @Operation(summary = "Consulta uma unidade organizacional por identificador")
    public UnidadeResponse consultar(@PathVariable UUID id) {
        return mapper.paraResponse(consultar.executar(id));
    }

    @GetMapping("/{id}/filhas")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_CONSULTAR.name())")
    @Operation(summary = "Lista filhas diretas de uma unidade")
    public PaginaUnidadesResponse listarFilhas(@PathVariable UUID id, @RequestParam(required = false) Boolean ativa,
            @RequestParam(defaultValue = "0") int pn, @RequestParam(defaultValue = "20") int ps,
            @RequestParam(defaultValue = "nome,codigo") String sort) {
        consultar.executar(id);
        return mapper.paraResponse(listar.executar(new ConsultaUnidadesOrganizacionais(null, null, null, null,
                ativa, id, null, pn, ps, sort)));
    }

    @GetMapping("/arvore")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_CONSULTAR.name())")
    @Operation(summary = "Consulta árvore organizacional com profundidade limitada")
    public List<NoArvoreResponse> consultarArvore(@RequestParam(required = false) UUID raizId,
            @RequestParam(required = false) Boolean ativa, @RequestParam(defaultValue = "5") int profundidade) {
        return arvore.executar(raizId, ativa, profundidade).stream().map(mapper::paraResponse).toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_ALTERAR.name())")
    @Operation(summary = "Altera integralmente os dados editáveis de uma unidade")
    public UnidadeResponse alterar(@PathVariable UUID id, @Valid @RequestBody AlterarUnidadeRequest request) {
        return mapper.paraResponse(gerenciar.alterar(id, request.versao(), mapper.paraDadosBasicos(request)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_DESATIVAR.name())")
    @Operation(summary = "Altera a situação de uma unidade")
    public UnidadeResponse alterarSituacao(@PathVariable UUID id, @Valid @RequestBody AlterarSituacaoRequest request) {
        return mapper.paraResponse(gerenciar.alterarSituacao(id, request.versao(), request.ativa()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(T(br.com.sample.service.security.Permissao).UNIDADE_DESATIVAR.name())")
    @Operation(summary = "Desativa logicamente uma unidade")
    public ResponseEntity<Void> desativar(@PathVariable UUID id, @RequestHeader("If-Match") long versao) {
        gerenciar.alterarSituacao(id, versao, false);
        return ResponseEntity.noContent().build();
    }
}
