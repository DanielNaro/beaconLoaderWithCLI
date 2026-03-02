# Data Directory

Place your Beacon JSON data files in this directory:

- `datasets.json` - Dataset definitions
- `individuals.json` - Individual records  
- `biosamples.json` - Biosample data
- `runs.json` - Sequencing run information
- `analyses.json` - Analysis metadata
- `cohorts.json` - Cohort definitions
- `genomicVariationsVcf.json` - Genomic variation data

When running with Docker, this directory will be mounted to `/data` in the container.

