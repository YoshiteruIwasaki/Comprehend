package jp.rte.comprehend.dto;

import lombok.Data;

@Data
public class CotohaAccessTokenResponseResource {

	private String access_token;

	private String token_type;

	private Long expires_in;

	private String scope;

	private Long issued_at;
}
