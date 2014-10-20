package de.qabel.desktop;

import java.io.File;

import org.apache.commons.cli.*;

import de.qabel.core.module.ModuleManager;

public class QblMain {
	private static final String MODULE_OPT = "module";

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		QblMain main = new QblMain();
		main.parse(args);
		main.startModules();
	}

	private ModuleManager moduleManager;
	private CommandLine commandLine;

	private void startModules() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if(!commandLine.hasOption(MODULE_OPT)) {
			return;
		}
        for (String module : options.getOption(MODULE_OPT).getValues()) {
            startModule(module);
        }
	}

	private QblMain() {
		options.addOption(MODULE_OPT, true, "start a module at loadtime");
		
		moduleManager = new ModuleManager();
	}

	private boolean parse(String... args) {
		CommandLineParser parser = new GnuParser();
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(HelpFormatter.DEFAULT_ARG_NAME, options);
			return false;
		}
		
		return true;
	}

	private void startModule(String module) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String[] moduleParts = module.split(":", 2);
		moduleManager.startModule(new File(moduleParts[0]), moduleParts[1]);
	}

	private Options options = new Options();

}
