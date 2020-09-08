package jp.rte.comprehend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jp.rte.comprehend.dto.UserRequest;

@SpringBootApplication
@Controller
public class ComprehendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComprehendApplication.class, args);
	}

	@GetMapping("/")
	public String home(Model model) {
        model.addAttribute("title", "日本語形態素解析ツール");
     // 入力フォームで取り扱うオブジェクトを設定
        model.addAttribute("userRequest", new UserRequest());
        return "index";
	}

	@GetMapping("/error")
	public String error() {
		return "エラーページ";
	}

	@PostMapping(value="/analyze")
	public String analyze(@ModelAttribute UserRequest userRequest, Model model) {
	// userRequestに入力フォームの内容が格納されている
		System.out.println(userRequest);
        model.addAttribute("title", "日本語形態素解析ツール");
        model.addAttribute("sentence", userRequest.getSentence());
		return "result";

	}
}
