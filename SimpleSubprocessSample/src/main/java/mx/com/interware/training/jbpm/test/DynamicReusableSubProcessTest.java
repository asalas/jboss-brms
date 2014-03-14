package mx.com.interware.training.jbpm.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import mx.com.interware.training.jbpm.model.Car;
import mx.com.interware.training.jbpm.model.Person;

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
public class DynamicReusableSubProcessTest {
	
	private KnowledgeRuntimeLogger fileLogger;
	private StatefulKnowledgeSession ksession;
	
	@Before
	public void setUp() throws IOException {
		this.ksession = this.createKSession();
		
		// Log de la consola
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(this.ksession);
		
		// File logger
		File logFile = File.createTempFile("process-output-dynamic", "");
		System.out.println("Log file= " + logFile.getAbsolutePath() + ".log");
		this.fileLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(this.ksession, logFile.getAbsolutePath());
	}
	
	@After
	public void cleanUp() {
		if (this.fileLogger != null) {
			this.fileLogger.close();
		}
	}
	
	@Test
	public void reusableProcessTest() {
		// Probando el proceso con una persona (Person)		
		String personName = "Leonardo Elian";
		
		Person person = new Person();
		person.setName(personName);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("object", person);
		
		// Se inicia el proceso utilizando su ID
		ProcessInstance process = this.ksession.startProcess("mx.com.interware.training.jbpm.dynamicreusablesubprocessparent", parameters);
		
		// Por que existe una activación, el proceso espera la ejecucion de fireAllRules()
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
		
		this.ksession.fireAllRules();
		
		// Ahora el proceso se ha completado
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		
		// El subproceso correcto se escoje cuando el nombre de la persona se cambia.
		System.out.println("Nombre de la persona: " + person.getName());
		Assert.assertFalse(person.getName().equals(personName));
		
		// Ahora se prueba el proceso con un carro (Car)
		String carBrand = "Ford";
		String carModel = "Mustang";
		
		Car car = new Car();
		car.setBrand(carBrand);
		car.setModel(carModel);
		
		parameters = new HashMap<String, Object>();
		parameters.put("object", car);
		
		// Se inicia el proceso usando su ID
		process = ksession.startProcess("mx.com.interware.training.jbpm.dynamicreusablesubprocessparent", parameters);
		
		// Debido a que existe una activacion, el proceso espera la ejecucion de fireAllRules()
		Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
		
		this.ksession.fireAllRules();
		
		// Ahora el proceso se ha completado
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
		
		// El subprocesos correcto se escige cuando el nombre de la persona ha cambiado.
		System.out.println("Marca del auto: " + car.getBrand());
		System.out.println("Modelo del auto: " + car.getModel());
		
		Assert.assertFalse(car.getModel().equals(carModel));
		Assert.assertFalse(car.getBrand().equals(carBrand));
	}

	/**
	 * Crea un ksession a partir de un kbase conteniendo la definicion de un proceso
	 * @return
	 */
	public StatefulKnowledgeSession createKSession() {
		// Se crea el kbuilder
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		// se recuperan los recursos de los archivos de configuracion.
		
		// se carga el archivo de reglas
		ClassPathResource rulesFile = new ClassPathResource("rules/dynamic/subProcessSelectionRules.drl");
		ClassPathResource dynamicReusableSubProcessParent = new ClassPathResource("process/dynamic/dynamicReusableSubProcess-Parent.bpmn");
		ClassPathResource personTaggerProcess = new ClassPathResource("process/dynamic/personTaggerProcess.bpmn");
		ClassPathResource carTaggerProcess = new ClassPathResource("process/dynamic/carTaggerProcess.bpmn");
		
		// se agregan los recusos al kbuilder
		kbuilder.add(rulesFile, ResourceType.DRL);
		kbuilder.add(dynamicReusableSubProcessParent, ResourceType.BPMN2);
		kbuilder.add(personTaggerProcess, ResourceType.BPMN2);
		kbuilder.add(carTaggerProcess, ResourceType.BPMN2);
		
		System.out.println("Compilando los recursos...");
		
		// Se verifican los errores
		if (kbuilder.hasErrors()) {
			if (kbuilder.getErrors().size() > 0) {
				for (KnowledgeBuilderError error: kbuilder.getErrors()) {
					System.out.println("Error construyendo el kbase: " + error.getMessage());
				}
			}
			throw new RuntimeException("Error construyendo el kbase!");
		}
		
		// Se crea el knowledge base y se agrega el paquete generado
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		// Se retorna un nuevo statefull session
		return kbase.newStatefulKnowledgeSession();
	}
}