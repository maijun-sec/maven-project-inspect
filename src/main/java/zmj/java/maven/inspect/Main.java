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

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.resolution.ArtifactDescriptorException;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import zmj.java.maven.inspect.bean.RemoteRepositoryMessageBean;
import zmj.java.maven.inspect.util.DependencyUtil;
import zmj.java.maven.inspect.util.MavenProjectUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * entrance of this program
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
@Slf4j
public class Main {
    public static void main(String[] args) throws MavenEmbedderException, ProjectBuildingException,
            ArtifactDescriptorException {
        Options options = getSupportedOptions();
        try {
            CommandLine commandLine = parseArg(args, options);

            // only need to print help message
            if (commandLine.hasOption("help") || commandLine.hasOption("h")) {
                printHelp(options);
                return;
            }

            Map<String, String> arguments = parseCommandLine(commandLine);
        } catch (ParseException e) {
            log.error("parse command error, error message: {}", e.getMessage());
            log.error("please check the arguments you set.");
            printHelp(options);
        } catch (MavenProjectInspectException e) {
            e.printStackTrace();
        }


        String pomFile = "D:\\workspace\\idea\\test\\test-abc\\pom.xml";
        String localRepo = "D:\\workspace\\maven_repo";
        File mavenHome = new File("C:\\development\\apache-maven-3.6.1");
        System.out.println(System.getenv("M2_HOME"));

        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(pomFile);
        mavenRequest.setLocalRepositoryPath(localRepo);

        MavenEmbedder mavenEmbedder = new MavenEmbedder(mavenHome, mavenRequest);
        List<ProjectBuildingResult> projectBuildingResults = mavenEmbedder.buildProjects(new File(pomFile), true);

        System.out.println(projectBuildingResults.size());
        for (ProjectBuildingResult result : projectBuildingResults) {
            System.out.println("==========================> begin");
            MavenProject project = result.getProject();
            System.out.println(project.getGroupId());
            System.out.println(project.getArtifactId());
            System.out.println(project.getVersion());
            System.out.println(MavenProjectUtil.isParent(project));
            System.out.println(result.getProjectId());

            if (!MavenProjectUtil.isParent(project)) {
                List<Dependency> dependencies = project.getDependencies();
                for (Dependency dependency : dependencies) {
                    String projectId =
                            dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
                    System.out.println("====> dependency: " + projectId);
                    RemoteRepositoryMessageBean remoteRepositoryMessageBean = new RemoteRepositoryMessageBean(
                            "alimaven", "default", "http://maven.aliyun.com/nexus/content/groups/public/");
                    ArtifactDescriptorResult artifactDescriptorResult =
                            DependencyUtil.getDependencies(remoteRepositoryMessageBean, projectId, localRepo);
                    for (org.eclipse.aether.graph.Dependency dependency1 : artifactDescriptorResult.getDependencies()) {
                        System.out.println(dependency1);
                    }
                }
            }
        }
    }

    private static Map<String, String> parseCommandLine(CommandLine commandLine) throws MavenProjectInspectException {
        Map<String, String> arguments = new HashMap<>();

        // maven pom.xml
        arguments.put("maven-script", getMavenPomFile(commandLine));

        // maven home
        String mavenHome = getMavenHome(commandLine);
        arguments.put("maven-home", mavenHome);

        // maven user setting file
        String userSettingFile = getUserSettingsFile(commandLine, mavenHome);
        arguments.put("user-setting-file", userSettingFile);

        return arguments;
    }

    private static String getUserSettingsFile(CommandLine commandLine, String mavenHome) throws MavenProjectInspectException {
        String userSettingFile = commandLine.getOptionValue("user-setting-file");
        File userSetFile = new File(userSettingFile);
        if (userSettingFile == null || !userSetFile.exists() || userSetFile.isDirectory()) {
            userSettingFile = mavenHome + File.separator + "conf" + File.separator + "settings.xml";
        }

        File userSetFile1 = new File(userSettingFile);
        if (!userSetFile1.exists()) {
            throw new MavenProjectInspectException("user settings file can't be find.");
        }

        return userSettingFile;
    }

    private static String getMavenHome(CommandLine commandLine) throws MavenProjectInspectException {
        String mavenHome = commandLine.getOptionValue("maven-home");
        File mavenHomeFile = new File(mavenHome);
        if (mavenHome == null || !mavenHomeFile.exists() || mavenHomeFile.isFile()) {
            mavenHome = System.getenv("M2_HOME");
        }

        if (mavenHome == null) {
            throw new MavenProjectInspectException("Maven home is not set");
        }

        return mavenHome;
    }

    private static String getMavenPomFile(CommandLine commandLine) throws MavenProjectInspectException {
        if (!commandLine.hasOption("maven-script")) {
            throw new MavenProjectInspectException("no maven project build file pom.xml set, it's necessory");
        }

        String mavenScript = commandLine.getOptionValue("maven-script");
        File mavenScriptFile = new File(mavenScript);
        if (mavenScript == null || !mavenScriptFile.exists() || mavenScriptFile.isDirectory() || !mavenScript.endsWith(".pom")) {
            throw new MavenProjectInspectException("maven pom.xml is not exist or set wrong!");
        }

        return mavenScript;
    }

    private static CommandLine parseArg(String[] args, Options options) throws ParseException {
        CommandLineParser paraer = new DefaultParser();
        CommandLine cmdLine = paraer.parse(options, args);
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
        hf.printHelp("maven-project-inspect", options, true);
    }

    /**
     * get all options that support
     *
     * @return all options that support
     */
    private static Options getSupportedOptions() {
        Options options = new Options();

        // print help message
        Option opt = new Option("h", "help", false, "Print help");
        opt.setRequired(false);
        options.addOption(opt);

        // if not set, we will find the System parameter M2_HOME
        // if we all can't find, will report error
        opt = new Option("mh", "maven-home", true, "Maven installation.");
        opt.setRequired(false);
        options.addOption(opt);

        // if not set, we will use ${M2_HOME}/config/settings.xml
        opt = new Option("usf", "user-setting-file", true, "user settings file for maven");
        opt.setRequired(false);
        options.addOption(opt);

        // if not set, we will get this message with the following order:
        // 1. user setting file specified;
        // 2. configuration in ${M2_HOME}/config/settings.xml
        opt = new Option("mlr", "maven-local-repo", true, "Maven local repository");
        opt.setRequired(false);
        options.addOption(opt);

        // necessary, must set
        opt = new Option("ms", "maven-script", true, "maven project build file: pom.xml");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }
}
