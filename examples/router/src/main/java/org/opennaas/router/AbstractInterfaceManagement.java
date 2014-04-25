package org.opennaas.router;

import java.util.ArrayList;
import java.util.List;

import org.opennaas.api.router.IInterface;
import org.opennaas.api.router.IInterfaceManagement;

public abstract class AbstractInterfaceManagement implements IInterfaceManagement {

	private List<IInterface>	interfaces	= new ArrayList<IInterface>();

	@Override
	public void addInterface(IInterface interfaze) {
		interfaces.add(interfaze);
	}

	@Override
	public void removeInterface(IInterface interfaze) {
		interfaces.remove(interfaze);
	}

	@Override
	public List<IInterface> getInterfaces() {
		return interfaces;
	}

}
