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
public class EmbeddedSubProcessTest {

	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;
	
	@Before
	public void setUp() throws IOException {
		this.ksession = this.createKSession();
		
		// Console Log
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// File logger
		File logFile = File.createTempFile("process-output-embedded-", "");
		System.out.println("Log file = " + logFile.getAbsolutePath() + ".log");
		this.fileLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.ksession, logFile.getAbsolutePath());
	}
	
	@After
	public void cleanUp() {
		if (this.fileLogger != null) {
			this.fileLogger.close();
		}
	}
	
	@Test
	public void embeddedProcessTest() {
		List<String> messages = new ArrayList<String>();
		messages.add("Mensaje 1");
		messages.add("Mensaje 2");
		messages.add("Mensaje 3");
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("messages", messages);
		
		// se inicia el proceso con su id
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.embeddedsubprocess", parameters);
		
		//The process will run until there are no more nodes to execute.
        //Because this process doesn't have any wait-state, the process is
        //running from start to end
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		
		//Because the process changed the reference of messages variable, we
        //need to get it again.
        //It is a good practice to retrieve the process variables after its 
        //execution instead of use the old variables passed as parameters.
		WorkflowProcessInstance currentProcessInstance = (WorkflowProcessInstance)process;
		messages = (List<String>) currentProcessInstance.getVariable("messages");
        for (String message : messages) {
            System.out.println("Mensaje = "+message);
        }
	}
	
	public StatefulKnowledgeSession createKSession() {
		// Se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// Se recuperan los recursos
		ClassPathResource embeddedSubProcess = new ClassPathResource("process/embeddedSubProcess.bpmn");
		
		// Se agregan los recursos al kbuilder
		kbuilder.add(embeddedSubProcess, ResourceType.BPMN2);
		
		System.out.println("Compilando los recursos");
		
		// se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error construyendo el kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error building kbase!");
		}
		
		// se crea el kbase y se agregan los paquetes generados
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		return kbase.newStatefulKnowledgeSession();
	}
	
}
