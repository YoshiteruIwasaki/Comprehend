package jp.rte.comprehend;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;

public class AmazonComprehendClient {
	static DetectSentimentResult comprehend(String text) {
		AWSCredentialsProvider awsCreds = DefaultAWSCredentialsProviderChain.getInstance();

		AmazonComprehend comprehendClient = AmazonComprehendClientBuilder.standard()
				.withCredentials(awsCreds)
				.withRegion("ap-northeast-1")
				.build();

		// Call detectSentiment API
		System.out.println("Calling DetectSentiment");
		DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(text)
				.withLanguageCode("ja");
		return comprehendClient.detectSentiment(detectSentimentRequest);
	}
}
