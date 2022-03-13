package utils;


import static utils.Parser.generateChunkOfAllStrFiles;

import org.junit.jupiter.api.Test;

class ParserTest
{

    private static final String DIRECTORY = "srt-files";
    private static final String FILE_NAME = "s0e1.srt";
    private static final String FILE_PATH = DIRECTORY + "/" + FILE_NAME;

    @Test
    public void runProcess()
    {
        generateChunkOfAllStrFiles();
//        File path = new File(FILE_PATH);
//        List<String> lineOfFile = readSrtFile(path.toPath());
//
//        Map<Integer, Subtitle> chunks = chunkOfSubtitles(lineOfFile);
//        writeAllChunkToFile("s0e1",chunks);
//        System.out.println(chunks);

    }

}