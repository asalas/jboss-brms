package mx.com.interware.training.jbpm;

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
 * Prueba Unitaria de JUnit para realizar la prueba del proceso de ejemplo
 * SimpleProcess
 * 
 * @author asalas
 * 
 */
public class SimpleProcessTest {

	private KnowledgeRuntimeLogger knowledgeRuntimeLogger;
	private StatefulKnowledgeSession ksession;
	
	@Before
	public void setup() throws IOException {
		this.ksession = this.createKSession();
		
		// Console log.
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// File logger
		File logFile = File.createTempFile("salida-proceso", "");
		String logFileAbsolutePath = logFile.getAbsolutePath();
		System.out.println("Archivo Log: " + logFileAbsolutePath + ".log");
		this.knowledgeRuntimeLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.ksession, logFileAbsolutePath);
	}
	
	@After
	public void cleanup() {
		if (this.knowledgeRuntimeLogger != null) {
			this.knowledgeRuntimeLogger.close();
		}
	}
	
	@Test
	public void simpleProcessTest() {
		// Se inicia el proceso usando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.simpleprocess");
		
		// El proceso se ejecuta de inicio a fin debido a que no hay estados de espera
		// En esta linea de codigo solo se verifica que el estado el proceso sea COMPLETO al terminar su ejecucion
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
	}
	
	private StatefulKnowledgeSession createKSession() {
		// Crear el knowledge builder "kbuilder"
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// Se agrega el proceso simpleProcess.bpm al kbuilder
		ClassPathResource processPathResource = new ClassPathResource("process/simpleProcess.bpmn");
		kbuilder.add(processPathResource, ResourceType.BPMN2);
		System.out.println("Compilando los recursos ...");
		
		// Se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error construyendo kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error construyendo el kbase!");
		}
		
		// Se crea el "knowledge base" y se agrega el paquete generado
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		
		return ksession;
	}
	
}
