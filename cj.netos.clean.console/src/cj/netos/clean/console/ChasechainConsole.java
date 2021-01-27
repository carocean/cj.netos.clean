package cj.netos.clean.console;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.disk.INetDisk;
import cj.netos.clean.Console;
import cj.studio.ecm.annotation.CjService;

@CjService(name="chaseChainConsole")
public class ChasechainConsole extends Console {
	ICube cube;

	@Override
	protected String prefix(INetDisk disk, Object... target) {
		return disk.name()+">";
	}

	@Override
	protected boolean exit(String cmd) {
		if("bye".equals(cmd)){
			return true;
		}
		return false;
	}
}
