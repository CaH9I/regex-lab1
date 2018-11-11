package by.bsu.regex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Run {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new IllegalArgumentException("path to file is not provided");
        }

        String path = args[0];
        List<String> lines = Files.readAllLines(Paths.get(path));

        for (String line : lines) {
            String[] input = line.split("\\s+");
            String regex = input[0];
            BsuRegex pattern = BsuRegex.compile(regex);
            StringBuilder result = new StringBuilder(regex);
            for (int i = 1; i < input.length; i++) {
                result.append(' ');
                result.append(pattern.matcher(input[i]).matches());
            }
            System.out.println(result);
        }
    }
}
