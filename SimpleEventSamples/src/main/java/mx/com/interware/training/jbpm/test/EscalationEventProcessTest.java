package mx.com.interware.training.jbpm.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author asalas
 *
 */
public class EscalationEventProcessTest {

	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;
	
	@Before
	public void setUp() throws IOException {
		this.ksession = createKSession();
		
		// console log
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// file logger
		File logFile = File.createTempFile("process-output", "escalation-event-process");
		System.out.println("Log file= " + logFile.getAbsolutePath() + ".log");
		this.fileLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.ksession, logFile.getAbsolutePath());
	}
	
	@After
	public void tearDown() {
		if (this.fileLogger != null) {
			this.fileLogger.close();
		}
	}
	
	@Test
	public void validScenarioTest() {
		List<String> errorList = new ArrayList<String>();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("inputData", new Object());
		parameters.put("errorList", errorList);
		
		// se inicia el proceso usando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.escalationeventprocess", parameters);
		
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		
		Assert.assertTrue(errorList.isEmpty());
	}
	
	@Test
	public void testInvalidScenarioTest() {
		List<String> errorList = new ArrayList<String>();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("inputData", null);
		parameters.put("errorList", errorList);
		
		// se inicia el proceso usando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.escalationeventprocess", parameters);
		
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		WorkflowProcessInstance processInstance = (WorkflowProcessInstance)process;
		errorList = (List<String>) processInstance.getVariable("errorList");
		
		Assert.assertFalse(errorList.isEmpty());
		Assert.assertEquals(1, errorList.size());
		Assert.assertTrue(errorList.contains("Invalid input data!"));
	}
	
	public StatefulKnowledgeSession createKSession() {
		// se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// se agrega el recurso del proceso bpmn
		ClassPathResource escalationEventProcess = new ClassPathResource("process/escalationEventProcess.bpmn");
		kbuilder.add(escalationEventProcess, ResourceType.BPMN2);
		
		// se revisan los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error construyendo el kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error construyendo el kbase");
		}
		
		// se agrega el paquete correspondiente
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		return kbase.newStatefulKnowledgeSession();
	}
	
}
