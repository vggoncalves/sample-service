package br.com.sample.service.organizationalunits.api;

import br.com.sample.service.organizationalunits.application.ConsultarUnidadeOrganizacionalUseCase;
import br.com.sample.service.organizationalunits.application.CriarUnidadeOrganizacionalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/unidadesOrganizacionais/v1.0.0/unidadesOrganizacionais")
@Tag(name = "Unidades Organizacionais")
public class UnidadeOrganizacionalController {
    private final CriarUnidadeOrganizacionalUseCase criar;
    private final ConsultarUnidadeOrganizacionalUseCase consultar;
    private final UnidadeOrganizacionalMapper mapper;

    public UnidadeOrganizacionalController(CriarUnidadeOrganizacionalUseCase criar,
            ConsultarUnidadeOrganizacionalUseCase consultar, UnidadeOrganizacionalMapper mapper) {
        this.criar = criar;
        this.consultar = consultar;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Cria uma unidade organizacional ativa")
    public ResponseEntity<UnidadeResponse> criar(@Valid @RequestBody CriarUnidadeRequest request) {
        var unidade = criar.executar(request.codigo(), mapper.paraDadosBasicos(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(unidade.id()).toUri();
        return ResponseEntity.created(location).body(mapper.paraResponse(unidade));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma unidade organizacional por identificador")
    public UnidadeResponse consultar(@PathVariable UUID id) {
        return mapper.paraResponse(consultar.executar(id));
    }
}
