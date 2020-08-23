/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zmj.java.maven.inspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import zmj.java.maven.inspect.constant.Constants;
import zmj.java.maven.inspect.exception.MavenProjectInspectException;
import zmj.java.maven.inspect.handler.JavaProjectOptionHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * entrance of this program
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        args = new String[] {"--maven-script", "D:\\workspace\\idea\\test\\test-abc\\pom.xml",
                "--maven-home", "C:\\development\\apache-maven-3.6.1"};
        Options options = getSupportedOptions();
        try {
            CommandLine commandLine = parseArg(args, options);

            // only need to print help message
            if (commandLine.hasOption(Constants.OPTION_HELP)) {
                printHelp(options);
                return;
            }

            Map<String, String> arguments = parseCommandLine(commandLine);

            JavaProjectOptionHandler.handle(arguments);
        } catch (ParseException e) {
            log.error("parse command error, error message: {}", e.getMessage());
            log.error("please check the arguments you set.");
            printHelp(options);
        } catch (MavenProjectInspectException e) {
            log.error("internal error: {}", e.getMessage());
            printHelp(options);
        }
    }

    private static Map<String, String> parseCommandLine(CommandLine commandLine) throws MavenProjectInspectException {
        Map<String, String> arguments = new HashMap<>();

        // maven pom.xml
        arguments.put(Constants.OPTION_MAVEN_SCRIPT, getMavenPomFile(commandLine));

        // maven home
        String mavenHome = getMavenHome(commandLine);
        arguments.put(Constants.OPTION_MAVEV_HOME, mavenHome);

        // maven user settings file
        // we will get maven local repository, remote repository from user settings file
        String userSettingFile = getUserSettingsFile(commandLine, mavenHome);
        arguments.put(Constants.OPTION_USER_SETTINGS, userSettingFile);

        return arguments;
    }

    private static String getUserSettingsFile(CommandLine commandLine, String mavenHome) throws MavenProjectInspectException {
        String userSettingFile = commandLine.getOptionValue("user-setting-file");
        if (userSettingFile != null) {
            File userSetFile = new File(userSettingFile);
            if (!userSetFile.exists() || userSetFile.isDirectory()) {
                userSettingFile = mavenHome + File.separator + "conf" + File.separator + "settings.xml";
            }
        } else {
            userSettingFile = mavenHome + File.separator + "conf" + File.separator + "settings.xml";
        }


        File userSetFile1 = new File(userSettingFile);
        if (!userSetFile1.exists()) {
            throw new MavenProjectInspectException("user settings file can't be find.");
        }

        return userSettingFile;
    }

    private static String getMavenHome(CommandLine commandLine) throws MavenProjectInspectException {
        String mavenHome = commandLine.getOptionValue(Constants.OPTION_MAVEV_HOME);

        if (mavenHome != null) {
            File mavenHomeFile = new File(mavenHome);
            if (!mavenHomeFile.exists() || mavenHomeFile.isFile()) {
                mavenHome = System.getenv(Constants.SYSTEM_PARAM_MAVEN_HOME);
            }
        } else {
            mavenHome = System.getenv(Constants.SYSTEM_PARAM_MAVEN_HOME);
        }

        if (mavenHome == null) {
            throw new MavenProjectInspectException("Maven home is not set");
        }

        return mavenHome;
    }

    private static String getMavenPomFile(CommandLine commandLine) throws MavenProjectInspectException {
        if (!commandLine.hasOption(Constants.OPTION_MAVEN_SCRIPT)) {
            throw new MavenProjectInspectException("no maven project build file pom.xml set, it's necessory");
        }

        String mavenScript = commandLine.getOptionValue(Constants.OPTION_MAVEN_SCRIPT);
        File mavenScriptFile = new File(mavenScript);
        if (mavenScript == null || !mavenScriptFile.exists() || mavenScriptFile.isDirectory() || !mavenScript.endsWith(".xml")) {
            throw new MavenProjectInspectException("maven pom.xml is not exist or set wrong!");
        }

        return mavenScript;
    }

    private static CommandLine parseArg(String[] args, Options options) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = parser.parse(options, args);
        return cmdLine;
    }

    /**
     * print help message
     *
     * @param options options support
     */
    private static void printHelp(Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(200);
        hf.printHelp(Constants.PROJECT_NAME, options, true);
    }

    /**
     * get all options that support
     *
     * @return all options that support
     */
    private static Options getSupportedOptions() {
        Options options = new Options();

        // print help message
        Option opt = new Option(Constants.OPTION_HELP_SHORT, Constants.OPTION_HELP, false, "Print help");
        opt.setRequired(false);
        options.addOption(opt);

        // if not set, we will find the System parameter M2_HOME
        // if we all can't find, will report error
        opt = new Option(Constants.OPTION_MAVEV_HOME_SHORT, Constants.OPTION_MAVEV_HOME, true, "Maven installation.");
        opt.setRequired(false);
        options.addOption(opt);

        // if not set, we will use ${M2_HOME}/config/settings.xml
        opt = new Option(Constants.OPTION_USER_SETTINGS_SHORT, Constants.OPTION_USER_SETTINGS, true, "user settings " +
                "file for maven");
        opt.setRequired(false);
        options.addOption(opt);

        // necessary, must set
        opt = new Option(Constants.OPTION_MAVEN_SCRIPT_SHORT, Constants.OPTION_MAVEN_SCRIPT, true, "maven " +
                "project build file: pom.xml");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }
}
