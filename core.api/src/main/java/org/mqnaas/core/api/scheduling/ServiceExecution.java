package org.mqnaas.core.api.scheduling;

import java.util.Arrays;

import org.mqnaas.core.api.IService;

/**
 * <p>
 * The <code>ServiceExecution</code> is the representation of a task to be scheduled by the {@link IServiceExecutionScheduler}
 * </p>
 * <p>
 * It consists of the {@link IService} to be executed, the parameters for this service execution, and a {@link Trigger}. The <code>trigger</code>
 * defines when this <code>IService</code> will be executed.
 * </p>
 * 
 * @author Adrián Roselló Rey (i2CAT)
 *
 */
public class ServiceExecution {

	private IService	service;
	private Object[]	parameters;
	private Trigger		trigger;

	public ServiceExecution(IService service, Trigger trigger) {
		if (service == null || trigger == null)
			throw new IllegalArgumentException("Service and trigger should not be null");

		this.service = service;
		this.trigger = trigger;
	}

	public IService getService() {
		return service;
	}

	public void setService(IService service) {
		if (service == null)
			throw new IllegalArgumentException("Service should not be null");

		this.service = service;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		if (trigger == null)
			throw new IllegalArgumentException("Trigger should not be null");
		this.trigger = trigger;
	}

	@Override
	public String toString() {
		return "ServiceExecution [service=" + service + ", parameters=" + Arrays.toString(parameters) + ", trigger=" + trigger + "]";
	}

}
