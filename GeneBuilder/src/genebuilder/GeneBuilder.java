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
    
    // INT, VIT, and CHA
    final int traitCount = 3;
    
    // Set to 3 because 3 base pairs -> 1 amino acid
    public static final int basesPerGene = 3;
    
    // The number of genes that contribute to an organism
    final int genomeLength = 200;
    
    // The number of options per gene
    final int allelesPerGene = 2;
    
    // The number of genes to generate
    final int geneCount = genomeLength*allelesPerGene;
    
    // The number of base pairs in the creature's genome
    final int basePairCount = genomeLength*basesPerGene;
    
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
            // Account for environmental influences on the creature
            // Doing this here rather than per-item because per gene
            // would average out on large genomes.
            String before = "Before: " + c.INT + ", " + c.VIT + ", " + c.CHA + "; ";
            c.INT *= getGaussian(r, 1, 0.5);
            c.VIT *= getGaussian(r, 1, 0.5);
            c.CHA *= getGaussian(r, 1, 0.5);
            String after = "After: " + c.INT + ", " + c.VIT + ", " + c.CHA + "; ";
            //echo(before + after);
            creatures.add(c);
            //echo(c.toString());
        }
        
        echo("Saving Gene Pools to File...");
        
        String trainingData = "TrainingData.csv";
        String testData = "TestData.csv";
        
        echo("Erasing Old Data");
        try{new File(trainingData).delete();}catch(Exception e){echo("Exception e: " + e);}
        try{new File(testData).delete();}catch(Exception e){echo("Exception e: " + e);}
        
        ArrayList<Thread> children = new ArrayList<>();
        int halfCreatures = creatures.size()/2;
        echo("Build Training Data");
        children.add(SaveGenePoolData(trainingData, 0, halfCreatures, true));
        
        echo("Build Test Data");
        children.add(SaveGenePoolData(testData, halfCreatures, creatures.size(), true));
        
        echo("Waiting for Build Completion");
        for(int i = 0; i < children.size(); i++){
            Thread child = children.get(i);
            if(child != null){
                try{
                    child.join();
                    echo("Thread[" + i + "] Complete");
                }
                catch(Exception e){
                    echo("Thread[" + i + "] Error: " + e);
                }
            } else {
                echo("Thread[" + i + "] Already Complete on Main Thread");
            }
        }
        echo("\n===== All Data Files Built =====");
    }
    
    public Thread SaveGenePoolData(String fileName, int start, int stop, boolean spawnThread){
        echo("Initiate Save to " + fileName);
        ArrayList<String> genePoolOutput = new ArrayList<>();
        
        // Add dummy first line as placeholder
        genePoolOutput.add("");
        double genomeTotal = 0;
        for(int i = start; i < stop; i++){
            Creature cur = creatures.get(i);
            genomeTotal += (cur.INT + cur.VIT + cur.CHA)/3;
            String line = cur.INT + "," + cur.VIT + "," + cur.CHA + "," + cur.GetStringCode();
            genePoolOutput.add(line);
        }
        double poolTraitAverage = genomeTotal/(stop-start);
        genePoolOutput.set(0,getDataSummaryLine(poolTraitAverage));
        
        // Spawn child thread if requested
        Thread child = null;
        if(spawnThread){
            child = new Thread(new Saver(fileName, genePoolOutput));
            child.start();
        } else{
            new Saver(fileName, genePoolOutput);
        }
        
        return child;
    }
    
    public String getDataSummaryLine(double traitAverage){
        return "CreatureCount=" + creatures.size()/2 + ",TraitCount=" + traitCount + ",TraitAvg=" + traitAverage + ",BasePairCount=" + basePairCount;
    }
    
    public static double getGaussian(Random r, double center, double width){
        double total = 0;
        int iterations = 3;
        for(int i = 0; i < iterations; i++){
            total += r.nextDouble()*width - width/2;
        }
        total /= iterations;
        
        total += center;
        
        return total;
    }
    
    public static void echo(int i){
        echo("" + i);
    }
    
    public static void echo(String s){
        System.out.println(s);
    }
}
