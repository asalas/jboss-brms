package mx.com.interware.training.jbpm.handler;

import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;

/**
 * 
 * @author asalas
 *
 */
public class HumanTaskMockHandler implements WorkItemHandler {

	private WorkItemManager workItemManager;
	private Long workItemId;
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO
	}
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		this.workItemId = workItem.getId();
		this.workItemManager = manager;		
	}
	
	public void completeWorkItem() {
		this.workItemManager.completeWorkItem(workItemId, null);
	}
}
