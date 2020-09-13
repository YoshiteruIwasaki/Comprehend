package jp.rte.comprehend.dto;

import com.azure.ai.textanalytics.models.TextSentiment;

import lombok.Data;

@Data
public class AzureResponseResource {

	private TextSentiment sentiment;

	private double positive;

	private double neutral;

	private double negative;


}
