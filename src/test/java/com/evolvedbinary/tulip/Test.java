package com.evolvedbinary.tulip;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;

public class Test {

    @DisplayName("Testing input buffering")
    @org.junit.jupiter.api.Test
    public void testInputBuffering() throws IOException {
        Path path = Paths.get("src/test/java/com/evolvedbinary/tulip/file.txt");
        Source fs = FileSource.open(path);
        XmlSpecification xmlSpecification = new XmlSpecification_1_0();
        XPath10Lexer lexer = new XPath10Lexer(fs, 4096, xmlSpecification);
        String testing[] = {"\"What\"", "\"are\"", "\"You\"", "5", "\"Planning\"", "10", "\"gugu\"", "56", "31", "23", "42", "\"my name is robin\"", "88", "11111", "904802", "\"this is an alphanumberic 123\""};
        int count = 0;
        while(true) {
            Token t = lexer.next();
            if(t.getTokenType()==null)
                break;
            String lexeme = new String(t.getLexeme());
            System.out.println(lexeme);
            assertEquals(lexeme, testing[count++]);
        }
    }
}
