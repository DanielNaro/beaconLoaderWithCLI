CREATE OR REPLACE VIEW zygosity_as_count as
SELECT distinct case_level_data.zygosity_id,
                CASE
                    WHEN ot.label = '0|0' THEN 0
                    WHEN ot.label = '0|1' THEN 1
                    WHEN ot.label = '1|0' THEN 1
                    WHEN ot.label = '1|1' THEN 2
                    ELSE -1
                    END AS zygosity_cout
FROM case_level_data
         inner join public.ontology_term ot on case_level_data.zygosity_id = ot.id;