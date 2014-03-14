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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author asalas
 *
 */
public class SingleEventInSubProcessTest {

	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;
	
	@Before
	public void setUp() throws IOException {
		this.ksession = createKSession();
		
		// console log
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// log file
		File logFile = File.createTempFile("process-output", "single-event-in-subprocess");
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
	public void simpleProcessTest() {
		// se inici el proceso usando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.singleeventprocess-parent");
		
		// el proceso espera el evento en el gateway
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
		
		// el evento arriba
		this.ksession.signalEvent("payment", null);
		
		// el proceso continua hasta que llega al nodo fin
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
	}
	
	/**
	 * Crea la ksession
	 * 
	 * @return
	 */
	public StatefulKnowledgeSession createKSession() {
		// se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// se agrega el proceso bpmn al kbuilder
		ClassPathResource singleEventProcessParent = new ClassPathResource("process/singleEventProcess-Parent.bpmn");
		ClassPathResource singleProcessEventChild = new ClassPathResource("process/singleEventProcess-Child.bpmn");
		
		kbuilder.add(singleProcessEventChild, ResourceType.BPMN2);
		kbuilder.add(singleEventProcessParent, ResourceType.BPMN2);
		
		System.out.println("Compilando los recursos!");
		
		// se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error creando el kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error creando el kbase!");
		}
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		return kbase.newStatefulKnowledgeSession();
	}
	
}
