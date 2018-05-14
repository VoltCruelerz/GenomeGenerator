package genebuilder;

import java.util.ArrayList;
import java.util.Random;

// This is just raw genetic data.  It does not recognize anything outside itself and does not handle dependencies.
public class Gene {
    public static String geneticAlphabet = "AGCT";
    
    public int id;
    
    public boolean isJunk;
    
    public String code = "";
    
    public int INT = 0;
    
    public int VIT = 0;
    
    public int CHA = 0;
    
    public boolean isActivator = false;
    public boolean isInhibitor = false;
    public float influence = 0;
    public Gene targetGene = null;
    ArrayList<Gene> affectors = new ArrayList<>();
    
    public Gene(int id, Random r) {
        this.id = id;
        
        for(int i = 0; i < GeneBuilder.basesPerGene; i++){
            code = code + GetRandomGeneticCharacter(r);
        }
        
        int statType = r.nextInt(5);
        if(statType == 0){
            INT = GetCenteredStat(r);
        }
        else if(statType == 1){
            VIT = GetCenteredStat(r);
        }
        else if(statType == 2){
            CHA = GetCenteredStat(r);
        }
        else if(statType == 3){
            isActivator = true;
            influence = r.nextFloat() + 1;
        }
        else if(statType == 4){
            isInhibitor = true;
            influence = r.nextFloat();
        }
    }
    
    public int GetCenteredStat(Random r){
        return r.nextInt(15)-5;
    }
    
    public String toString(){
        return "Gene[" + id + "]:\tCode=" + GetPaddedCode() + "\t{\tINT=" + INT + ",\tVIT=" + VIT + ",\tCHA=" + CHA + "\t}\t= " + GetTotal();
    }
    
    public int GetTotal(){
        return INT + VIT + CHA;
    }
    
    public String GetPaddedCode(){
        int codeLeng = code.length();
        if(codeLeng != GeneBuilder.basesPerGene){
            return padRight(code, GeneBuilder.basesPerGene - codeLeng);
        }
        else{
            return code;
        }
    }
    
    public String padRight(String s, int n) {
         String retVal = s;
         for(int i = 0; i < n; i++){
             retVal = retVal + " ";
         }
         return retVal;
    }
    
    public char GetRandomGeneticCharacter(Random r){
        return geneticAlphabet.charAt(r.nextInt(geneticAlphabet.length()));
    }
    
    public int codeNum(){
        int place = 1;
        int totalNum = 0;
        for(int i = code.length() -1; i >= 0 ; i--){
            char geneticLetter = code.charAt(i);
            int codeIndex = geneticAlphabet.indexOf(geneticLetter);
            totalNum += codeIndex * place;
            place *= 10;
        }
        return totalNum;
    }
    
    public String codeString(){
        String codeString = "";
        for(int i = 0; i < code.length(); i++){
            char geneticLetter = code.charAt(i);
            if(codeString.equals("")){
                codeString = "" + geneticAlphabet.indexOf(geneticLetter);
            }else{
                codeString = codeString + "," + geneticAlphabet.indexOf(geneticLetter);
            }
        }
        return codeString;
    }
    
    public void setToJunk(){
        INT = 0;
        VIT = 0;
        CHA = 0;
        isActivator = false;
        isInhibitor = false;
    }
    
    public static void echo(int i){
        echo("" + i);
    }
    
    public static void echo(String s){
        System.out.println(s);
    }
}
