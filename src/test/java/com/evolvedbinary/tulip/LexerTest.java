package com.evolvedbinary.tulip;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static com.evolvedbinary.tulip.LexerConstants.BUFFER_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;

public class LexerTest {

    @DisplayName("Testing input buffering")
    @org.junit.jupiter.api.Test
    public void testInputBuffering() throws IOException {
        Path path = Paths.get("src/test/java/com/evolvedbinary/tulip/file.txt");
        Source fs = FileSource.open(path);
        XmlSpecification xmlSpecification = new XmlSpecification_1_0();
        XPath10Lexer lexer = new XPath10Lexer(fs, BUFFER_SIZE, xmlSpecification);
        String testing[] = {"\"When\"", "\"What\"", "\"Hello\"", "\"Hi\"", "1.1", ".2", "3", "4", "11", "2", "33", "44", "\"Evolved\"", "22", "\"Evolved Binary\""};
        int count = 0;

        Instant start = Instant.now();
        while(true) {
            Token t = lexer.next();
            if(t.getTokenType()==null)
                break;
            String lexeme = new String(t.getLexeme());
            //Comment the next two lines when checking for time spent to tokenise
            System.out.println(lexeme + "\t\t\t\t\t\t\t" + t.getTokenType());
//            assertEquals(lexeme, testing[count++]);
        }
        Instant end = Instant.now();
        System.out.println("Execution Time: " + Duration.between(start, end).toMillis() + " ms");
    }
}
