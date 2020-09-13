package jp.rte.comprehend;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

import jp.rte.comprehend.dto.AzureResponseResource;
import jp.rte.comprehend.dto.CotohaAccessTokenRequest;
import jp.rte.comprehend.dto.CotohaAccessTokenResponseResource;
import jp.rte.comprehend.dto.CotohaResponseResource;
import jp.rte.comprehend.dto.UserRequest;

@SpringBootApplication
@Controller
public class ComprehendApplication {

	@Value("${COTOHA.ACCESS.TOKEN.URL}")
	private String COTOHA_ACCESS_TOKEN_URL;

	@Value("${COTOHA.API.ENDPOINT}")
	private String COTOHA_API_ENDPOINT;

	@Value("${COTOHA.CLIENT.ID}")
	private String COTOHA_CLIENT_ID;

	@Value("${COTOHA.CLIENT.SECRET}")
	private String COTOHA_CLIENT_SECRET;

	@Value("${AZURE.API.KEY}")
	private String AZURE_API_KEY;

	@Value("${AZURE.API.ENDPOINT}")
	private String AZURE_API_ENDPOINT;

	public static void main(String[] args) {
		SpringApplication.run(ComprehendApplication.class, args);
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "日本語感情分析ツール");
		// 入力フォームで取り扱うオブジェクトを設定
		model.addAttribute("userRequest", new UserRequest());
		return "index";
	}

	@GetMapping("/error")
	public String error(Model model) {
		model.addAttribute("title", "日本語感情分析ツール");
		return "error";
	}

	@PostMapping(value = "/analyze")
	public String analyze(@ModelAttribute UserRequest userRequest, Model model) throws URISyntaxException {
		// userRequestに入力フォームの内容が格納されている
		model.addAttribute("title", "日本語感情分析ツール");


		//COTOHA API
		try {
			//access tokenの取得
			CotohaAccessTokenRequest request = new CotohaAccessTokenRequest();
			request.setGrantType("client_credentials");
			request.setClientId(COTOHA_CLIENT_ID);
			request.setClientSecret(COTOHA_CLIENT_SECRET);
			RestTemplate restTemplate = new RestTemplate();

			// create headers
			HttpHeaders headers = new HttpHeaders();
			// set `content-type` header
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<CotohaAccessTokenRequest> entity = new HttpEntity<>(request, headers);
			CotohaAccessTokenResponseResource cotohaAccessTokenResponseResource = restTemplate
					.postForObject(COTOHA_ACCESS_TOKEN_URL, entity, CotohaAccessTokenResponseResource.class);

			RequestEntity<UserRequest> requestEntity = RequestEntity
					.post(new URI(COTOHA_API_ENDPOINT))
					.header("Authorization", "Bearer " + cotohaAccessTokenResponseResource.getAccess_token())
					.contentType(MediaType.APPLICATION_JSON)
					.body(userRequest);

			ResponseEntity<CotohaResponseResource> responseEntity = restTemplate.exchange(requestEntity,
					CotohaResponseResource.class);

			model.addAttribute("sentence", userRequest.getSentence());
			model.addAttribute("message", responseEntity.getBody().getMessage());
			model.addAttribute("status", responseEntity.getBody().getStatus());
			model.addAttribute("sentiment", responseEntity.getBody().getResult().getSentiment());
			model.addAttribute("isPositive",
					"Positive".equals(responseEntity.getBody().getResult().getSentiment()) ? true : false);
			model.addAttribute("isNegative",
					"Negative".equals(responseEntity.getBody().getResult().getSentiment()) ? true : false);
			model.addAttribute("isNeutral",
					"Neutral".equals(responseEntity.getBody().getResult().getSentiment()) ? true : false);

			model.addAttribute("score", responseEntity.getBody().getResult().getScore());

			model.addAttribute("rating", Math.round(responseEntity.getBody().getResult().getScore() * 10));
		} catch (Exception e) {
			System.out.println(e);
		}

		//GOOGLE Natural Language API
		try (LanguageServiceClient language = LanguageServiceClient.create()) {

			Document doc = Document.newBuilder().setContent(userRequest.getSentence()).setType(Type.PLAIN_TEXT).build();

			// Detects the sentiment of the text
			Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();

			model.addAttribute("isGNLPositive",
					sentiment.getScore() > 0 ? true : false);
			model.addAttribute("isGNLNegative",
					sentiment.getScore() < 0 ? true : false);
			model.addAttribute("isGNLNeutral",
					sentiment.getScore() == 0 ? true : false);
			model.addAttribute("sentimentGNL",
					sentiment.getScore() > 0 ? "Positive" : sentiment.getScore() < 0 ? "Negative" : "Neutral");

			model.addAttribute("scoreGNL", sentiment.getScore());
			model.addAttribute("magnitudeGNL", sentiment.getMagnitude());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		// AZURE Text Analytics API
		try {
			//You will create these methods later in the quickstart.
			TextAnalyticsClient client = AzureTextAnalytics.authenticateClient(AZURE_API_KEY, AZURE_API_ENDPOINT);
			AzureResponseResource azureResponseResource = AzureTextAnalytics.sentimentAnalysis(client,
					userRequest.getSentence());

			model.addAttribute("isAzurePositive",
					azureResponseResource.getPositive() > azureResponseResource.getNeutral()
							&& azureResponseResource.getPositive() > azureResponseResource.getNegative() ? true
									: false);
			model.addAttribute("isAzureNegative",
					azureResponseResource.getNegative() > azureResponseResource.getPositive()
							&& azureResponseResource.getNegative() > azureResponseResource.getNeutral() ? true : false);
			model.addAttribute("isAzureNeutral",
					azureResponseResource.getNeutral() > azureResponseResource.getPositive()
							&& azureResponseResource.getNeutral() > azureResponseResource.getNegative() ? true : false);
			model.addAttribute("sentimentAzure", azureResponseResource.getSentiment().toString());


			model.addAttribute("scoreAzurePositive", azureResponseResource.getPositive());
			model.addAttribute("scoreAzureNeutral", azureResponseResource.getNeutral());
			model.addAttribute("scoreAzureNegative", azureResponseResource.getNegative());

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "result";

	}
}
