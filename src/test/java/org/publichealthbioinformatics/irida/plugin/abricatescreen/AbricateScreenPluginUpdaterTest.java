package org.publichealthbioinformatics.irida.plugin.abricatescreen;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;



public class AbricateScreenPluginUpdaterTest {
    @Mock
    private SampleService sampleService;
    @Mock
    private MetadataTemplateService metadataTemplateService;
    @Mock
    private IridaWorkflowsService iridaWorkflowsService;

    private AbricateScreenPluginUpdater updater;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        updater = new AbricateScreenPluginUpdater(metadataTemplateService, sampleService, iridaWorkflowsService);
    }

    @Test
    public void testUpdate() throws Throwable {
        //Analysis analysis = new Analysis();
    }

    @Test
    public void testParseGeneDetectionStatusFile() throws Throwable {
        Path geneDetectionStatusFilePath = Paths.get(ClassLoader.getSystemResource("gene_detection_status.tsv").toURI());
        List<Map<String, String>> geneDetectionStatuses = updater.parseGeneDetectionStatusFile(geneDetectionStatusFilePath);
        for (Map<String, String> geneDetectionStatus : geneDetectionStatuses) {
            for (String key : geneDetectionStatus.keySet()) {
                System.out.println(geneDetectionStatus.get(key));
            }
        }
    }
}