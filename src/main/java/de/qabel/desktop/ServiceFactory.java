package de.qabel.desktop;

public interface ServiceFactory {
	/**
	 * @deprecated only used for magic DI and tests
	 */
	@Deprecated
	Object get(String key);
	/**
	 * @deprecated only used for magic DI and tests
	 */
	@Deprecated
	Object getByType(Class type);
	/**
	 * @deprecated only used for magic DI and tests
	 */
	@Deprecated
	void put(String key, Object instance);
}
