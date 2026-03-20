create view gene(gene_id) as
SELECT DISTINCT gvgid.gene_ids AS gene_id
FROM genomic_variation_gene_ids gvgid;