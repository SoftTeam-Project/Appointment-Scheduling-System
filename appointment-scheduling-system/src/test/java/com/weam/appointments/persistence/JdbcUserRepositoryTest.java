package com.weam.appointments.persistence;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.weam.appointments.persistence.JdbcUserRepository;
import com.weam.appointments.persistence.SchemaInitializer;
import com.weam.appointments.presentation.CliApp;
import com.weam.appointments.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Disabled;
class JdbcUserRepositoryTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Disabled("template test")
	@Test
	void test() {
		fail("Not yet implemented");
	}
	@Test
	void cliExitShouldReturnWithoutAskingPassword() {
	    new SchemaInitializer().init();
	    AuthService auth = new AuthService(new JdbcUserRepository());

	    String input = "exit\n";
	    ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
	    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
	    PrintStream out = new PrintStream(outBytes);

	    CliApp app = new CliApp(auth, in, out);
	    app.runOnce();

	    String output = outBytes.toString();
	    assertTrue(output.contains("Username"));
	    assertFalse(output.contains("Password"));
	}

}
