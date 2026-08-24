package br.com.sample.service.organizationalunits.api;

import br.com.sample.service.organizationalunits.domain.model.TipoUnidade;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AlterarUnidadeRequest(@NotBlank @Size(max = 150) String nome, @Size(max = 30) String sigla,
        @Size(max = 500) String descricao, @NotNull TipoUnidade tipo, UUID unidadePaiId,
        @Email @Size(max = 254) String emailContato, @Size(max = 32) String telefone,
        @NotNull @PositiveOrZero Long versao) {
}
