CREATE OR REPLACE VIEW case_level_data_for_aggregation AS
SELECT * FROM case_level_data;



create or replace view individual_zygosity_by_gene as
SELECT i.id           AS individual_id,
       b.id AS biosample_id,
       case_level_data.id AS case_level_data_id,
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

CREATE OR REPLACE VIEW individual_zygosity_by_gene_for_aggregation AS
SELECT * FROM individual_zygosity_by_gene;



create or replace view gene as
SELECT DISTINCT gvgid.gene_ids AS gene_id
FROM genomic_variation_gene_ids gvgid;

DO $$
DECLARE
assay_code_id TEXT;
    label TEXT;
    create_view_query TEXT;
BEGIN
    -- Loop through each unique measurement_name
FOR assay_code_id, label IN
SELECT distinct m.assay_code_id, ot.label
FROM individual_measures
         inner join public.measure m on m.id = individual_measures.measures_id
         inner join public.ontology_term ot on ot.id = m.assay_code_id
    LOOP
        -- Construct the dynamic SQL query to create the view
        create_view_query := format('
            CREATE OR REPLACE VIEW %I AS
            SELECT individual_measures.*
            from individual_measures
            inner join public.measure m on m.id = individual_measures.measures_id
            WHERE m.assay_code_id=%s;', concat('Individual_measure', replace(label,'-','_')), quote_literal(assay_code_id));

-- Execute the dynamic SQL query to create the view
EXECUTE create_view_query;

RAISE NOTICE 'Created view for measurement: %', concat('Individual_measure', label);
END LOOP;
END $$;

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