package cj.netos.clean.start;

import cj.studio.ecm.Assembly;
import cj.studio.ecm.IAssembly;
import cj.studio.ecm.adapter.IActuator;
import cj.studio.ecm.adapter.IAdaptable;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class Main {
	private static String fileName;

	// java -jar cjnet -h 127.0.0.1:10000 -t udt -debug cmdassembly.jar
	public static void main(String... args) throws IOException, ParseException {
		fileName = "cj.netos.clean.console";
		Options options = new Options();
		Option h = new Option("h", "host",true, "必须指定远程地址，格式：-h ip:port，port可以省去");
		options.addOption(h);
		Option  l = new Option("l","log", false, "充许网络日志输出到控制台");
		options.addOption(l);
		Option  m = new Option("m","man", false, "帮助");
		options.addOption(m);
		Option  u = new Option("u","user", true, "用户名");
		options.addOption(u);
		Option  p = new Option("p","pwd", true, "密码，如果密码前有!符，请将密码前后加引号'");
		options.addOption(p);
//		Option  db = new Option("db","database", true, "mongodb的库名，有权限访问的");
//		options.addOption(db);
		Option debug = new Option("d","debug", true, "调试命令行程序集时使用，需指定以下jar包所在目录\r\n"+fileName);
		options.addOption(debug);
		
		
//		GnuParser
//		BasicParser
//		PosixParser
		GnuParser parser = new GnuParser();
		CommandLine line = parser.parse(options, args);
		if(line.hasOption("m")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "disk", options );
			return;
		}
		//取属性的方式line.getOptionProperties("T").get("boss")
//		System.out.println(line.getOptionProperties("T").get("boss"));
		
		if(StringUtil.isEmpty(line.getOptionValue("h")))
			throw new ParseException("参数-h是host为必需，但为空");
		String usr = System.getProperty("user.dir");
		File f = new File(usr);
		File[] arr = f.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.startsWith(fileName)) {
					return true;
				}
				return false;
			}
		});
		if (arr.length < 1 && !line.hasOption("debug")){
			throw new IOException(fileName+" 程序集不存在.");
		}
		if (line.hasOption("debug")) {
			File[] da = new File(line.getOptionValue("debug")).listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.startsWith(fileName)) {
						return true;
					}
					return false;
				}
			});
			if(da.length<0)
				throw new IOException("调试时不存在指定的必要jar包"+fileName);
			f =da[0];
		} else {
			f = arr[0];
		}

		IAssembly assembly = Assembly.loadAssembly(f.toString());
		assembly.start();
		Object main = assembly.workbin().part("EntryPoint");
		IAdaptable a = (IAdaptable) main;
		IActuator act = a.getAdapter(IActuator.class);
		act.exeCommand("main", line);
	}
}
