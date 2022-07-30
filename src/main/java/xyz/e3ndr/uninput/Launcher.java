package xyz.e3ndr.uninput;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import co.casterlabs.rakurai.json.Rson;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class Launcher {
    private static final Path CONFIG_PATH = new File("config.json").toPath();

    public static void main(String[] args) throws Exception {
//        ConsoleUtil.summonConsoleWindow();

        FastLoggingFramework.setDefaultLevel(LogLevel.DEBUG);
        FastLoggingFramework.setColorEnabled(false);

        Config config;

        // Load the config or supply our own.
        if (CONFIG_PATH.toFile().exists()) {
            config = Rson.DEFAULT.fromJson(
                Files.readString(CONFIG_PATH),
                Config.class
            );
        } else {
            config = new Config();
            CONFIG_PATH.toFile().createNewFile();
        }

        Files.writeString(
            CONFIG_PATH,
            Rson.DEFAULT
                .toJson(config)
                .toString(true)
        );

        Uninput uninput = new Uninput(config);

        Thread.sleep(Long.MAX_VALUE);
    }

}
