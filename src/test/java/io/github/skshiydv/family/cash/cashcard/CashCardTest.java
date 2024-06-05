package io.github.skshiydv.family.cash.cashcard;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardTest {
    @Autowired
    private JacksonTester<CashCard> json;
    @Test
    public void serialize() throws IOException {
        CashCard newCashCard = new CashCard(99L, 123.45);
        Assertions.assertThat(json.write(newCashCard)).isStrictlyEqualToJson("expected.json");
        Assertions.assertThat(json.write(newCashCard)).hasJsonPathNumberValue("@.id");
        Assertions.assertThat(json.write(newCashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        Assertions.assertThat(json.write(newCashCard)).hasJsonPathNumberValue("@.amount");
        Assertions.assertThat(json.write(newCashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    @Test
    public void deserialize() throws IOException {
        String expectedJson = """
                {
                "id":99,
                "amount":123.45
                }
                """;
        Assertions.assertThat(json.parse(expectedJson)).isEqualTo(new CashCard(99L, 123.45));
        assertThat(json.parseObject(expectedJson).id()).isEqualTo(99);
        assertThat(json.parseObject(expectedJson).amount()).isEqualTo(123.45);
    }

}
