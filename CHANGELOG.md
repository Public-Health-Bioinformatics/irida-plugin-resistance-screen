# 0.2.0

- For each gene in the 'gene screening file', both the 'detection status' (True/False) and the detected allele
  are written to the IRIDA line-list. Alleles are also reported in the 'gene_detection_status.tsv' output file.
- Limited pipeline parameters to simplify and standardize operation
  - Resistance Gene Database is fixed on CARD database.
  - Cannot disable post-assembly correction or read trimming.
  - Cannot change contig name format
  - Cannot provide 'extra spades options' to shovill assembler
- Added thresholds for resistance gene %Coverage and %Identity during secondary screening phase
- The gene screening file used for the analysis is included in the pipeline output

# 0.1.1

- Fixed [issue](https://github.com/Public-Health-Bioinformatics/irida-plugin-resistance-screen/issues/1) where sequence data was not being transferred to galaxy

# 0.1.0

- Initial release of example plugin.

