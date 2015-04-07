package org.ezand.telldus.cli.data;

public enum SensorProtocol {
	MANDOLYN;

	public static SensorProtocol fromName(final String name) {
		return name == null ? null : valueOf(name.toUpperCase());
	}
}