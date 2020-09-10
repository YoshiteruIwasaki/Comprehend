package jp.rte.comprehend.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CotohaAccessTokenRequest implements Serializable {

	@NotBlank
	private String grantType;

	@NotBlank
	private String clientId;

	@NotBlank
	private String clientSecret;
}