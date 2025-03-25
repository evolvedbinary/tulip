package com.evolvedbinary.tulip;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Test {
    public static void main(String args[]) throws IOException {
        Path path = Paths.get("src/test/java/com/evolvedbinary/tulip/file.txt");
        Source fs = FileSource.open(path);
        XmlSpecification xmlSpecification = new XmlSpecification_1_0();
        XPath10Lexer lexer = new XPath10Lexer(fs, 10, xmlSpecification);
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
        System.out.println(lexer.next().toString());
    }
}
