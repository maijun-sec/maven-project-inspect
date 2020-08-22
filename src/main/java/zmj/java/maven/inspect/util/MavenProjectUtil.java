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
package zmj.java.maven.inspect.util;

import hudson.maven.MavenEmbedder;
import hudson.maven.MavenEmbedderException;
import hudson.maven.MavenRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * operations about MavenProject
 *
 * @author zhang maijun
 * @since 2020/08/22
 */
@Slf4j
public class MavenProjectUtil {
    private static final String POM_FILE_NAME = "pom.xml";
    private static final String ADDITIONAL_SOURCE_PLUGIN_GROUPID = "org.codehaus.mojo";
    private static final String ADDITIONAL_SOURCE_PLUGIN_ARTIFACTID = "build-helper-maven-plugin";
    private static final String ADD_SOURCE_GOAL = "add-source";

    /**
     * check if a MavenProject is a parent module.
     *
     * @param project MavenProject
     * @return if a MavenProject is a parent module
     */
    public static boolean isParent(MavenProject project) {
        return "pom".equals(project.getPackaging());
    }

    /**
     * insert all maven modules to a list, contains the sub module of this project
     *
     * @param project   MavenProject
     * @param localRepo the local maven repository
     * @return List of all maven modules
     */
    public static List<MavenProject> getAllSubProjects(MavenProject project, String localRepo) {
        List<MavenProject> projects = new ArrayList<>();
        addAllSubProjectsToList(projects, project, localRepo);
        return projects;
    }

    private static void addAllSubProjectsToList(List<MavenProject> projects, MavenProject project, String localRepo) {
        projects.add(project);

        if (!isParent(project)) {
            return;
        }

        List<MavenProject> subProjects = getSubProjects(project, localRepo);
        if (CollectionUtils.isNotEmpty(subProjects)) {
            for (MavenProject subProject : subProjects) {
                addAllSubProjectsToList(projects, subProject, localRepo);
            }
        }
    }

    /**
     * get all sub modules of current MavenProject
     *
     * @param project   MavenProject
     * @param localRepo the local maven repository
     * @return all sub modules of current MavenProject
     */
    public static List<MavenProject> getSubProjects(MavenProject project, String localRepo) {
        List<MavenProject> projects = new ArrayList<>();

        File baseDir = project.getBasedir();
        List<String> modules = project.getModules();
        for (String module : modules) {
            File subDir = new File(baseDir, module);
            File subPom = new File(subDir, POM_FILE_NAME);
            if (subPom.exists() && subPom.isFile()) {
                try {
                    String pomPath = subPom.getCanonicalPath();
                    projects.add(getMavenProject(pomPath, localRepo, project));
                } catch (IOException e) {
                    log.warn("get pom file failed, we will skip this module, message: {}", e.getMessage());
                } catch (ProjectBuildingException | MavenEmbedderException e) {
                    log.warn("parse pom file failed, we will skip this module, message: {}", e.getMessage());
                }
            } else {
                log.warn("there is no pom.xml in module {}, we will skip this module", module);
            }
        }

        return projects;
    }

    /**
     * get MavenProject from pom file and local maven repository
     *
     * @param pomFile   pom file
     * @param localRepo the local maven repository
     * @param parent    parent module of current maven project(can be null)
     * @return MavenProject
     * @throws MavenEmbedderException
     * @throws ProjectBuildingException
     */
    public static MavenProject getMavenProject(String pomFile, String localRepo, MavenProject parent) throws MavenEmbedderException, ProjectBuildingException {
        MavenRequest mavenRequest = new MavenRequest();
        mavenRequest.setPom(pomFile);
        mavenRequest.setLocalRepositoryPath(localRepo);

        MavenEmbedder mavenEmbedder = new MavenEmbedder(Thread.currentThread().getContextClassLoader(), mavenRequest);

        MavenProject project = mavenEmbedder.readProject(new File(pomFile));

        if (parent != null) {
            project.setParent(parent);
        }

        return project;
    }

    /**
     * get all source paths of current module
     *
     * @param project maven module
     * @return all source paths
     */
    public static List<String> getSourcePaths(MavenProject project) {
        List<String> sourcePaths = new ArrayList<>();

        Build build = project.getBuild();

        // 1. get the basic source path
        sourcePaths.add(build.getSourceDirectory());

        // 2. add all additional source Paths.
        // for plugin: org.codehaus.mojo<groupId> / build-helper-maven-plugin<artifactId>
        List<Plugin> plugins = build.getPlugins();
        if (CollectionUtils.isNotEmpty(plugins)) {
            for (Plugin plugin : plugins) {
                if (ADDITIONAL_SOURCE_PLUGIN_GROUPID.equals(plugin.getGroupId())
                        && ADDITIONAL_SOURCE_PLUGIN_ARTIFACTID.equals(plugin.getArtifactId())) {
                    List<String> additonalSourcePaths = getAdditonalSourcePathsFromPlugin(plugin);
                    sourcePaths.addAll(additonalSourcePaths);
                }
            }
        }

        return sourcePaths;
    }

    @Deprecated
    private static void addAbsoluteSourcePaths(List<String> sourcePaths, File basePath, List<String> additonalSourcePaths) {
        if (CollectionUtils.isNotEmpty(additonalSourcePaths)) {
            for (String additionalSourcePath : additonalSourcePaths) {
                File file = new File(basePath, additionalSourcePath);
                try {
                    sourcePaths.add(file.getCanonicalPath());
                } catch (IOException e) {
                    log.warn("can't get absolute path for {}, error message: {}", file.getName(), e.getMessage());
                }
            }
        }
    }

    private static List<String> getAdditonalSourcePathsFromPlugin(Plugin plugin) {
        List<String> additionalSourcePaths = new ArrayList<>();

        List<PluginExecution> executions = plugin.getExecutions();
        if (CollectionUtils.isNotEmpty(executions)) {
            for (PluginExecution execution : executions) {
                if (CollectionUtils.isNotEmpty(execution.getGoals())
                        && execution.getGoals().contains(ADD_SOURCE_GOAL)) {
                    Object configuration = execution.getConfiguration();
                    additionalSourcePaths.addAll(getAdditonalSourcePathsFromConfiguration(configuration));
                }
            }
        }

        return additionalSourcePaths;
    }

    private static List<String> getAdditonalSourcePathsFromConfiguration(Object configuration) {
        if (configuration instanceof Xpp3Dom) {
            Xpp3Dom confNode = (Xpp3Dom) configuration;
            Xpp3Dom sourcesNode = confNode.getChild("sources");
            if (sourcesNode != null) {
                Xpp3Dom[] sourceNodes = sourcesNode.getChildren("source");
                if (sourceNodes != null) {
                    return Stream.of(sourceNodes).map(node -> node.getValue()).collect(Collectors.toList());
                }
            }
        }

        return new ArrayList<>();
    }
}
