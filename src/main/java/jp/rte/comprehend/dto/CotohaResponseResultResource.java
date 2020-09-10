package jp.rte.comprehend.dto;

import java.util.List;

import lombok.Data;

@Data
public class CotohaResponseResultResource {

	private String sentiment;

	private Float score;

	private List<CotohaResponseEmotionalPhraseResource> emotional_phrase;

}
