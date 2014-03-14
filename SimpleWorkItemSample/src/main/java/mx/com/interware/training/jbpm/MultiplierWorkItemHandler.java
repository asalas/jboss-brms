package mx.com.interware.training.jbpm;

import mx.com.interware.training.jbpm.model.MultiplierOperation;

import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

/**
 * 
 * @author asalas
 * 
 */
public class MultiplierWorkItemHandler implements WorkItemHandler {

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		MultiplierOperation multiplierOperation = (MultiplierOperation) workItem
				.getParameter("internalMultiplierOperation");
		multiplierOperation.setResult(multiplierOperation.getOp1()
				* multiplierOperation.getOp2());

		// debido a que no se crea un nuevo MultiplierOperation, pero se
		// modifica el existente,
		// no es necesario agregar ningun parametro de salida
		manager.completeWorkItem(workItem.getId(), null);
	}

}
