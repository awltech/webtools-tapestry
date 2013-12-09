package net.atos.webtools.tapestry.core.listeners.internal;

import java.util.ArrayList;
/*
 * CLiC, Framework for Command Line Interpretation in Eclipse
 *
 *     Copyright (C) 2013 Worldline or third-party contributors as
 *     indicated by the @author tags or express copyright attribution
 *     statements applied by the authors.
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import net.atos.webtools.tapestry.core.TapestryCore;

/**
 * 
 * @author mvanbesien/ahavez
 * 
 */
public class ListenerRegistry {

	/**
	 * Static internal class, in charge of holding the Singleton instance.
	 * 
	 * @generated Singleton Generator on 2013-12-03 17:34:14 CET
	 */
	private static class SingletonHolder {
		static ListenerRegistry instance = new ListenerRegistry();
	}

	/**
	 * Returns the Singleton instance of this class.
	 * 
	 * @generated Singleton Generator on 2013-12-03 17:34:14 CET
	 */
	public static ListenerRegistry getInstance() {
		return SingletonHolder.instance;
	}

	/**
	 * Default constructor. Generated because used in singleton instanciation &
	 * needs to be private Implementation enriched to load the extension point
	 * 
	 * @generated Singleton Generator on 2013-12-03 17:34:14 CET
	 */
	private ListenerRegistry() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(TapestryCore.PLUGIN_ID,
				"listeners");
		for (IConfigurationElement element : extensionPoint.getConfigurationElements()) {
			if ("listener".equals(element.getName())) {
				try {
					Object createdExecutableExtension = element.createExecutableExtension("implementation");
					if (createdExecutableExtension instanceof IListener<?>) {
						this.registeredListeners.add((IListener<?>) createdExecutableExtension);
					} else {
						// TODO Put an error message.
					}
				} catch (Exception e) {
					// TODO Put an error message
				}
			}
		}
	}

	/**
	 * List of all the listeners registered through extension point.
	 */
	private Collection<IListener<?>> registeredListeners = new ArrayList<IListener<?>>();

	/**
	 * Returns all the listeners registered for CLiC
	 * 
	 * @return
	 */
	public Collection<IListener<?>> getAllListeners() {
		return Collections.unmodifiableCollection(this.registeredListeners);
	}

	/**
	 * Returns all the listeners of a specific type, registered for CLiC
	 * 
	 * @param clazz : Listener type.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends IListener<?>> Collection<T> getListenersFor(Class<T> clazz) {
		Collection<T> validListeners = new ArrayList<T>();
		for (IListener<?> listener : this.registeredListeners) {
			if (clazz.isAssignableFrom(listener.getClass())) {
				validListeners.add((T) listener);
			}
		}
		return validListeners;
	}

}
