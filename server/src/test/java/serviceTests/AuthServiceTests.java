package serviceTests;

import static org.junit.jupiter.api.Assertions.*;

import service.AuthService;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthServiceTests extends ServiceTests {
  @Test
  @Order(1)
  @DisplayName("Generate Token")
  public void generateNewToken() throws Exception {
    String newToken = AuthService.generateNewToken();
    assertNotNull(newToken, "Token should not be null");
  }
}
