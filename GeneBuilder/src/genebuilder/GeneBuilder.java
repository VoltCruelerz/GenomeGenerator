package genebuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Random;

public class GeneBuilder {
    Random r;
    
    // The number of genes to generate
    final int geneCount = 12;
    
    // The number of genes that contribute to an organism
    final int genomeLength = 3;
    
    // The number of creatures to generate
    final int populationSize = 40000;
    
    // Finalized genes for creature generation
    ArrayList<Gene> genes = new ArrayList<>();
    
    // The total list of alleles in existence
    ArrayList<Allele> alleles = new ArrayList<>();
    
    // The list of Allele Bundles that make up the entire genetic material for the species
    ArrayList<AlleleBundle> geneticMaterial = new ArrayList<>();
    
    // The list of creature that have been generated.
    ArrayList<Creature> creatures = new ArrayList<>();
    
    
    public static void main(String[] args){
        GeneBuilder gb = new GeneBuilder(5);
    }
    
    public GeneBuilder(int seed) {        
        echo("Initializing Gene Builder...");
        r = new Random(seed);
        
        
        echo("Building Genes...");
        ArrayList<Gene> starterGenes = new ArrayList<>();
        for(int i = 0; i < geneCount; i++){
            Gene g = new Gene(i, r);
            starterGenes.add(g);
            echo(g.toString());
        }
        
        
        echo("Assigning Genes to Alleles...");
        int genesPerAllele = geneCount/genomeLength;
        for(int i = 0; i < genomeLength; i++){
            AlleleBundle bundle = new AlleleBundle(i);
            for(int j = 0; j < genesPerAllele; j++){
                int index = i*genesPerAllele + j;
                Gene g = starterGenes.get(index);
                Allele a = new Allele(g);
                bundle.Add(a);
            }
            bundle.PruneAndReplaceWorst(r, genesPerAllele/4);
            
            for(int j = 0; j < bundle.Alleles.size(); j++){
                Allele a = bundle.Alleles.get(j);
                alleles.add(a);
                genes.add(a.gene);
                //echo("Allele[" + i + "][" + j + "]: " + a.gene.INT + ", " + a.gene.VIT + ", " + a.gene.CHA);
            }
        }
        
        
        // echo("Building Allele Dependencies...");
        // TODO build allele dependencies
        
        echo("Building Creatures...");
        long totalViability = 0;
        for(int i = 0; i < populationSize; i++){
            int viability = 0;
            int attempts = 0;
            Creature c = null;
            while(viability < Creature.ViabilityThreshold && attempts++ < 10){
                c = new Creature();
                
                // Select genes from bundles
                for(int j = 0; j < genomeLength; j++){
                    Allele a = alleles.get(j*genesPerAllele + r.nextInt(genesPerAllele));
                    c.Genome.add(a);
                    c.INT+=a.gene.INT;
                    c.VIT+=a.gene.VIT;
                    c.CHA+=a.gene.CHA;
                }
                //echo(c.GetStats());
                viability = c.GetOptimum();
            }
            totalViability += viability;
            creatures.add(c);
            //echo("Creature[" + i + "] Viability: " + viability);
            //echo(c.toString());
        }
        echo("Average Viability: " + totalViability/creatures.size());
        
        echo("Saving Genomes to File...");
        ArrayList<String> output = new ArrayList<>();
        
        String trainingData = "TrainingData.txt";
        String testData = "TestData.txt";
        
        new File(trainingData).delete();
        new File(testData).delete();
        
        echo("Build Training Data");
        int halfCreatures = creatures.size()/2;
        Save(trainingData, halfCreatures + ", 4, INT, VIT, CHA, CODE");
        for(int i = 0; i < halfCreatures; i++){
            Creature cur = creatures.get(i);
            String line = cur.SummaryType() + "," + cur.GetNumCode();
            output.add(line);
        }
        Save(trainingData, output);
        
        echo("Build Test Data");
        output.clear();
        Save(testData, halfCreatures + ", 4, INT, VIT, CHA, CODE");
        for(int i = halfCreatures; i < creatures.size(); i++){
            Creature cur = creatures.get(i);
            String line = cur.SummaryType() + "," + cur.GetNumCode();
            output.add(line);
        }
        Save(testData, output);
    }
    
    public static void echo(int i){
        echo("" + i);
    }
    
    public static void echo(String s){
        System.out.println(s);
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
}
