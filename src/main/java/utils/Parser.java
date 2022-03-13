package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;;

import model.Subtitle;

public class Parser
{
    private static final String DIRECTORY = "srt-files";
    private static final String FILE_NAME = "s0e1.srt";
    private static final String FILE_PATH = DIRECTORY + "/" + FILE_NAME;
    private static final String GENERATED_DIRECTORY = "generated";
    private static final String SCRIPTS = "scripts";
    private static final double GAP_SECOND = 0.5;
    

    public static void generateChunkOfAllStrFiles() {
        List<Path> srtFileNameList = listFilesForFolder(DIRECTORY);

        srtFileNameList.forEach(strFileName->{
            List<String> strFile = readSrtFile(strFileName);
            Map<Integer, List<Subtitle>> chunks = chunkOfSubtitles(strFile);
            String episodeName = strFileName.toString().split("/|\\.")[1];
            
            writeAllChunkToFile(episodeName, chunks);

            writePythonScriptCall(episodeName, chunks);
        });

    }
    
    public static List<Path> listFilesForFolder(String path) {
        List<Path> fileNames = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(path)))
        {
            fileNames.addAll(paths.filter(Files::isRegularFile).collect(Collectors.toList()));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return fileNames;
    }

    public static List<String> readSrtFile(Path path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(path)),
                             StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                lines.add(line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return lines;
    }

    public static Map<Integer, List<Subtitle>> chunkOfSubtitles(List<String> lineOfFile) {
        Map<Integer, List<Subtitle>> chunkMap = new HashMap<>();
        List<String> chunk = new ArrayList<>();
        int mapKey=0;

        for (int i = 0; i < lineOfFile.size() ; i++)
        {
            String line = lineOfFile.get(i);
            if (line.isEmpty() && !chunk.isEmpty())
            {
                Subtitle subtitle = generateSubtitleObject(chunk);

                addToList(chunkMap, mapKey, subtitle);

                int sizeOfChunk = chunkMap.get(mapKey).size();

                if (sizeOfChunk % 3 == 0) {
                    mapKey++;
                }

                chunk = new ArrayList<>();
                continue;
            }
            chunk.add(line);
        }
        return chunkMap;
    }

    public static synchronized void addToList(Map<Integer, List<Subtitle>> chunkMap, Integer mapKey, Subtitle subtitle)
    {
        List<Subtitle> subtitleList = chunkMap.get(mapKey);
        if (subtitleList == null) {
            subtitleList = new ArrayList<>();
            subtitleList.add(subtitle);
            chunkMap.put(mapKey, subtitleList);
        }else {
            if (!subtitleList.contains(subtitle)) subtitleList.add(subtitle);
        }
    }

    public static void writeAllChunkToFile(String episodeName, Map<Integer, List<Subtitle>> chunks)
    {
        String path = GENERATED_DIRECTORY + "/" + episodeName;
        prepareDir(path);
        for (int key : chunks.keySet())
        {
            List<Subtitle> subtitles = chunks.get(key);
            
            FileWriter myWriter = null;
            try
            {
                double firstStartSubTime = getStartSecondOfStr(subtitles);
                double firstEndSubTime = getStartSecondOfStr(subtitles);
                double videoSize = subtitles.get(subtitles.size() - 1).getEndSecond()-firstStartSubTime;
                if(videoSize>10)continue;
                
                String fileName = path + "/" + (key + 1) + ".srt";
                myWriter = new FileWriter(fileName);
                for (int i = 0; i < subtitles.size(); i++)
                {
                    Subtitle subtitle = subtitles.get(i);
                    myWriter.write(String.valueOf(i + 1));
                    myWriter.write(System.getProperty("line.separator"));
                    myWriter.write(shiftTimeForStart(firstStartSubTime, firstEndSubTime, subtitle.getTime()));
                    myWriter.write(System.getProperty("line.separator"));
                    myWriter.write(subtitle.getText());
                    myWriter.write(System.getProperty("line.separator"));
                    myWriter.write(System.getProperty("line.separator"));
                }
                myWriter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }
    
    public static void writePythonScriptCall(String episodeName, Map<Integer, List<Subtitle>> chunks) {
        //prepareDir(SCRIPTS);

        String fileName = SCRIPTS + "/python-script.txt";
        FileWriter myWriter = null;
        try
        {
            myWriter = new FileWriter(fileName);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        for (int key : chunks.keySet())
        {
            List<Subtitle> subtitles = chunks.get(key);
            try
            {
                double start = getStartSecondOfStr(subtitles) - GAP_SECOND;
                double end   = getEndSecondOfStr(subtitles) + GAP_SECOND;
                if(end-start>10) continue;
                myWriter.write("python3 parser.py friends-s1/episode/" + episodeName + ".mkv " + start + " " + end + " " + (key + 1));
                myWriter.write(System.getProperty("line.separator"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            myWriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private static double getEndSecondOfStr(List<Subtitle> subtitles)
    {
        return Double.parseDouble(new DecimalFormat("#.###").format(subtitles.get(subtitles.size() - 1).getEndSecond()));
    }

    private static double getStartSecondOfStr(List<Subtitle> subtitles)
    {
        return Double.parseDouble(new DecimalFormat("#.###").format(subtitles.get(0).getStartSecond()));
    }

    private static void prepareDir(String path) {
        File generatedDir = new File(path);
        if (!generatedDir.exists())
        {
            generatedDir.mkdirs();
        }
        try
        {
            FileUtils.cleanDirectory(generatedDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private static String shiftTimeForStart(double firstStartSubTime, double firstEndSubTime, String currentTime) {
        String[] splitOfTime = getSplitOfTime(currentTime);
        String txtStart = splitOfTime[0];
        String txtEnd = splitOfTime[1];

        double newStartTime = shiftTime(firstStartSubTime, txtStart) + GAP_SECOND;
        double newEndTime = shiftTime(firstEndSubTime, txtEnd) + GAP_SECOND;
        
        return convertTime(newStartTime) + " --> " + convertTime(newEndTime);
    }

    private static double shiftTime(double firstStartSubTime, String txtStart)
    {        
        return convertTime(txtStart) - firstStartSubTime;
    }

    private static int getOrderKey(List<String> chunk) {
        return Integer.parseInt(chunk.get(0));
    }

    private static Subtitle generateSubtitleObject(List<String> chunk) {
        int key = getOrderKey(chunk);
        String strTime = getTime(chunk);
        double startSecond = getStartSecond(strTime);
        double endSecond = getEndSecond(strTime);
        String text = getSubtitleChunkText(chunk);

        return new Subtitle(key, strTime, text, startSecond, endSecond);
    }

    private static String getSubtitleChunkText(List<String> chunk) {
        return String.join(System.lineSeparator(), chunk.subList(2, chunk.size()));
    }

    private static double getEndSecond(String time) {
        String[] splitOfTime = getSplitOfTime(time);
        return convertTime(splitOfTime[1]);
    }

    private static double getStartSecond(String time) {
        String[] splitOfTime = getSplitOfTime(time);
        return convertTime(splitOfTime[0]);
    }

    private static String[] getSplitOfTime(String time) {
        return time.trim().split("-->");
    }

    private static String getTime(List<String> chunk) {
        return chunk.get(1);
    }

    private static double convertTime(String time) {
        String[] partOfTime = time.split(":");

        double hour = Double.parseDouble(partOfTime[0]);
        double minute = Double.parseDouble(partOfTime[1]);
        double second = Double.parseDouble(partOfTime[2].replace(",", "."));

        return hour * 60 + minute * 60 + second;
    }

    private static String convertTime(double time)
    {
        double minute = Math.floor(time / 60);
        double second = time - minute * 60;

        return "00:" +  getMinuteDigitNumber(minute) + ":" + new DecimalFormat("#.###").format(second);
    }

    private static String getMinuteDigitNumber(double time)
    {
        String strTime;
        int integerTime = (int) time;
        strTime = time < 10 ? "0" + integerTime : String.valueOf(integerTime);

        return strTime;
    }
}
