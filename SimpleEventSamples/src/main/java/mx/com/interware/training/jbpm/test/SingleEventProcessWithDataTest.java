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
public class SingleEventProcessWithDataTest {

	private static final String PROCESS_WITH_DATA_ID = "mx.com.interware.training.jbpm.singleeventprocessswithdata";
	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;

	@Before
	public void setUp() throws IOException {
		this.ksession = createKSession();
		
		// console log
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// file log
		File logFile = File.createTempFile("process-output", "single-event-process-with-data");
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
    public void validPaymentTest(){
        //Start the process using its id
        ProcessInstance process = ksession.startProcess(PROCESS_WITH_DATA_ID);
        
        //The process is in the gateway waiting for the event
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        
        //The event arrives
        ksession.signalEvent("payment", 110);
        
        //The process continues until it reaches the end node
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
    }
    
    @Test
    public void invalidPaymentTest(){
        //Start the process using its id
        ProcessInstance process = ksession.startProcess(PROCESS_WITH_DATA_ID);
        
        //The process is in the gateway waiting for the event
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        
        //The event arrives
        ksession.signalEvent("payment", 90);
        
        //The process continues until it reaches the end node
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
    }

	/**
	 * Crea el ksession
	 * 
	 * @return
	 */
	public StatefulKnowledgeSession createKSession() {
		// se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// se agregan los recusos bpmn
		ClassPathResource singleEventProcessWithData = new ClassPathResource("process/singleEventProcessWithData.bpmn");
		kbuilder.add(singleEventProcessWithData, ResourceType.BPMN2);
		System.out.println("Compilando los recursos!");
		
		// se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                    System.out.println("Error construyendo el kbase: " + error.getMessage());
                }
			}
			throw new RuntimeException("Error construyendo el kbase!");
		}
		
		// se crea el kbase
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		return kbase.newStatefulKnowledgeSession();
	}
}
