package org.publichealthbioinformatics.irida.plugin.resistancescreen;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.PostProcessingException;
import ca.corefacility.bioinformatics.irida.model.sample.MetadataTemplateField;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.PipelineProvidedMetadataEntry;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisOutputFile;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.type.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.results.updater.AnalysisSampleUpdater;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

/**
 * This implements a class used to perform post-processing on the analysis
 * pipeline results to extract information to write into the IRIDA metadata
 * tables. Please see
 * <https://github.com/phac-nml/irida/blob/development/src/main/java/ca/corefacility/bioinformatics/irida/pipeline/results/AnalysisSampleUpdater.java>
 * or the README.md file in this project for more details.
 */
public class ResistanceScreenPluginUpdater implements AnalysisSampleUpdater {

	private final MetadataTemplateService metadataTemplateService;
	private final SampleService sampleService;
	private final IridaWorkflowsService iridaWorkflowsService;

	/**
	 * Builds a new {@link ResistanceScreenPluginUpdater} with the given services.
	 * 
	 * @param metadataTemplateService The metadata template service.
	 * @param sampleService           The sample service.
	 * @param iridaWorkflowsService   The irida workflows service.
	 */
	public ResistanceScreenPluginUpdater(MetadataTemplateService metadataTemplateService, SampleService sampleService,
										 IridaWorkflowsService iridaWorkflowsService) {
		this.metadataTemplateService = metadataTemplateService;
		this.sampleService = sampleService;
		this.iridaWorkflowsService = iridaWorkflowsService;
	}

	/**
	 * Code to perform the actual update of the {@link Sample}s passed in the
	 * collection.
	 * 
	 * @param samples  A collection of {@link Sample}s that were passed to this
	 *                 pipeline.
	 * @param analysis The {@link AnalysisSubmission} object corresponding to this
	 *                 analysis pipeline.
	 */
	@Override
	public void update(Collection<Sample> samples, AnalysisSubmission analysis) throws PostProcessingException {
		if (samples == null) {
			throw new IllegalArgumentException("samples is null");
		} else if (analysis == null) {
			throw new IllegalArgumentException("analysis is null");
		} else if (samples.size() != 1) {
			// In this particular pipeline, only one sample should be run at a time so I
			// verify that the collection of samples I get has only 1 sample
			throw new IllegalArgumentException(
					"samples size=" + samples.size() + " is not 1 for analysisSubmission=" + analysis.getId());
		}

		// extract the 1 and only sample (if more than 1, would have thrown an exception
		// above)
		final Sample sample = samples.iterator().next();

		// extracts paths to the analysis result files
		AnalysisOutputFile geneDetectionStatusAnalysisOutputFile = analysis.getAnalysis().getAnalysisOutputFile("gene_detection_status");
		Path geneDetectionStatusFilePath = geneDetectionStatusAnalysisOutputFile.getFile();

		try {
			Map<String, MetadataEntry> metadataEntries = new HashMap<>();

			// get information about the workflow (e.g., version and name)
			IridaWorkflow iridaWorkflow = iridaWorkflowsService.getIridaWorkflow(analysis.getWorkflowId());
			String workflowVersion = iridaWorkflow.getWorkflowDescription().getVersion();
			String workflowName = iridaWorkflow.getWorkflowDescription().getName();

			// gets information from the "gene_detection_status.tsv" output file and constructs metadata
			// objects
			List<Map<String, String>> geneDetectionStatuses = parseGeneDetectionStatusFile(geneDetectionStatusFilePath);

			for (Map<String, String> geneDetectionStatus : geneDetectionStatuses) {
				String geneName = geneDetectionStatus.get("gene_name");
				String geneDetected = geneDetectionStatus.get("detected");
				String alleles = geneDetectionStatus.get("alleles");
				PipelineProvidedMetadataEntry geneDetectedEntry = new PipelineProvidedMetadataEntry(geneDetected, "xs:boolean", analysis);
				PipelineProvidedMetadataEntry allelesEntry = new PipelineProvidedMetadataEntry(alleles, "xs:string", analysis);
				// key will be string like 'resistance-screen/KPC/detected'
				String key;
				key = workflowName + "/" + geneName + "/" + "detected";
				metadataEntries.put(key, geneDetectedEntry);
				key = workflowName + "/" + geneName + "/" + "alleles";
				metadataEntries.put(key, allelesEntry);
			}

			Map<MetadataTemplateField, MetadataEntry> metadataMap = metadataTemplateService.getMetadataMap(metadataEntries);

			// merges with existing sample metadata
			sample.mergeMetadata(metadataMap);

			// does an update of the sample metadata
			sampleService.updateFields(sample.getId(), ImmutableMap.of("metadata", sample.getMetadata()));
		} catch (IOException e) {
			throw new PostProcessingException("Error parsing gene detection status file", e);
		} catch (IridaWorkflowNotFoundException e) {
			throw new PostProcessingException("Could not find workflow for id=" + analysis.getWorkflowId(), e);
		}
	}


	/**
	 * Parses out values from the hash file into a {@link List<Map>} linking 'gene_name' to 'detection_status'.
	 * 
	 * @param geneDetectionStatusFilePath The {@link Path} to the file containing the hash values from
	 *                 the pipeline. This file should contain contents like:
	 * 
	 *                 <pre>
	 * gene_name	detected	alleles
	 * KPC	True	KPC-2
	 * OXA	False
	 *                 </pre>
	 * 
	 * @return An {@link List<Map>} linking 'geneName' to 'detectionStatus'.
	 * @throws IOException             If there was an error reading the file.
	 * @throws PostProcessingException If there was an error parsing the file.
	 */
	@VisibleForTesting
	List<Map<String, String>> parseGeneDetectionStatusFile(Path geneDetectionStatusFilePath) throws IOException, PostProcessingException {
		List<Map<String, String>> geneDetectionStatuses = new ArrayList<Map<String, String>>();

		BufferedReader geneDetectionStatusReader = new BufferedReader(new FileReader(geneDetectionStatusFilePath.toFile()));

		try {
			String headerLine = geneDetectionStatusReader.readLine();

			String[] fieldNames = headerLine.split("\t");
			HashMap<String, String> geneDetectionStatus = new HashMap<>();
			String geneDetectionStatusLine;
			while (( geneDetectionStatusLine = geneDetectionStatusReader.readLine()) != null) {
				String[] geneDetectionStatusEntries = geneDetectionStatusLine.split("\t", -1);
				for (int i = 0; i < fieldNames.length; i++) {
					geneDetectionStatus.put(fieldNames[i], geneDetectionStatusEntries[i]);
				}
				HashMap<String, String> clonedGeneDetectionStatus = (HashMap<String, String>) geneDetectionStatus.clone();
				geneDetectionStatuses.add(clonedGeneDetectionStatus);
			}

		} finally {
			// make sure to close, even in cases where an exception is thrown
			geneDetectionStatusReader.close();
		}

		return geneDetectionStatuses;
	}

	/**
	 * The {@link AnalysisType} this {@link AnalysisSampleUpdater} corresponds to.
	 * 
	 * @return The {@link AnalysisType} this {@link AnalysisSampleUpdater}
	 *         corresponds to.
	 */
	@Override
	public AnalysisType getAnalysisType() {
		return ResistanceScreenPlugin.RESISTANCE_SCREEN;
	}
}
