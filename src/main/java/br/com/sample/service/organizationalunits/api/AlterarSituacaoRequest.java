package br.com.sample.service.organizationalunits.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AlterarSituacaoRequest(@NotNull Boolean ativa, @NotNull @PositiveOrZero Long versao) {
}
