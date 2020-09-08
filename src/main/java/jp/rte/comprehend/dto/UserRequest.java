package jp.rte.comprehend.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UserRequest implements Serializable {

@NotBlank
private String sentence;
}