package jp.rte.comprehend.dto;

import lombok.Data;

@Data
public class CotohaResponseResource {

	private CotohaResponseResultResource result;

	private Integer status;

	private String message;
}
