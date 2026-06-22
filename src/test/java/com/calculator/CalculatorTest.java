package test.java.com.calculator;
import junit.framework.TestCase;
import main.java.core.calculator.Calculator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@Nested
public class CalculatorTest extends TestCase {

    // ----- Tests unitaires Calculator ----- //
    @ParameterizedTest(name = "devrait retourner {0} + {1} = {2}")
    @CsvSource({
            "2,    3,    5",
            "-5,  -3,   -8",
            "-5,   3,   -2",
            "7,    0,    7",
            "0.1,  0.2,  0.3"
    })
    void shouldAdd(double a, double b, double expected) {
        assertEquals(expected, Calculator.add(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} - {1} = {2}")
    @CsvSource({
            "10,   4,    6",
            "3,    10,   -7",
            "5,    0,    5",
            "-5,   -3,   -2",
            "0.3,  0.1,  0.2"
    })
    void shouldSubtract(double a, double b, double expected) {
        assertEquals(expected, Calculator.subtract(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} * {1} = {2}")
    @CsvSource({
            "6,    7,    42",
            "0,    999,  0",
            "-3,   -4,   12",
            "3,    -4,   -12",
            "0.1,  0.2,  0.02"
    })
    void shouldMultiply(double a, double b, double expected) {
        assertEquals(expected, Calculator.multiply(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} / {1} = {2}")
    @CsvSource({
            "20,    5,    4",
            "0,     5,    0",
            "-10,   -2,   5",
            "-7,    2,    -3.5",
            "10,    3,    3.333333333"
    })
    void shouldDivide(double a, double b, double expected) {
        assertEquals(expected, Calculator.divide(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait lancer une exception lors de la division par zéro")
    @CsvSource({
            "10, 0",
            "-5, 0",
            "0, 0"
    })
    void testShouldThrowWhenDivideByZero(double a, double b) {
        try {
            Calculator.divide(a, b);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Cannot divide by zero", e.getMessage());
        }
    }

    // ----- Tests coercion Calculator ----- //
    @ParameterizedTest(name = "devrait retourner {0} + {1} = {2} (coercion)")
    @CsvSource({
            "'2',    '3',    5",
            "'-5',  '-3',   -8",
            "'-5',   '3',   -2",
            "'7',    '0',    7",
            "'0.1',  '0.2',  0.3"
    })
    void shouldAddWithCoercion(String a, String b, double expected) {
        assertEquals(expected, Calculator.add(Double.parseDouble(a), Double.parseDouble(b)), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} - {1} = {2} (coercion)")
    @CsvSource({
            "'10',   '4',    6",
            "'3',    '10',   -7",
            "'5',    '0',    5",
            "'-5',   '-3',   -2",
            "'0.3',  '0.1',  0.2"
    })
    void shouldSubtractWithCoercion(String a, String b, double expected) {
        assertEquals(expected, Calculator.subtract(Double.parseDouble(a), Double.parseDouble(b)), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} * {1} = {2} (coercion)")
    @CsvSource({
            "'6',    '7',    42",
            "'0',    '999',  0",
            "'-3',   '-4',   12",
            "'3',    '-4',   -12",
            "'0.1',  '0.2',  0.02"
    })
    void shouldMultiplyWithCoercion(String a, String b, double expected) {
        assertEquals(expected, Calculator.multiply(Double.parseDouble(a), Double.parseDouble(b)), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} / {1} = {2} (coercion)")
    @CsvSource({
            "'20',    '5',    4",
            "'0',     '5',    0",
            "'-10',   '-2',   5",
            "'-7',    '2',    -3.5",
            "'10',    '3',    3.333333333"
    })
    void shouldDivideWithCoercion(String a, String b, double expected) {
        assertEquals(expected, Calculator.divide(Double.parseDouble(a), Double.parseDouble(b)), 0.1);
    }

    // ----- Tests valeurs limite Calculator ----- //
    private static Stream<Arguments> addLargeNumberCases() {
        return Stream.of(
                Arguments.of(Double.MAX_VALUE, 0.0,              Double.MAX_VALUE),
                Arguments.of(Double.MIN_VALUE, 0.0,              Double.MIN_VALUE),
                Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.POSITIVE_INFINITY)
        );
    }

    private static Stream<Arguments> subtractLargeNumberCases() {
        return Stream.of(
                Arguments.of(Double.MAX_VALUE, 0.0,              Double.MAX_VALUE),
                Arguments.of(Double.MIN_VALUE, 0.0,              Double.MIN_VALUE),
                Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, 0.0)
        );
    }

    private static Stream<Arguments> multiplyLargeNumberCases() {
        return Stream.of(
                Arguments.of(Double.MAX_VALUE, 1.0,              Double.MAX_VALUE),
                Arguments.of(Double.MIN_VALUE, 1.0,              Double.MIN_VALUE),
                Arguments.of(Double.MAX_VALUE, 2.0,              Double.POSITIVE_INFINITY)
        );
    }

    private static Stream<Arguments> divideLargeNumberCases() {
        return Stream.of(
                Arguments.of(Double.MAX_VALUE, 2.0,              Double.MAX_VALUE / 2.0),
                Arguments.of(Double.MIN_VALUE, 0.5,              Double.MIN_VALUE / 0.5),
                Arguments.of(Double.MAX_VALUE, Double.MAX_VALUE, 1.0)
        );
    }

    @ParameterizedTest(name = "devrait retourner {0} + {1} = {2} (valeurs limites)")
    @MethodSource("addLargeNumberCases")
    void shouldAddWithLimits(double a, double b, double expected) {
        assertEquals(expected, Calculator.add(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} - {1} = {2} (valeurs limites)")
    @MethodSource("subtractLargeNumberCases")
    void shouldSubtractWithLimits(double a, double b, double expected) {
        assertEquals(expected, Calculator.subtract(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} * {1} = {2} (valeurs limites)")
    @MethodSource("multiplyLargeNumberCases")
    void shouldMultiplyWithLimits(double a, double b, double expected) {
        assertEquals(expected, Calculator.multiply(a, b), 0.1);
    }

    @ParameterizedTest(name = "devrait retourner {0} / {1} = {2} (valeurs limites)")
    @MethodSource("divideLargeNumberCases")
    void shouldDivideWithLimits(double a, double b, double expected) {
        assertEquals(expected, Calculator.divide(a, b), 0.1);
    }

}
