package com.evolvedbinary.tulip;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static com.evolvedbinary.tulip.constants.LexerConstants.BUFFER_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.evolvedbinary.tulip.lexer.Token;
import com.evolvedbinary.tulip.lexer.TokenType;
import com.evolvedbinary.tulip.lexer.XPath10Lexer;
import com.evolvedbinary.tulip.source.FileSource;
import com.evolvedbinary.tulip.source.Source;
import com.evolvedbinary.tulip.spec.XmlSpecification;
import com.evolvedbinary.tulip.spec.XmlSpecification_1_0;
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
            if(t.getTokenType()== TokenType.EOF)
                break;
            String lexeme = t.getLexeme();
            //Comment the next two lines when checking for time spent to tokenise
            System.out.println(lexeme + "\t\t\t\t\t\t\t" + t.getTokenType());
//            assertEquals(lexeme, testing[count++]);
        }
        Instant end = Instant.now();
        System.out.println("Execution Time: " + Duration.between(start, end).toMillis() + " ms");
    }
}
