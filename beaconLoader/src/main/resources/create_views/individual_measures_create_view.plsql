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