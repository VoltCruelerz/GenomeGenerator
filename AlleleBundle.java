package genebuilder;

import java.util.ArrayList;
import java.util.Random;

public class AlleleBundle {
    public final int id;
    
    public ArrayList<Allele> Alleles = new ArrayList<>();
    
    public AlleleBundle(int id) {
        this.id = id;
    }
    
    public void Add(Allele a){
        Alleles.add(a);
    }
    
    public String toString(){
        String retVal = "AlleleBundle[" + id + "]\n";
        for(int i = 0; i < Alleles.size(); i++){
            retVal = retVal + "\t" + Alleles.get(i).gene.toString() + "\n";
        }
        
        return retVal;
    }
    
    public void PruneAndReplaceWorst(Random r, int times){
        for(int i = 0; i < times; i++){
            Allele worst = Alleles.get(0);
            int worstVal = Integer.MAX_VALUE;
            for(int j = 0; j < Alleles.size(); j++){
                Allele cur = Alleles.get(j);
                int curVal = cur.gene.GetTotal();
                if(curVal < worstVal){
                    worst = cur;
                    worstVal = curVal;
                }
            }
            Alleles.remove(worst);
            
            Gene replacementGene = new Gene(worst.gene.id, r);
            Allele replacementAllele = new Allele(replacementGene);
            Alleles.add(replacementAllele);
        }
        
    }
}
