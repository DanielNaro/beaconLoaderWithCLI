CREATE OR REPLACE VIEW zygosity_as_count as
SELECT distinct case_level_data.zygosity_id,
                CASE
                    WHEN ot.label = '0|0' THEN 0
                    WHEN ot.label = '0|1' THEN 1
                    WHEN ot.label = '1|0' THEN 1
                    WHEN ot.label = '1|1' THEN 2
                    ELSE -1
                    END AS zygosity_count
FROM case_level_data
         inner join public.ontology_term ot on case_level_data.zygosity_id = ot.id;

CREATE OR REPLACE VIEW zygosity_as_count_by_genomic_variation as
SELECT case_level_data.zygosity_id,
       CASE
           WHEN ot.label = '0|0' THEN 0
           WHEN ot.label = '0|1' THEN 1
           WHEN ot.label = '1|0' THEN 1
           WHEN ot.label = '1|1' THEN 2
           ELSE -1
           END AS zygosity_count,
       gv.genomichgvsid,
       gvcld.genomic_variation_variant_internal_id
FROM case_level_data
         inner join genomic_variation_case_level_data gvcld on case_level_data.id = gvcld.case_level_data_id
         inner join genomic_variation gv on gvcld.genomic_variation_variant_internal_id = gv.variant_internal_id
         inner join public.ontology_term ot on case_level_data.zygosity_id = ot.id;