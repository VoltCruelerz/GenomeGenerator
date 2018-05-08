package genebuilder;

import java.util.ArrayList;

public class Creature {
    public static final int ViabilityThreshold = 20;
    
    // Intelligence
    int INT = 0;
    
    // Vitality (Health)
    int VIT = 0;
    
    // Charisma (Attractiveness)
    int CHA = 0;
    
    ArrayList<Allele> Genome = new ArrayList<>();
    
    public Creature() {
        super();
    }
    
    // Likelihood of breeding (probably not going to use this function
    public int GetFitness(){
        if(INT > 0 && VIT > 0 && CHA > 0){
            return INT + 2*VIT + 3*CHA;
        }
        return 0;
    }
    
    // What a human might consider an idealized child
    public int GetOptimum(){
        if(INT > 0 && VIT > 0 && CHA > 0){
            return (int) (2 * INT + 2 * VIT + 2 * CHA);
        }
        return INT + VIT + CHA;
    }
    
    public String GetCode(){
        String code = "";
        for(int i = 0; i < Genome.size(); i++){
            code = code + Genome.get(i).gene.code;
        }
        return code;
    }
    
    public int SummaryType(){
        if(INT <= 0 && VIT <= 5 && CHA <= 5){
            return 6;
        }
        else if(INT >= VIT && INT >= CHA){
            return 7;
        }
        else if(VIT >= INT && VIT >= CHA){
            return 8;
        }
        else{
            return 9;
        }
    }
    
    public String GetNumCode(){
        String code = "";
        code = code + Genome.get(0).gene.codeNum();
        for(int i = 1; i < Genome.size(); i++){
            code = code + ',' + Genome.get(i).gene.codeNum();
        }
        return code;
    }
    
    public String GetStringCode(){
        String code = "";
        code = code + Genome.get(0).gene.codeString();
        for(int i = 1; i < Genome.size(); i++){
            code = code + ',' + Genome.get(i).gene.codeString();
        }
        return code;
    }
    
    public String GetStats(){
        String retVal = "Creature Stats: INT=" + INT + ", VIT=" + VIT + ", CHA=" + CHA;
        return retVal;
    }
    
    public String toString(){
        String retVal = "Creature:\t{\tINT=" + INT + ",\tVIT=" + VIT + ",\tCHA=" + CHA + "\t}\t= " + GetOptimum() + "\n";
        retVal = retVal + GetCode() + "\n";
        for(int i = 0; i < Genome.size(); i++){
            retVal = retVal + "\t" + Genome.get(i).gene.toString() + "\n";
        }
        return retVal;
    }
}
