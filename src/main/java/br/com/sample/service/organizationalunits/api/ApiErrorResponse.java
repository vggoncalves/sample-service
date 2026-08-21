package br.com.sample.service.organizationalunits.api;

import java.util.List;

public record ApiErrorResponse(List<ApiError> error, String correlationId) {
}
