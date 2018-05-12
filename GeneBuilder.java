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
    
    // The number of genes that contribute to an organism
    final int genomeLength = 1000;
    
    // The number of options per gene
    final int allelesPerGene = 2;
    
    // The number of genes to generate
    final int geneCount = genomeLength*allelesPerGene;
    
    // The number of creatures to generate
    final int populationSize = 20000;
    
    // Finalized genes for creature generation
    ArrayList<Gene> genes = new ArrayList<>();
    ArrayList<Gene> activators = new ArrayList<>();
    ArrayList<Gene> inhibitors = new ArrayList<>();
    ArrayList<Gene> targets = new ArrayList<>();
    
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
            //echo(g.toString());
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

            boolean isJunk = r.nextBoolean();
            for(int j = 0; j < bundle.Alleles.size(); j++){
                Allele a = bundle.Alleles.get(j);
                alleles.add(a);
                genes.add(a.gene);
                if(isJunk){
                    a.gene.setToJunk();
                }
                else if(a.gene.isActivator){
                    activators.add(a.gene);
                }
                else if(a.gene.isInhibitor){
                    inhibitors.add(a.gene);
                }
                else{
                    targets.add(a.gene);
                }
            }
        }
        
        echo("Building Allele Dependencies...");
        for(int i = 0; i < activators.size(); i++){
            Gene activator = activators.get(i);
            int targetIndex = r.nextInt(targets.size());
            Gene target = targets.get(targetIndex);
            activator.targetGene = target;
            target.affectors.add(activator);
        }
        for(int i = 0; i < inhibitors.size(); i++){
            Gene inhibitor = inhibitors.get(i);
            int targetIndex = r.nextInt(targets.size());
            Gene target = targets.get(targetIndex);
            inhibitor.targetGene = target;
            target.affectors.add(inhibitor);
        }
        
        echo("Building Creatures...");
        for(int i = 0; i < populationSize; i++){
            Creature c = new Creature();
            // Select genes from bundles
            for(int j = 0; j < genomeLength; j++){
                Allele a = alleles.get(j*genesPerAllele + r.nextInt(genesPerAllele));
                c.Genome.add(a);
            }
            
            for(int j = 0; j < c.Genome.size(); j++){
                Gene g = c.Genome.get(j).gene;
                float multiplier = 1;
                for(int k = 0; k < g.affectors.size(); k++){
                    float influence = g.affectors.get(k).influence;
                    multiplier = multiplier * influence;
                    //echo("Multiplier: " + multiplier + " after Influence: " + influence);
                }
                
                c.INT+=g.INT*multiplier;
                c.VIT+=g.VIT*multiplier;
                c.CHA+=g.CHA*multiplier;
            }
            creatures.add(c);
            //echo(c.toString());
        }
        
        echo("Saving Genomes to File...");
        
        String trainingData = "TrainingData.txt";
        String testData = "TestData.txt";
        
        int halfCreatures = creatures.size()/2;
        
        echo("Erasing Old Data");
        try{new File(trainingData).delete();}catch(Exception e){echo("Exception e: " + e);}
        try{new File(testData).delete();}catch(Exception e){echo("Exception e: " + e);}
        
        echo("Clearing Progress Data");
        try{new File(trainingData).delete();}catch(Exception e){echo("Exception e: " + e);}
        try{new File(testData).delete();}catch(Exception e){echo("Exception e: " + e);}
        
        echo("Build Training Data");
        ArrayList<String> trainingOutput = new ArrayList<>();
        trainingOutput.add(halfCreatures + ", 4, INT, VIT, CHA, CODE");
        for(int i = 0; i < halfCreatures; i++){
            Creature cur = creatures.get(i);
            String line = cur.INT + "," + cur.VIT + "," + cur.CHA + "," + cur.GetStringCode();
            trainingOutput.add(line);
        }
        Thread t1 = new Thread(new Saver(trainingData, trainingOutput));
        t1.start();
        
        echo("Build Test Data");
        ArrayList<String> testOutput = new ArrayList<>();
        Saver.Save(testData, halfCreatures + ", 4, INT, VIT, CHA, CODE");
        for(int i = halfCreatures; i < creatures.size(); i++){
            Creature cur = creatures.get(i);
            String line = cur.INT + "," + cur.VIT + "," + cur.CHA + "," + cur.GetStringCode();
            testOutput.add(line);
        }
        Saver.SpeedWrite(testData, testOutput);
        
        try{
            t1.join();
        }
        catch(Exception e){
            echo("Error: " + e);
        }
    }
    
    public static void echo(int i){
        echo("" + i);
    }
    
    public static void echo(String s){
        System.out.println(s);
    }
}
