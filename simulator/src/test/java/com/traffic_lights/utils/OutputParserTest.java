package com.traffic_lights.utils;

import com.traffic_lights.dto.SimulationOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputParserTest {

    private static final String OUTPUT_DIR = "data/output/";
    private static final String TEST_FILE = "test_output.json";

    @BeforeEach
    @AfterEach
    void cleanUp() throws IOException {
        Path dirPath = Paths.get(OUTPUT_DIR);
        if (Files.exists(dirPath)) {
            try (Stream<Path> stream = Files.list(dirPath)) {
                stream.filter(path -> path.getFileName().toString().endsWith(TEST_FILE))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                System.err.println("Could not delete test file: " + path);
                            }
                        });
            }
        }
    }

    @Test
    void shouldSaveSimulationOutputToFile() throws IOException {
        SimulationOutput output = SimulationOutput.createEmptySimOutput();

        OutputParser.saveOutput(TEST_FILE, output);
        List<Path> generatedFiles;
        try (Stream<Path> stream = Files.list(Paths.get(OUTPUT_DIR))) {
            generatedFiles = stream
                    .filter(p -> p.getFileName().toString().endsWith(TEST_FILE))
                    .toList();
        }

        assertEquals(1, generatedFiles.size());

        Path generatedFilePath = generatedFiles.getFirst();
        assertTrue(Files.exists(generatedFilePath));
        assertTrue(Files.size(generatedFilePath) > 0);
    }
}
