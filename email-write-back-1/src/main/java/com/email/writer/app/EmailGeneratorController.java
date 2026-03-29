package com.email.writer.app;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
@CrossOrigin(origins="*")
public class EmailGeneratorController {
   
	private final EmailGeneraterService emailGeneratorService;
	
	@PostMapping("/generate")
	public Mono<String> generateEmail(@RequestBody EmailRequest request) {
	    return emailGeneratorService.generateEmailReply(request);
	}
}
