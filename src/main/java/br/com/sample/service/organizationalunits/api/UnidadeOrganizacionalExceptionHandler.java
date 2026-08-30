package br.com.sample.service.organizationalunits.api;

import br.com.sample.service.organizationalunits.application.CodigoUnidadeDuplicadoException;
import br.com.sample.service.organizationalunits.application.ConsultaUnidadeInvalidaException;
import br.com.sample.service.organizationalunits.application.ConflitoVersaoUnidadeException;
import br.com.sample.service.organizationalunits.application.ArvoreUnidadeInvalidaException;
import br.com.sample.service.organizationalunits.application.LimiteArvoreExcedidoException;
import br.com.sample.service.organizationalunits.application.UnidadeOrganizacionalNaoEncontradaException;
import br.com.sample.service.organizationalunits.domain.exception.DadoUnidadeInvalidoException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadeOrganizacionalException;
import br.com.sample.service.organizationalunits.domain.exception.UnidadePaiInexistenteException;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UnidadeOrganizacionalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> entradaInvalida(MethodArgumentNotValidException exception) {
        var erros = exception.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ApiError(erro.getDefaultMessage(), "UNIDADE-0002", erro.getField())).toList();
        return resposta(HttpStatus.BAD_REQUEST, erros);
    }

    @ExceptionHandler(CodigoUnidadeDuplicadoException.class)
    ResponseEntity<ApiErrorResponse> codigoDuplicado(CodigoUnidadeDuplicadoException exception) {
        return resposta(HttpStatus.CONFLICT, List.of(new ApiError(exception.getMessage(), "UNIDADE-0001", "codigo")));
    }

    @ExceptionHandler(ConsultaUnidadeInvalidaException.class)
    ResponseEntity<ApiErrorResponse> consultaInvalida(ConsultaUnidadeInvalidaException exception) {
        return resposta(HttpStatus.BAD_REQUEST, List.of(new ApiError(exception.getMessage(), "UNIDADE-0008", null)));
    }

    @ExceptionHandler(ConflitoVersaoUnidadeException.class)
    ResponseEntity<ApiErrorResponse> conflitoVersao(ConflitoVersaoUnidadeException exception) {
        return resposta(HttpStatus.CONFLICT, List.of(new ApiError(exception.getMessage(), "UNIDADE-0009", "versao")));
    }

    @ExceptionHandler(ArvoreUnidadeInvalidaException.class)
    ResponseEntity<ApiErrorResponse> arvoreInvalida(ArvoreUnidadeInvalidaException exception) {
        return resposta(HttpStatus.BAD_REQUEST, List.of(new ApiError(exception.getMessage(), "UNIDADE-0014", "profundidade")));
    }

    @ExceptionHandler(LimiteArvoreExcedidoException.class)
    ResponseEntity<ApiErrorResponse> limiteArvore(LimiteArvoreExcedidoException exception) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, List.of(new ApiError(exception.getMessage(), "UNIDADE-0015", null)));
    }

    @ExceptionHandler(UnidadeOrganizacionalNaoEncontradaException.class)
    ResponseEntity<ApiErrorResponse> naoEncontrada(UnidadeOrganizacionalNaoEncontradaException exception) {
        return resposta(HttpStatus.NOT_FOUND, List.of(new ApiError("Unidade organizacional não encontrada", "UNIDADE-0003", "id")));
    }

    @ExceptionHandler(UnidadeOrganizacionalException.class)
    ResponseEntity<ApiErrorResponse> regraDeDominio(UnidadeOrganizacionalException exception) {
        return resposta(statusPara(exception),
                List.of(new ApiError(exception.getMessage(), exception.getCodigoErro(), exception.getCampo())));
    }

    private static HttpStatus statusPara(UnidadeOrganizacionalException exception) {
        if (exception instanceof DadoUnidadeInvalidoException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exception instanceof UnidadePaiInexistenteException) {
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.CONFLICT;
    }

    private static ResponseEntity<ApiErrorResponse> resposta(HttpStatus status, List<ApiError> erros) {
        var correlationId = MDC.get("correlationId");
        return ResponseEntity.status(status).body(new ApiErrorResponse(erros,
                correlationId == null ? UUID.randomUUID().toString() : correlationId));
    }
}
