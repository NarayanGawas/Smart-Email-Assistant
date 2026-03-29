package com.email.writer.app; 
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service 
public class EmailGeneraterService {
	private final WebClient webClient;
	@Value("${gemini.api.url}") 
	private String geminiApiUrl; 
	
	@Value("${gemini.api.key}")
	private String geminiApiKey;
	
	public EmailGeneraterService(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build(); } 
	
	public Mono<String> generateEmailReply(EmailRequest emailRequest) {

	    String prompt = buildPrompt(emailRequest);

	    Map<String, Object> requestBody = Map.of(
	    	    "contents", List.of(
	    	        Map.of(
	    	            "parts", List.of(
	    	                Map.of("text", prompt)
	    	            )
	    	        )
	    	    )
	    	);

	    return webClient.post()
	            .uri(geminiApiUrl + "?key=" + geminiApiKey)
	            .header("Content-Type", "application/json")
	            .bodyValue(requestBody)
	            .retrieve()
	            .onStatus(status -> status.isError(), response ->
	                    response.bodyToMono(String.class)
	                            .map(error -> new RuntimeException("Gemini API Error: " + error))
	            )
	            .bodyToMono(String.class)
	            .map(this::extractResponseContent); // ✅ no blocking
	}
	private String extractResponseContent(String response) {
	    try {
	        ObjectMapper mapper = new ObjectMapper();
	        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(response);

	        com.fasterxml.jackson.databind.JsonNode candidates = rootNode.path("candidates");

	        if (candidates.isEmpty()) {
	            return "No response from Gemini: " + response;
	        }

	        return candidates.get(0)
	                .path("content")
	                .path("parts")
	                .get(0)
	                .path("text")
	                .asText();

	    } catch (Exception e) {
	        return "Error parsing response: " + e.getMessage() + "\nFull Response: " + response;
	    }
	}
	private String buildPrompt(EmailRequest emailRequest) { 
		StringBuilder prompt = new StringBuilder();
		prompt.append("Generate a professional email reply for the following email content. Please don't generate a subject line ");
		if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) 
		{ prompt.append("Use a ")
			.append(emailRequest.getTone())
			.append(" tone. ");
		} prompt.append("\nOriginal email:\n")
		.append(emailRequest.getEmailContent()); 
		return prompt.toString(); 
		} }