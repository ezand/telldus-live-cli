package org.ezand.telldus.cli.repository;

import static java.lang.String.valueOf;
import static org.ezand.telldus.cli.data.Type.DIMMER;
import static org.ezand.telldus.cli.data.Type.SWITCH;
import static org.ezand.telldus.cli.data.Type.UNKNOWN;
import static org.ezand.telldus.cli.utils.CliResultParser.parseDevices;
import static org.ezand.telldus.cli.utils.CliResultParser.parseDimResult;
import static org.ezand.telldus.cli.utils.CliResultParser.parseSensors;
import static org.ezand.telldus.cli.utils.CliResultParser.parseSwitchResult;

import java.util.List;
import java.util.Optional;

import org.ezand.telldus.cli.data.Device;
import org.ezand.telldus.cli.data.Sensor;
import org.ezand.telldus.cli.data.State;
import org.ezand.telldus.cli.data.SwitchState;
import org.ezand.telldus.cli.utils.CommandExecutor;
import org.ezand.telldus.cli.utils.TelldusException;

public class CliRepository implements TelldusRepository {
	private final String tdtool;

	public CliRepository(final String tdtool) {
		this.tdtool = tdtool;
	}

	/**
	 * @return a list of {@link Device} objects.
	 */
	@Override
	public List<Device> getDevices() {
		return parseDevices(CommandExecutor.execute(tdtool, "--list-devices"));
	}

	/**
	 * @return a list of {@link Sensor} objects.
	 * @throws TelldusException if tdtool-command fails.
	 */
	@Override
	public List<Sensor> getSensors() throws TelldusException {
		return parseSensors(CommandExecutor.execute(tdtool, "--list-sensors"));
	}

	/**
	 * @param id the device id.
	 * @return a {@link State} object containing the device type and state.
	 * @throws TelldusException if tdtool-command fails.
	 */
	@Override
	public State getDeviceState(final int id) throws TelldusException {
		final Optional<Device> optional = getDevices().stream().filter(d -> d.getId() == id).findFirst();
		final Device device = optional.orElseThrow(() -> new TelldusException("State unknown"));
		switch (device.getLastSentCommand()) {
			case ON:
				return new State(SWITCH, SwitchState.ON.lowerName());
			case OFF:
				return new State(SWITCH, SwitchState.OFF.lowerName());
			case DIMMED:
				return new State(DIMMER, device.getProperties().get("dimlevel"));
			default:
				return new State(UNKNOWN, "Unknown state");
		}
	}

	/**
	 * @param id the device id.
	 * @return true if device was successfully switched on, false otherwise.
	 * @throws TelldusException if tdtool-command fails.
	 */
	@Override
	public State turnDeviceOn(final int id) throws TelldusException {
		final boolean success = parseSwitchResult(CommandExecutor.execute(tdtool, "--on", valueOf(id)));
		return new State(SWITCH, success ? SwitchState.ON.lowerName() : SwitchState.OFF.lowerName());
	}

	/**
	 * @param id the device id.
	 * @return true if device was successfully switched off, false otherwise.
	 * @throws TelldusException if tdtool-command fails.
	 */
	@Override
	public State turnDeviceOff(final int id) {
		final boolean success = parseSwitchResult(CommandExecutor.execute(tdtool, "--off", valueOf(id)));
		return new State(SWITCH, success ? SwitchState.OFF.lowerName() : SwitchState.ON.lowerName());
	}

	/**
	 * @param id    the device id.
	 * @param level the dim-level value (0-255).
	 * @return the curretn dim-level value (0-255) after the operation is finished.
	 * @throws TelldusException if tdtool-command fails.
	 */
	@Override
	public State dimDevice(final int id, final int level) throws TelldusException {
		return new State(DIMMER, valueOf(parseDimResult(CommandExecutor.execute(tdtool, "--dimlevel", valueOf(level), "--dim", valueOf(id)))));
	}
}
