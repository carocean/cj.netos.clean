package cj.netos.clean.cmd;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.disk.INetDisk;
import cj.netos.clean.CmdLine;
import cj.netos.clean.Command;
import cj.netos.clean.Console;
import cj.netos.clean.model.TrafficPool;
import cj.netos.clean.service.IPoolService;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceInvertInjection;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import com.mongodb.MongoClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CjService(name = "chaseChainItemCmd")
public class ChaseChainItemCmd extends Command {
    @CjServiceInvertInjection
    @CjServiceRef(refByName = "chaseChainConsole")
    Console chaseChainConsole;
    @CjServiceRef(refByName = "poolService")
    IPoolService poolService;

    @Override
    public String cmd() {
        return "ls";
    }

    @Override
    public String cmdDesc() {
        return "列表流量池";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option t = new Option("l", "limit", true, "页大小");
        options.addOption(t);
        Option o = new Option("s", "skip", true, "页码");
        options.addOption(o);
        return options;
    }

    @Override
    public void doCommand(CmdLine cl) throws IOException, CircuitException {
        INetDisk disk = (INetDisk) cl.prop("disk");
        ICube cube = disk.home();
        CommandLine line = cl.line();
        String limit = line.getOptionValue("l");
        String skip = line.getOptionValue("s");
        if (StringUtil.isEmpty(limit)) {
            limit = "10";
        }
        if (StringUtil.isEmpty(skip)) {
            skip = "0";
        }
        List<TrafficPool> pools=poolService.pagePool(cube, Integer.valueOf(limit), Long.valueOf(skip));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        for (TrafficPool pool : pools) {
            String ctime=format.format(new Date(pool.getCtime()));
            System.out.println(String.format("%s %s %s",pool.getId(),pool.getTitle(),ctime));
        }
    }
}
