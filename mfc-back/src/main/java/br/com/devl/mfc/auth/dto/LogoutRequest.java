package br.com.devl.mfc.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class LogoutRequest {

	@NotBlank(message = "O refresh token é obrigatório")
	private String refreshToken;
	
	public String getRefreshToken() {
		return this.refreshToken;
	}
	
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
