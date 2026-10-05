package com.example.proyecto.global.exception;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.proyecto.global.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static final String GENERIC_MESSAGE = "Ocurrio un error inesperado. Intenta nuevamente.";

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex,
			HttpServletRequest request) {
		return build(ex.getStatus(), ex.getMessage(), request, Map.of(), List.of());
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessRule(BusinessRuleException ex,
			HttpServletRequest request) {
		log.warn("Regla de negocio violada en {}: {}", request.getRequestURI(), ex.getMessage());
		return build(ex.getStatus(), ex.getMessage(), request, Map.of(), List.of());
	}

	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<ApiErrorResponse> handleCredenciales(CredencialesInvalidasException ex,
			HttpServletRequest request) {
		log.warn("Fallo de autenticacion en {}", request.getRequestURI());
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, Map.of(), List.of());
	}

	@ExceptionHandler(EmailDuplicadoException.class)
	public ResponseEntity<ApiErrorResponse> handleEmailDuplicado(EmailDuplicadoException ex,
			HttpServletRequest request) {
		log.warn("Intento de registro con correo duplicado en {}", request.getRequestURI());
		return build(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(fieldError.getField(), defaultMessage(fieldError));
		}
		return build(HttpStatus.BAD_REQUEST, "Datos invalidos en la solicitud", request, fieldErrors, List.of());
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidParams(HandlerMethodValidationException ex,
			HttpServletRequest request) {
		List<String> details = ex.getParameterValidationResults().stream()
				.flatMap(result -> result.getResolvableErrors().stream())
				.map(MessageSourceResolvable::getDefaultMessage)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		String message = details.isEmpty() ? "Parametros invalidos en la solicitud" : String.join("; ", details);
		return build(HttpStatus.BAD_REQUEST, message, request, Map.of(), details);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {
		List<String> details = ex.getConstraintViolations().stream()
				.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Parametros invalidos en la solicitud", request, Map.of(), details);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class })
	public ResponseEntity<ApiErrorResponse> handleMalformedRequest(Exception ex, HttpServletRequest request) {
		log.debug("Solicitud malformada en {}: {}", request.getRequestURI(), ex.getMessage());
		return build(HttpStatus.BAD_REQUEST, "La solicitud enviada no es valida", request, Map.of(), List.of());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNoResource(NoResourceFoundException ex,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "El recurso solicitado no existe", request, Map.of(), List.of());
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {
		return build(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP no soportado: " + ex.getMethod(), request,
				Map.of(), List.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		log.warn("Violacion de integridad de datos en {}: {}", request.getRequestURI(),
				ex.getMostSpecificCause().getMessage());
		return build(HttpStatus.CONFLICT, "La operacion entra en conflicto con los datos existentes", request,
				Map.of(), List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Error no controlado en {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_MESSAGE, request, Map.of(), List.of());
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request,
			Map<String, String> fieldErrors, List<String> details) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors,
				details);
		return ResponseEntity.status(status).body(body);
	}

	private String defaultMessage(ObjectError error) {
		return error.getDefaultMessage() == null ? "valor invalido" : error.getDefaultMessage();
	}
}