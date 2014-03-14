package mx.com.interware.training.jbpm.test;

import java.io.File;
import java.io.IOException;

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
public class DelayTimerEventProcessTest {

	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;
	
	@Before
	public void setUp() throws IOException {
		this.ksession = createKSession();
		
		// Console log
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// file logger
		File logFile = File.createTempFile("process_output", "delay_timer_event");
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
	public void delayTimerEventProcessTest() throws InterruptedException{
		// se inicia el proces usando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.delaytimereventprocess");
		
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
		
		int sleep = 5000;
		System.out.println("Sleeping " + sleep / 1000 + " seconds.");
		Thread.sleep(sleep);
		System.out.println("Awake!");
		
		// el proceso continua hasta que se alcanza el nodo de fin
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		
		WorkflowProcessInstance processInstance = (WorkflowProcessInstance)process;
		Long timerExecutionTime = (Long) processInstance.getVariable("timerExecutionTime");
		System.out.println("Value of var timerExecutionTime: " + timerExecutionTime);
		Assert.assertTrue(timerExecutionTime >= 2000);
		
	}
	
	
	public StatefulKnowledgeSession createKSession() {
		// crear el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// se agrega el recurso bpmn al kbuilder
		ClassPathResource delayTimerEventProcess = new ClassPathResource("process/delayTimerEventProcess.bpmn");
		
		kbuilder.add(delayTimerEventProcess, ResourceType.BPMN2);
		System.out.println("Compilando los recursos");
		
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error construyendo el kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error construyendo el kbase!");
		}
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		return kbase.newStatefulKnowledgeSession();
	}
}
