package org.mqnaas.test.helpers.capability;

import org.mqnaas.core.api.ICapability;
import org.mqnaas.core.api.ICoreModelCapability;
import org.mqnaas.core.api.IRootResource;

/**
 * {@link ICapability}'s factory that provides test implementations useful for tests.
 * 
 * @author Julio Carlos Barrera
 *
 */
public class TestCapabilitiesFactory {

	/**
	 * @see ArtificialBundleGuard
	 * 
	 */
	public static ArtificialBundleGuard createArtificialBundleGuard() {
		return new ArtificialBundleGuard();
	}

	/**
	 * @see ArtificialCoreModelCapability
	 * 
	 */
	public static ICoreModelCapability createArtificialCoreModelCapability(final IRootResource resourceToBeReturned) {
		return new ArtificialCoreModelCapability(resourceToBeReturned);
	}
}
