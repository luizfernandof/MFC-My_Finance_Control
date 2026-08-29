package br.com.devl.mfc.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ProblemDetail> handleBusinessException(BusinessException ex) {
		return problem(ex.getStatus(), "Regra de negócio", ex.getMessage(), ex.getCode());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
		return problem(HttpStatus.UNAUTHORIZED, "Credenciais inválidas", "E-mail ou senha incorretos.",
				"INVALID_CREDENTIALS");
	}

	@ExceptionHandler(PropertyReferenceException.class)
	public ResponseEntity<ProblemDetail> handlePropertyReference(PropertyReferenceException ex) {
		return problem(HttpStatus.BAD_REQUEST, "Parâmetros inválidos", "O campo de ordenação informado é inválido.",
				"INVALID_SORT");
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
		log.warn("Conflito de integridade de dados", ex);
		return problem(HttpStatus.CONFLICT, "Conflito de dados",
				"A operação conflita com dados já existentes ou vinculados.", "DATA_CONFLICT");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fields = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
		ProblemDetail problem = createProblem(HttpStatus.BAD_REQUEST, "Dados inválidos",
				"Revise os campos informados.", "VALIDATION_ERROR");
		problem.setProperty("fields", fields);
		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler({ HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class,
			MissingServletRequestParameterException.class, HttpMessageNotReadableException.class })
	public ResponseEntity<ProblemDetail> handleInvalidRequest(Exception ex) {
		return problem(HttpStatus.BAD_REQUEST, "Parâmetros inválidos", "A requisição contém parâmetros inválidos.",
				"INVALID_REQUEST");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
		String traceId = UUID.randomUUID().toString();
		log.error("Erro interno. traceId={}", traceId, ex);
		ProblemDetail problem = createProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
				"Não foi possível concluir a operação.", "INTERNAL_ERROR");
		problem.setProperty("traceId", traceId);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
	}

	private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail, String code) {
		return ResponseEntity.status(status).body(createProblem(status, title, detail, code));
	}

	private ProblemDetail createProblem(HttpStatus status, String title, String detail, String code) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setProperty("code", code);
		return problem;
	}
}
