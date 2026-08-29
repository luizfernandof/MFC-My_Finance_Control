package br.com.devl.mfc.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

	@NotBlank(message = "O e-mail é obrigatório")
	@Email(message = "Informe um e-mail válido")
	@Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
	private String email;

	@NotBlank(message = "A senha é obrigatória")
	@Size(max = 72, message = "A senha deve ter no máximo 72 caracteres")
	private String password;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
