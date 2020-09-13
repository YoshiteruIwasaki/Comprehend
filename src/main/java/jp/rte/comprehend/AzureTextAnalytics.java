package jp.rte.comprehend;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.ai.textanalytics.models.DocumentSentiment;
import com.azure.core.credential.AzureKeyCredential;

import jp.rte.comprehend.dto.AzureResponseResource;

public class AzureTextAnalytics {

	static TextAnalyticsClient authenticateClient(String key, String endpoint) {
		return new TextAnalyticsClientBuilder()
				.credential(new AzureKeyCredential(key))
				.endpoint(endpoint)
				.buildClient();
	}

	static AzureResponseResource sentimentAnalysis(TextAnalyticsClient client, String text) {

		DocumentSentiment documentSentiment = client.analyzeSentiment(text);
		AzureResponseResource resource = new AzureResponseResource();
		resource.setSentiment(documentSentiment.getSentiment());
		resource.setPositive(documentSentiment.getConfidenceScores().getPositive());
		resource.setNeutral(documentSentiment.getConfidenceScores().getNeutral());
		resource.setNegative(documentSentiment.getConfidenceScores().getNegative());
		return resource;
	}

}
