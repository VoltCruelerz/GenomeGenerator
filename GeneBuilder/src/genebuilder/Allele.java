package genebuilder;

// A wrapper class for a gene that can have dependencies on other alleles
public class Allele {
    Gene gene;
    
    public Allele(Gene gene) {
        this.gene = gene;
    }
}
