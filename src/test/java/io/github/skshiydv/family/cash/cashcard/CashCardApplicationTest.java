package io.github.skshiydv.family.cash.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext documentContext = JsonPath.parse(response.getBody());
        Number id = documentContext.read("$.id");
        assertThat(id).isEqualTo(99);

        Double amount = documentContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldReturnAPageOfCashCard() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards?page=0&size=1", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray page = documentContext.read("$[*]");
        assertThat(page.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnAllCashCardsWhenListIsRequested() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        int cashCardsCount = documentContext.read("$.length()");
        assertThat(cashCardsCount).isEqualTo(3);
        JSONArray ids = documentContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);
        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00);
    }

    @Test
    void shouldNotReturnACashCardWhenUsingBadCredentials() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("BAD-USER", "abc123")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        response = restTemplate
                .withBasicAuth("sarah1", "BAD-PASSWORD")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnSortedPageOfCashCards() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");

        assertThat(read.size()).isEqualTo(1);
        double amountOfFirstCashCard = documentContext.read("$[0].amount");
        assertThat(amountOfFirstCashCard).isEqualTo(150.00);
    }

    @Test
    void shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(response.getBody());
        JSONArray read = documentContext.read("$[*]");
        assertThat(read.size()).isEqualTo(3);
        JSONArray amounts = documentContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.00, 150.00);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards/1000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateACashCard() {
        CashCard newCashCard = new CashCard(null, 250.50, "sakshi1");
        ResponseEntity<Void> createResponse = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .postForEntity("/cashcards", newCashCard, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity(locationOfNewCashCard, String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
        Number id = documentContext.read("$.id");
        Double amount = documentContext.read("$.amount");
        assertThat(id).isNotNull();
        assertThat(amount).isEqualTo(250.50);

    }

    @Test
    void shouldRejectUsersWhoAreNotCardUsers() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("hank-owns-no-cards", "qrs456")
                .getForEntity("/cashcards/99", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        ResponseEntity<String> response = restTemplate.withBasicAuth("sakshi1", "abc123").getForEntity(
                "/cashcards/102", String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @DirtiesContext
    @Test
    void shouldUpdateAnExistingCashCard() {
        CashCard newCashCard = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> req = new HttpEntity<>(newCashCard);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .exchange("/cashcards/99", HttpMethod.PUT, req, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> response1 = restTemplate.withBasicAuth("sakshi1", "abc123").getForEntity("/cashcards/99", String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext documentContext1 = JsonPath.parse(response1.getBody());
        Number id = documentContext1.read("$.id");
        assertThat(id).isEqualTo(99);
        double amount = documentContext1.read("$.amount");
        assertThat(amount).isEqualTo(19.99);
    }

    @Test
    void shouldNotUpdateACashCardThatDoesNotExist() {
        CashCard unknownCard = new CashCard(null, 19.99, null);
        HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
        CashCard kumarsCashCard = new CashCard(null, 333.33, null);
        HttpEntity<CashCard> req = new HttpEntity<>(kumarsCashCard);
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .exchange("/cashcards/102", HttpMethod.PUT, req, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);


    }


    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCashCard() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<String> response1 = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .getForEntity("/cashcards/102", String.class);
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void shouldNotDeleteACashCardThatDoesNotExist() {
        ResponseEntity<Void> response = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    }


    @Test
    void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
        ResponseEntity<Void> deleteResponse = restTemplate
                .withBasicAuth("sakshi1", "abc123")
                .exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ResponseEntity<String> getResponse = restTemplate
                .withBasicAuth("aryan1", "qrs456")
                .getForEntity("/cashcards/102", String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


}