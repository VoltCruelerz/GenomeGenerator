package genebuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.Writer;

import java.util.ArrayList;
import java.util.List;

public class Saver implements Runnable{
    
    ArrayList<String> printableData = null;
    String printableFileName = null;
    
    public Saver(String fileName, ArrayList<String> dataToAsyncPrint) {
        printableData = dataToAsyncPrint;
        printableFileName = fileName;
    }
    
    public void run(){
        SpeedWrite(printableFileName, printableData);
    }
    
    public static void Save(String filename, int val){
        Save(filename, "" + val);
    }
    
    public static void Save(String filename, double val){
        Save(filename, "" + val);
    }
    
    public static void Save(String filename, ArrayList<String> list){
        for(String entry : list){
            Save(filename, entry);
        }
    }
    
    public static void Save(String filename, String[] list){
        Save(filename, "" + list.length);
        for(String entry : list){
            Save(filename, entry);
        }
    }
    
    public static void Save(String filename, int[] list){
        Save(filename, "" + list.length);
        for(int entry : list){
            Save(filename, entry);
        }
    }
    
    public static void Save(String filename, boolean val){
        Save(filename, val ? 1 : 0);
    }
    
    // The final, base Save a string function that everything else is an overload of
    public static void Save(String filename, String outputLine){
        String localPath = filename;
        try(FileWriter fw = new FileWriter(localPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(outputLine);
            //System.out.println(outputLine);
        } catch (IOException e) {
            System.err.println("Failed to print to " + filename + " the following: \n" + outputLine);
            System.err.println("Error: " + e.toString());
        }
    }
    
    public static void SpeedWrite(String fileName, ArrayList<String> records) {
        int bufSize = 4 * (int)(Math.pow(1024, 2));
        try {
            FileWriter writer = new FileWriter(new File(fileName));
            BufferedWriter bufferedWriter = new BufferedWriter(writer, bufSize);

            System.out.print("Writing buffered (buffer size: " + bufSize + ")... ");
            SpeedWriteLines(records, bufferedWriter);
        }
        catch(Exception e){
            System.out.println("Error: " + e.toString());
        }
    }

    private static void SpeedWriteLines(ArrayList<String> records, Writer writer) throws IOException {
        for (String record : records) {
            writer.write(record);
            writer.write(System.lineSeparator());
        }
        writer.flush();
        writer.close();
    }
}
