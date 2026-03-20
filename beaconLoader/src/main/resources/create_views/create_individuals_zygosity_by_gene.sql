create view individual_zygosity_by_gene (individual_id, gene_id, variation_id, zygosity_id, label, zygosity_count) as
SELECT i.id           AS individual_id,
       gvgid.gene_ids AS gene_id,
       gv.variation_id,
       case_level_data.zygosity_id,
       zyg_ot.label,
       CASE
           WHEN zyg_ot.label::text = '0|0'::text THEN 0
           WHEN zyg_ot.label::text = '0/0'::text THEN 0
           WHEN zyg_ot.label::text = '0|1'::text THEN 1
           WHEN zyg_ot.label::text = '0/1'::text THEN 1
           WHEN zyg_ot.label::text = '1|0'::text THEN 1
           WHEN zyg_ot.label::text = '1/0'::text THEN 1
           WHEN zyg_ot.label::text = '1|1'::text THEN 2
           WHEN zyg_ot.label::text = '1/1'::text THEN 2
           ELSE '-1'::integer
END        AS zygosity_count
FROM case_level_data
         JOIN ontology_term zyg_ot ON case_level_data.zygosity_id::text = zyg_ot.id::text
         JOIN genomic_variation_case_level_data gvcld ON case_level_data.id = gvcld.case_level_data_id
         JOIN genomic_variation gv ON gvcld.genomic_variation_variant_internal_id::text = gv.variant_internal_id::text
         JOIN genomic_variation_gene_ids gvgid
              ON gv.variant_internal_id::text = gvgid.genomic_variation_variant_internal_id::text
         JOIN analysis a ON case_level_data.analysis_id::text = a.id::text
         JOIN run r ON a.run_id::text = r.id::text
         JOIN biosample b ON r.biosample_id::text = b.id::text
         JOIN individual i ON b.individual_id::text = i.id::text;