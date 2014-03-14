package mx.com.interware.training.jbpm.test;

import java.io.File;
import java.io.IOException;

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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author asalas
 *
 */
public class SingleEventProcessTest {

	private StatefulKnowledgeSession ksession;
	private KnowledgeRuntimeLogger fileLogger;
	
	@Before
	public void setUp() throws IOException {
		this.ksession = createKSession();
		
		// console log
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// log file
		File logFile = File.createTempFile("process-output", "single-event-process");
		System.out.println("File Log = " + logFile.getAbsolutePath() + ".log");
		this.fileLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.ksession, logFile.getAbsolutePath());
	}
	
	@After
	public void tearDown() {
		if (this.fileLogger != null) {
			this.fileLogger.close();
		}
	}
	
	@Test
	public void simpleEventProcessTest() {
		// se inicia el proceso utilizando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.singleeventprocess");
		
		//The process is in the gateway waiting for the event
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        
        //The event arrives
        this.ksession.signalEvent("payment", null);
        
        //The process continues until it reaches the end node
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
	}
	
	/**
	 * Metodo que crea la ksession
	 * 
	 * @return
	 */
	public StatefulKnowledgeSession createKSession() {
		// se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// se agrega el recurso del proceso bpmn
		ClassPathResource singleEventProcess = new ClassPathResource("process/singleEventProcess.bpmn");		
		kbuilder.add(singleEventProcess, ResourceType.BPMN2);
		
		// se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error constriyendo el kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error construyendo el kbase!");
		}
		
		// se agregan los paquetes al kbase
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		return kbase.newStatefulKnowledgeSession();
	}
	
}
