package io.github.skshiydv.family.cash.cashcard;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
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
    @Autowired
    private JacksonTester<CashCard[]> jsonList;
    private CashCard[] cashCards;

    @BeforeEach
    void setUp() {
        cashCards = Arrays.array(
                new CashCard(99L, 123.45, "sakshi1"),
                new CashCard(100L, 1.00, "sakshi1"),
                new CashCard(101L, 150.00, "sakshi1"));
    }

    @Test
    public void ListSerializationTest() throws IOException {
        Assertions.assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");

    }

    @Test
    public void ListDeserializationTest() throws IOException {
        String expected = """
                [
                  {"id": 99, "amount": 123.45 ,"owner":"sakshi1"},
                  {"id": 100, "amount": 1.00 ,"owner":"sakshi1"},
                  {"id": 101, "amount": 150.00 ,"owner":"sakshi1"}
                ]
                """;
        Assertions.assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
    @Test
    public void serialize() throws IOException {
        CashCard newCashCard = new CashCard(99L, 123.45, "sakshi1");
        Assertions.assertThat(json.write(newCashCard)).isStrictlyEqualToJson("single.json");
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
                "amount":123.45,
                "owner":"sakshi1"
                }
                """;
        Assertions.assertThat(json.parse(expectedJson)).isEqualTo(new CashCard(99L, 123.45, "sakshi1"));
        assertThat(json.parseObject(expectedJson).id()).isEqualTo(99);
        assertThat(json.parseObject(expectedJson).amount()).isEqualTo(123.45);
    }

}
