package br.com.itau.transaction_service.infra.web.v1.exception;

import br.com.itau.transaction_service.domain.exception.TransactionErrorException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<ApiErrorResponse.ValidationError> validationErrors = bindingResult.getFieldErrors()
                .stream()
                .map(fieldError -> new ApiErrorResponse.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                "Erro de validação nos campos da requisição",
                request.getRequestURI(),
                validationErrors
        );
    }

    @ExceptionHandler(TransactionErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionError(
            TransactionErrorException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Business Error",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    //@ExceptionHandler(br.com.itau.transaction_service.domain.exception.ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            RuntimeException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleJsonErrors(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON",
                "Corpo da requisição inválido ou mal formatado",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred at {}", request.getRequestURI(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Ocorreu um erro inesperado no servidor. Contate o suporte.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(
            org.springframework.web.bind.MissingRequestHeaderException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Missing Header",
                "O header obrigatório '" + ex.getHeaderName() + "' não foi informado.",
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            List<ApiErrorResponse.ValidationError> validations) {

        ApiErrorResponse response = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path,
                validations
        );
        return ResponseEntity.status(status).body(response);
    }
}