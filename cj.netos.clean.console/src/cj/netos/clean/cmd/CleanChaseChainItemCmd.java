package cj.netos.clean.cmd;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.disk.INetDisk;
import cj.netos.clean.CmdLine;
import cj.netos.clean.Command;
import cj.netos.clean.Console;
import cj.netos.clean.model.ContentItem;
import cj.netos.clean.model.ItemPointer;
import cj.netos.clean.model.TrafficPool;
import cj.netos.clean.service.IPoolService;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceInvertInjection;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@CjService(name = "cleanChaseChainItemCmd")
public class CleanChaseChainItemCmd extends Command {
    @CjServiceInvertInjection
    @CjServiceRef(refByName = "chaseChainConsole")
    Console chaseChainConsole;
    @CjServiceRef(refByName = "poolService")
    IPoolService poolService;
    long skip = 0;
    long itemSkip = 0;

    @Override
    public String cmd() {
        return "cleanItem";
    }

    @Override
    public String cmdDesc() {
        return "清除追链内容";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option l = new Option("l", "limit", true, "页大小,默认10");
        options.addOption(l);
        Option t = new Option("t", "time", true, "格式：yyMMddhhmmss。清除该时间以前的内容，包括指向的感知器、管道内容");
        options.addOption(t);
        return options;
    }

    @Override
    public void doCommand(CmdLine cl) throws IOException, CircuitException {
        INetDisk disk = (INetDisk) cl.prop("disk");
        ICube cube = disk.home();
        CommandLine line = cl.line();
        String limit = line.getOptionValue("l");
        if (StringUtil.isEmpty(limit)) {
            limit = "10";
        }
        if (!line.hasOption("t")) {
            System.out.println("缺少参数t");
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyMMddHHmmss");
        long ctime = 0;
        try {
            String str = line.getOptionValue("t");
            Date date = format2.parse(str);
            ctime = date.getTime();
            System.out.println(String.format("清除到时间:%s", format.format(ctime)));
        } catch (ParseException e) {
            System.out.println("解析时间出错");
            return;
        }
        skip = 0;
        while (true) {
            List<TrafficPool> pools = poolService.pagePool(cube, Integer.valueOf(limit), skip);
            if (pools.isEmpty()) {
                break;
            }
            skip += pools.size();
            for (TrafficPool pool : pools) {
                String pctime = format.format(new Date(pool.getCtime()));
                System.out.println(String.format("%s %s %s", pool.getId(), pool.getTitle(), pctime));
                String cubeName = String.format("chasechain.traffic.pools.%s", pool.getId());
                ICube poolCube = disk.cube(cubeName);
                itemSkip = 0;
                while (true) {
                    List<ContentItem> items = poolService.pageContentItem(poolCube, ctime, limit, itemSkip);
                    if (items.isEmpty()) {
                        break;
                    }
                    itemSkip += items.size();
                    for (ContentItem item : items) {
                        ItemPointer pointer = item.getPointer();
                        String ictime = format.format(new Date(pointer.getCtime()));
                        System.out.println(String.format("\t%s %s %s %s", pointer.getType(), pointer.getCreator(), pointer.getId(), ictime));
                        if ("geo.receptor.docs".equals(pointer.getType())) {
                            poolService.cleanReceptorItem(disk.home(), pointer);
                        } else {
                            ICube channelCube = disk.cube(pointer.getCreator());
                            poolService.cleanChannelItem(channelCube, pointer);
                        }
                        poolService.removeContentItem(poolCube, item);
                    }
                }
            }
        }
    }

}
