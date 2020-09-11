package jp.rte.comprehend;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;

import jp.rte.comprehend.dto.CotohaAccessTokenRequest;
import jp.rte.comprehend.dto.CotohaAccessTokenResponseResource;
import jp.rte.comprehend.dto.CotohaResponseResource;
import jp.rte.comprehend.dto.UserRequest;

@SpringBootApplication
@Controller
public class ComprehendApplication {

	String ACCESS_TOKEN_URL = "https://api.ce-cotoha.com/v1/oauth/accesstokens";

	String API_URL = "https://api.ce-cotoha.com/api/dev/";

	String SENTIMENT_URL = "nlp/v1/sentiment";

	@Value("${COTOHA.CLIENT.ID}")
	private String CLIENT_ID;

	@Value("${COTOHA.CLIENT.SECRET}")
	private String CLIENT_SECRET;

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

		//access tokenの取得

		CotohaAccessTokenRequest request = new CotohaAccessTokenRequest();
		request.setGrantType("client_credentials");
		request.setClientId(CLIENT_ID);
		request.setClientSecret(CLIENT_SECRET);
		RestTemplate restTemplate = new RestTemplate();

		CotohaAccessTokenResponseResource cotohaAccessTokenResponseResource = restTemplate
				.postForObject(ACCESS_TOKEN_URL, request, CotohaAccessTokenResponseResource.class);

		RequestEntity<UserRequest> requestEntity = RequestEntity
				.post(new URI(API_URL + SENTIMENT_URL))
				.header("Authorization", "Bearer " + cotohaAccessTokenResponseResource.getAccess_token())
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

		// Instantiates a client
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
			System.out.println("Hello, logs!");
			System.out.println(e.getMessage());
		}

		return "result";

	}
}
