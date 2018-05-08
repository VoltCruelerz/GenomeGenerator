package genebuilder;

import java.util.Random;

// This is just raw genetic data.  It does not recognize anything outside itself and does not handle dependencies.
public class Gene {
    public static String geneticAlphabet = "AGCT";
    
    final int maxCodeLeng = 5;
    
    public int id;
    
    public boolean isJunk;
    
    public String code = "";
    
    public int INT = 0;
    
    public int VIT = 0;
    
    public int CHA = 0;
    
    public Gene(int id, Random r) {
        this.id = id;
        
        // Code length will be [1-5] characters
        int codeLeng = 4;//r.nextInt(maxCodeLeng)+1;
        for(int i = 0; i < codeLeng; i++){
            code = code + GetRandomGeneticCharacter(r);
        }
        
        int statType = r.nextInt(8);
        if(statType == 0){
            INT = GetCenteredStat(r);
        }
        else if(statType == 1){
            VIT = GetCenteredStat(r);
        }
        else if(statType == 2){
            CHA = GetCenteredStat(r);
        }
        else{
            // Simulate junk DNA
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
        if(codeLeng != maxCodeLeng){
            return padRight(code, maxCodeLeng - codeLeng);
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
    }
}
