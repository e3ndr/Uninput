package xyz.e3ndr.uninput;

import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {

    public static void main(String[] args) throws Exception {
//        ConsoleUtil.summonConsoleWindow();

        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        FastLoggingFramework.setColorEnabled(false);

        Config config = new Config(); // TODO load from file.
        Uninput uninput = new Uninput(config);

        Thread.sleep(Long.MAX_VALUE);
    }

}
