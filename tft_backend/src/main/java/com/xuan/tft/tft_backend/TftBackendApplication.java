package com.xuan.tft.tft_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TftBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TftBackendApplication.class, args);
	}

//	@RestController
//	@RequestMapping("/api")
//	static class HealthController {
//		@GetMapping("/health")
//		public String health() {
//			return "OK";
//		}
//	}
}
