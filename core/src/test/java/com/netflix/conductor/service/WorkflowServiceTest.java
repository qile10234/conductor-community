package com.netflix.conductor.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.netflix.conductor.annotations.Service;
import com.netflix.conductor.common.metadata.workflow.RerunWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.SkipTaskRequest;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.SearchResult;
import com.netflix.conductor.common.run.Workflow;
import com.netflix.conductor.common.run.WorkflowSummary;
import com.netflix.conductor.core.config.Configuration;
import com.netflix.conductor.core.config.ValidationModule;
import com.netflix.conductor.core.execution.ApplicationException;
import com.netflix.conductor.core.execution.WorkflowExecutor;
import com.netflix.conductor.interceptors.ServiceInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.*;

import static com.netflix.conductor.utility.TestUtils.getConstraintViolationMessages;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class WorkflowServiceTest {

    private WorkflowExecutor mockWorkflowExecutor;

    private ExecutionService mockExecutionService;

    private MetadataService mockMetadata;

    private WorkflowService workflowService;

    @Before
    public void before() {
        this.mockWorkflowExecutor = Mockito.mock(WorkflowExecutor.class);
        this.mockExecutionService = Mockito.mock(ExecutionService.class);
        this.mockMetadata = Mockito.mock(MetadataService.class);
        Configuration mockConfig = Mockito.mock(Configuration.class);

        when(mockConfig.getIntProperty(anyString(), anyInt())).thenReturn(5_000);
        this.workflowService = new WorkflowService(this.mockWorkflowExecutor, this.mockExecutionService,
                this.mockMetadata, mockConfig);
        Injector injector =
                Guice.createInjector(
                        new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(WorkflowExecutor.class).toInstance(mockWorkflowExecutor);
                                bind(ExecutionService.class).toInstance(mockExecutionService);
                                bind(MetadataService.class).toInstance(mockMetadata);
                                bind(Configuration.class).toInstance(mockConfig);
                                install(new ValidationModule());
                                bindInterceptor(Matchers.any(), Matchers.annotatedWith(Service.class), new ServiceInterceptor(getProvider(Validator.class)));
                            }
                        });
        workflowService = injector.getInstance(WorkflowService.class);
    }

    @Test
    public void testStartWorkflowNull() {
        try{
            workflowService.startWorkflow(null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("StartWorkflowRequest cannot be null"));
        }
    }

    @Test
    public void testStartWorkflowName() {
        try{
            Map<String, Object> input = new HashMap<>();
            input.put("1", "abc");
            workflowService.startWorkflow(null, 1, "abc", input);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("Workflow name cannot be null or empty"));
        }
    }

    @Test
    public void testStartWorkflow() {
        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName("test");
        workflowDef.setVersion(1);

        StartWorkflowRequest startWorkflowRequest = new StartWorkflowRequest();
        startWorkflowRequest.setName("w123");

        Map<String, Object> input = new HashMap<>();
        input.put("1", "abc");
        startWorkflowRequest.setInput(input);
        String workflowID = "w112";

        when(mockMetadata.getWorkflowDef(anyString(), anyInt())).thenReturn(workflowDef);
        when(mockWorkflowExecutor.startWorkflow(anyString(), anyInt(), anyString(),
                anyMapOf(String.class, Object.class), any(String.class), any(String.class),
                anyMapOf(String.class, String.class))).thenReturn(workflowID);
        assertEquals("w112", workflowService.startWorkflow(startWorkflowRequest));
    }

    @Test
    public void testStartWorkflowParam() {
        WorkflowDef workflowDef = new WorkflowDef();
        workflowDef.setName("test");
        workflowDef.setVersion(1);

        Map<String, Object> input = new HashMap<>();
        input.put("1", "abc");
        String workflowID = "w112";

        when(mockMetadata.getWorkflowDef(anyString(), anyInt())).thenReturn(workflowDef);
        when(mockWorkflowExecutor.startWorkflow(anyString(), anyInt(), anyString(),
                anyMapOf(String.class, Object.class), any(String.class))).thenReturn(workflowID);
        assertEquals("w112", workflowService.startWorkflow("test", 1, "c123", input));
    }

    @Test(expected = ApplicationException.class)
    public void testApplicationExceptionStartWorkflowMessageParam() {
        try {
            when(mockMetadata.getWorkflowDef(anyString(), anyInt())).thenReturn(null);

            Map<String, Object> input = new HashMap<>();
            input.put("1", "abc");

            workflowService.startWorkflow("test", 1, "c123", input);
        } catch (ApplicationException ex) {
            String message = "No such workflow found by name: test, version: 1";
            assertEquals(message, ex.getMessage());
            throw ex;
        }
        fail("ApplicationException did not throw!");
    }

    @Test
    public void testGetWorkflowsNoName() {
        try{
            workflowService.getWorkflows("", "c123", true, true);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("Workflow name cannot be null or empty"));
        }
    }

    @Test
    public void testGetWorklfowsSingleCorrelationId() {
        Workflow workflow = new Workflow();
        workflow.setCorrelationId("c123");

        List<Workflow> workflowArrayList = new ArrayList<Workflow>() {{
            add(workflow);
        }};

        when(mockExecutionService.getWorkflowInstances(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(workflowArrayList);
        assertEquals(workflowArrayList, workflowService.getWorkflows("test", "c123",
                true, true));
    }

    @Test
    public void testGetWorklfowsMultipleCorrelationId() {
        Workflow workflow = new Workflow();
        workflow.setCorrelationId("c123");

        List<Workflow> workflowArrayList = new ArrayList<Workflow>() {{
            add(workflow);
        }};

        List<String> correlationIdList = new ArrayList<String>() {{
            add("c123");
        }};

        Map<String, List<Workflow>> workflowMap = new HashMap<>();
        workflowMap.put("c123", workflowArrayList);

        when(mockExecutionService.getWorkflowInstances(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(workflowArrayList);
        assertEquals(workflowMap, workflowService.getWorkflows("test", true,
                true, correlationIdList));
    }

    @Test
    public void testGetExecutionStatus() {
        Workflow workflow = new Workflow();
        workflow.setCorrelationId("c123");

        when(mockExecutionService.getExecutionStatus(anyString(), anyBoolean())).thenReturn(workflow);
        assertEquals(workflow, workflowService.getExecutionStatus("w123", true));
    }

    @Test
    public void testGetExecutionStatusNoWorkflowId() {
        try{
            workflowService.getExecutionStatus("", true);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test(expected = ApplicationException.class)
    public void testApplicationExceptionGetExecutionStatus() {
        try {
            when(mockExecutionService.getExecutionStatus(anyString(), anyBoolean())).thenReturn(null);
            workflowService.getExecutionStatus("w123", true);
        } catch (ApplicationException ex) {
            String message = "Workflow with Id: w123 not found.";
            assertEquals(message, ex.getMessage());
            throw ex;
        }
        fail("ApplicationException did not throw!");
    }

    @Test
    public void testDeleteWorkflow() {
        workflowService.deleteWorkflow("w123", true);
        verify(mockExecutionService, times(1)).removeWorkflow(anyString(), anyBoolean());
    }

    @Test
    public void testInvalidDeleteWorkflow() {
        try{
            workflowService.deleteWorkflow(null, true);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test
    public void testInvalidPauseWorkflow() {
        try{
            workflowService.pauseWorkflow(null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test
    public void testInvalidResumeWorkflow() {
        try{
            workflowService.resumeWorkflow(null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test
    public void testInvalidSkipTaskFromWorkflow() {
        try{
            SkipTaskRequest skipTaskRequest = new SkipTaskRequest();
            workflowService.skipTaskFromWorkflow(null, null, skipTaskRequest);
        } catch (ConstraintViolationException ex){
            assertEquals(2, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId name cannot be null or empty."));
            assertTrue(messages.contains("TaskReferenceName cannot be null or empty."));
        }
    }

    @Test
    public void testInvalidWorkflowNameGetRunningWorkflows() {
        try{
            workflowService.getRunningWorkflows(null, 123, null, null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("Workflow name cannot be null or empty."));
        }
    }

    @Test
    public void testGetRunningWorkflowsTime() {
        workflowService.getRunningWorkflows("test", 1, 100L, 120L);
        verify(mockWorkflowExecutor, times(1)).getWorkflows(anyString(), anyInt(), anyLong(), anyLong());
    }

    @Test
    public void testGetRunningWorkflows() {
        workflowService.getRunningWorkflows("test", 1, null, null);
        verify(mockWorkflowExecutor, times(1)).getRunningWorkflowIds(anyString());
    }

    @Test
    public void testDecideWorkflow() {
        workflowService.decideWorkflow("test");
        verify(mockWorkflowExecutor, times(1)).decide(anyString());
    }

    @Test
    public void testPauseWorkflow() {
        workflowService.pauseWorkflow("test");
        verify(mockWorkflowExecutor, times(1)).pauseWorkflow(anyString());
    }

    @Test
    public void testResumeWorkflow() {
        workflowService.resumeWorkflow("test");
        verify(mockWorkflowExecutor, times(1)).resumeWorkflow(anyString());
    }

    @Test
    public void testSkipTaskFromWorkflow() {
        workflowService.skipTaskFromWorkflow("test", "testTask", null);
        verify(mockWorkflowExecutor, times(1)).skipTaskFromWorkflow(anyString(), anyString(),
                any(SkipTaskRequest.class));
    }

    @Test
    public void testRerunWorkflow() {
        RerunWorkflowRequest request = new RerunWorkflowRequest();
        workflowService.rerunWorkflow("test", request);
        verify(mockWorkflowExecutor, times(1)).rerun(any(RerunWorkflowRequest.class));
    }

    @Test
    public void testRerunWorkflowNull() {
        try{
            workflowService.rerunWorkflow(null, null);
        } catch (ConstraintViolationException ex){
            assertEquals(2, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
            assertTrue(messages.contains("RerunWorkflowRequest cannot be null."));
        }
    }

    @Test
    public void testRestartWorkflowNull() {
        try{
            workflowService.restartWorkflow(null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test
    public void testRetryWorkflowNull() {
        try{
            workflowService.retryWorkflow(null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test
    public void testResetWorkflowNull() {
        try{
            workflowService.resetWorkflow(null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }


    @Test
    public void testTerminateWorkflowNull() {
        try{
            workflowService.terminateWorkflow(null, null);
        } catch (ConstraintViolationException ex){
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("WorkflowId cannot be null or empty."));
        }
    }

    @Test
    public void testRerunWorkflowReturnWorkflowId() {
        RerunWorkflowRequest request = new RerunWorkflowRequest();
        String workflowId = "w123";
        when(mockWorkflowExecutor.rerun(any(RerunWorkflowRequest.class))).thenReturn(workflowId);
        assertEquals(workflowId, workflowService.rerunWorkflow("test", request));
    }

    @Test
    public void testRestartWorkflow() {
        workflowService.restartWorkflow("w123");
        verify(mockWorkflowExecutor, times(1)).rewind(anyString());
    }

    @Test
    public void testRetryWorkflow() {
        workflowService.retryWorkflow("w123");
        verify(mockWorkflowExecutor, times(1)).retry(anyString());
    }

    @Test
    public void testResetWorkflow() {
        workflowService.resetWorkflow("w123");
        verify(mockWorkflowExecutor, times(1)).resetCallbacksForInProgressTasks(anyString());
    }

    @Test
    public void testTerminateWorkflow() {
        workflowService.terminateWorkflow("w123", "test");
        verify(mockWorkflowExecutor, times(1)).terminateWorkflow(anyString(), anyString());
    }

    @Test
    public void testSearchWorkflows() {
        Workflow workflow = new Workflow();
        workflow.setCorrelationId("c123");

        WorkflowSummary workflowSummary = new WorkflowSummary(workflow);
        List<WorkflowSummary> listOfWorkflowSummary = new ArrayList<WorkflowSummary>() {{
            add(workflowSummary);
        }};
        SearchResult<WorkflowSummary> searchResult = new SearchResult<WorkflowSummary>(100, listOfWorkflowSummary);

        when(mockExecutionService.search(anyString(), anyString(), anyInt(), anyInt(), anyListOf(String.class))).thenReturn(searchResult);
        assertEquals(searchResult, workflowService.searchWorkflows(0,100,"asc", "*", "*"));
    }

    @Test
    public void testInvalidSizeSearchWorkflows() {
        try {
            workflowService.searchWorkflows(0,6000,"asc", "*", "*");
        } catch (ConstraintViolationException ex) {
            assertEquals(1, ex.getConstraintViolations().size());
            Set<String> messages = getConstraintViolationMessages(ex.getConstraintViolations());
            assertTrue(messages.contains("Cannot return more than 5000 workflows. Please use pagination."));
        }
    }

    @Test
    public void searchWorkflowsByTasks() {
        workflowService.searchWorkflowsByTasks(0,100,"asc", "*", "*");
        verify(mockExecutionService, times(1)).searchWorkflowByTasks(anyString(), anyString(), anyInt(), anyInt(), anyListOf(String.class));
    }

}