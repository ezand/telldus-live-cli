package org.ezand.telldus.cli.repository;

import static java.lang.String.valueOf;
import static org.ezand.telldus.cli.utils.CliResultParser.parseDevices;
import static org.ezand.telldus.cli.utils.CliResultParser.parseDimResult;
import static org.ezand.telldus.cli.utils.CliResultParser.parseSensors;
import static org.ezand.telldus.cli.utils.CliResultParser.parseSwitchResult;

import java.util.List;
import java.util.Optional;

import org.ezand.telldus.cli.data.Device;
import org.ezand.telldus.cli.data.LastSentCommand;
import org.ezand.telldus.cli.data.Sensor;
import org.ezand.telldus.cli.utils.CommandExecutor;
import org.ezand.telldus.cli.utils.TelldusException;

public class CliRepository implements TelldusRepository {
	private final String tdtool;

	public CliRepository(final String tdtool) {
		this.tdtool = tdtool;
	}

	@Override
	public List<Device> getDevices() {
		return parseDevices(CommandExecutor.execute(tdtool, "--list-devices"));
	}

	@Override
	public List<Sensor> getSensors() throws TelldusException {
		return parseSensors(CommandExecutor.execute(tdtool, "--list-sensors"));
	}

	@Override
	public String getDeviceState(final int id) {
		final Optional<Device> optional = getDevices().stream().filter(d -> d.getId() == id).findFirst();
		final Device device = optional.orElseThrow(() -> new TelldusException("State unknown"));
		switch (device.getLastSentCommand()) {
			case ON:
				return LastSentCommand.ON.name();
			case OFF:
				return LastSentCommand.OFF.name();
			case DIMMED:
				return LastSentCommand.DIMMED.name() + " dim_level=" + device.getProperties().get("dimlevel");
			default:
				return "Unknown state";
		}
	}

	@Override
	public boolean turnDeviceOn(final int deviceId) {
		return parseSwitchResult(CommandExecutor.execute(tdtool, "--on", valueOf(deviceId)));
	}

	@Override
	public boolean turnDeviceOff(final int deviceId) {
		return parseSwitchResult(CommandExecutor.execute(tdtool, "--off", valueOf(deviceId)));
	}

	@Override
	public int dimDevice(final int deviceId, final int level) throws TelldusException {
		return parseDimResult(CommandExecutor.execute(tdtool, "--dimlevel", valueOf(level), "--dim", valueOf(deviceId)));
	}
}