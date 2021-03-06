package org.mqnaas.core.impl;

import org.mqnaas.core.api.IResource;

/**
 * 
 * @author Isart Canyameres Gimenez (i2cat)
 * 
 */
public class SampleCapability implements ISampleCapability {

	private int				counter;
	private final Object	lock	= new Object();

	@org.mqnaas.core.api.annotations.Resource
	private IResource		resource;

	@Override
	public void increment() {
		synchronized (lock) {
			counter++;
		}
	}

	@Override
	public void setCounter(int counterValue) {
		synchronized (lock) {
			counter = counterValue;
		}
	}

	@Override
	public int getCounter() {
		synchronized (lock) {
			return counter;
		}
	}

	public static boolean isSupporting(IResource resource) {
		return true;
	}

	public IResource getResource() {
		return resource;
	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

}
