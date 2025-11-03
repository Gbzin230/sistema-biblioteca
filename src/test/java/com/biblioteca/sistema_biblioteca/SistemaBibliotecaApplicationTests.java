package com.biblioteca.sistema_biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SistemaBibliotecaApplicationTests {

	@Test
	void appStartsCorrectly() {
		System.out.println("✅ Aplicação iniciou com sucesso para testes!");
	}

}
