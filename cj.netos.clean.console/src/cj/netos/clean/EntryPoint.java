package cj.netos.clean;

import cj.lns.chip.sos.disk.INetDisk;
import cj.lns.chip.sos.disk.NetDisk;
import cj.netos.clean.console.ChasechainConsole;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.logging.ILogging;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CjService(name = "EntryPoint", isExoteric = true)
public class EntryPoint {
	ILogging logger;
	@CjServiceRef(refByName="chaseChainConsole")
	ChasechainConsole console;
	public void main(CommandLine line) throws IOException {
		logger= CJSystem.logging();
		String conStr = line.getOptionValue("h");

		List<ServerAddress> seeds = new ArrayList<>();
		if (conStr.contains(":")) {
			String[] arr=conStr.split(":");
			seeds.add(new ServerAddress(arr[0],Integer.valueOf(arr[1])));
		} else {
			seeds.add(new ServerAddress(conStr));
		}
		List<MongoCredential> credential = new ArrayList<>();
		if(line.hasOption("u")&&line.hasOption("p")/*&&line.hasOption("db")*/){
			MongoCredential m = MongoCredential.createCredential(
					line.getOptionValue("u"), "admin"/*line.getOptionValue("db")*/, line.getOptionValue("p").toCharArray());
			credential.add(m);
		}
		MongoClientOptions options = MongoClientOptions.builder().socketKeepAlive(true).heartbeatSocketTimeout(5000).heartbeatConnectTimeout(5000).build();
		MongoClient client = new MongoClient(seeds, credential, options);
		INetDisk disk=NetDisk.open(client,"netos","superadmin","123456");
		logger.info(getClass(),"连接远程服务器成功。");
		console.monitor(disk);
		System.out.println("正在退出...");
		if (client != null) {
			client.close();
		}
		try {//如果3秒后还没退出，则强制
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			
		}finally{
			System.exit(0);
		}
	}

}
