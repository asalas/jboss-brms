package mx.com.interware.training.jbpm.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import mx.com.interware.training.jbpm.MultiplierWorkItemHandler;
import mx.com.interware.training.jbpm.model.MultiplierOperation;

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
public class SimpleWorkItemParameterTest {

	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;

	@Before
	public void setUp() throws IOException {
		this.ksession = this.createKSession();

		// Console Log.
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);

		// File logger
		File logFile = File.createTempFile("process_output",
				"_workitem_handler");
		System.out.println("Log file = " + logFile.getAbsolutePath() + ".log");
		fileLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.ksession,
				logFile.getAbsolutePath());
	}

	@After
	public void cleanUp() {
		if (this.fileLogger != null) {
			this.fileLogger.close();
		}
	}

	@Test
	public void simpleWorkItemParameterTest() {
		// el objeto operacion
		MultiplierOperation multiplierOperation = new MultiplierOperation(3D, 2D);

		// parametros de entrada
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("multiplierOperation", multiplierOperation);

		// se inicia el proceso usando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.simpleworkitemparameterprocess",	parameters);

		// El proces se ejecuta de inicio a fin por que no hay una tarea que
		// requiera esperar por intereaccion con el usuario
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		
		// La operacion debe tener un resultado
		Assert.assertEquals(6, multiplierOperation.getResult(), 0.1);
	}

	/**
	 * Crea el ksession a partir del kbase contenido en la definicion del
	 * proceso
	 * 
	 * @return
	 */
	public StatefulKnowledgeSession createKSession() {
		// se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();

		// se agrega el recursos simpleWorkItemParameter.bpmn al kbuilder
		ClassPathResource process = new ClassPathResource(
				"process/simpleWorkItemParameter.bpmn");
		kbuilder.add(process, ResourceType.BPMN2);
		System.out.println("Compilando recursos");

		// Se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error : kbuilder.getErrors()) {
					System.out.println("Error construyendo el kbase: "
							+ error.getMessage());
				}
			}
			throw new RuntimeException("Error construyendo el kbase!");
		}

		// se crea el knowledge base y se agrega el paquete generado.
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

		StatefulKnowledgeSession _ksession = kbase
				.newStatefulKnowledgeSession();

		// se agrega el handler para el nodo Multiply
		_ksession.getWorkItemManager().registerWorkItemHandler(
				"multiplier_process", new MultiplierWorkItemHandler());

		return _ksession;
	}

}
