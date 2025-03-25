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
        XPath10Lexer lexer = new XPath10Lexer(fs, 20, xmlSpecification);
        String testing[] = {"\"What\"", "\"are\"", "\"You\"", "5", "\"Planning\"", "10", "\"gugu\""};
        int count = 0;
        while(true) {
            Token t = lexer.next();
            if(t.getTokenType()==null)
                break;
            assertEquals(t.getLexeme(), testing[count++]);
        }
    }
}
