package br.com.devl.mfc.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String code;
	private final HttpStatus status;

	public BusinessException(String message) {
		this("BUSINESS_RULE", message, HttpStatus.BAD_REQUEST);
	}

	public BusinessException(String code, String message, HttpStatus status) {
		super(message);
		this.code = code;
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
