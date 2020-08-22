package zmj.java.maven.inspect;

import hudson.maven.MavenEmbedderException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import zmj.java.maven.inspect.util.MavenProjectUtil;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ProjectBuildingException, MavenEmbedderException {
        String pomFile = "D:\\workspace\\idea\\test\\test-abc\\pom.xml";
        String localRepo = "D:\\workspace\\maven_repo";
        String m2Home = "C:\\development\\apache-maven-3.6.1";
        File mavenHome = new File(m2Home);

        MavenProject project = MavenProjectUtil.getMavenProject(pomFile, localRepo, null);
        List<MavenProject> projects = MavenProjectUtil.getAllSubProjects(project, localRepo);
        System.out.println(projects.size());

        for (MavenProject project1 : projects) {
            System.out.println("===========> begin");
            System.out.println(project1.getGroupId() + " <==> " + project1.getArtifactId() + " <==> " + project1.getVersion());
            System.out.println("is a pom maven project: " + MavenProjectUtil.isParent(project1));

            if (!MavenProjectUtil.isParent(project1)) {
                System.out.println("====> source " + project1.getBuild().getSourceDirectory());

                Build build = project1.getBuild();

                List<Dependency> dependencies = project1.getDependencies();
                for (Dependency dependency : dependencies) {
                    System.out.println("====> dependency");
                    System.out.println(dependency.getGroupId() + " <==> " + dependency.getArtifactId() + " <==> " + dependency.getVersion());
                }
            }
        }

        // PlexusContainer container = MavenEmbedderUtils.buildPlexusContainer(mavenHome, mavenRequest);
    }
}
