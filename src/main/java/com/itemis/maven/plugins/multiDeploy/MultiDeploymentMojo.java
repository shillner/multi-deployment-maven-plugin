package com.itemis.maven.plugins.multiDeploy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.Deployer;
import org.eclipse.aether.repository.RemoteRepository;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.VERIFY)
public class MultiDeploymentMojo extends AbstractMojo {
  @Component
  private Deployer deployer;
  @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
  private RepositorySystemSession repoSession;
  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;
  @Parameter(defaultValue = "${settings}", readonly = true, required = true)
  private Settings settings;
  @Parameter(required = true)
  private Set<Repository> repositories;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    List<Artifact> projectArtifacts = getAllArtifacts();
    Set<RemoteRepository> remoteRepositories = isSnapshotVersion() ? setupSnapshotRepositories()
        : setupReleaseRepositories();
    Set<DeployRequest> deploymentRequests = remoteRepositories.stream().map(repo -> {
      DeployRequest request = new DeployRequest();
      request.setArtifacts(projectArtifacts);
      request.setRepository(repo);
      return request;
    }).collect(Collectors.toSet());

    for (DeployRequest request : deploymentRequests) {
      try {
        this.deployer.deploy(this.repoSession, request);
      } catch (DeploymentException e) {
        throw new MojoFailureException(e.getMessage(), e);
      }
    }
  }

  private boolean isSnapshotVersion() {
    return this.project.getVersion().endsWith("-SNAPSHOT");
  }

  private Set<RemoteRepository> setupSnapshotRepositories() {
    return this.repositories.stream().filter(repo -> repo.isSnapshotsEnabled()).map(repo -> toRemoteRepository(repo))
        .collect(Collectors.toSet());
  }

  private Set<RemoteRepository> setupReleaseRepositories() {
    return this.repositories.stream().filter(repo -> repo.isReleasesEnabled()).map(repo -> toRemoteRepository(repo))
        .collect(Collectors.toSet());
  }

  private RemoteRepository toRemoteRepository(Repository repo) {
    DefaultRepositoryLayout layout = new DefaultRepositoryLayout();
    ArtifactRepositoryPolicy snapshotsPolicy = new ArtifactRepositoryPolicy();
    ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy();

    ArtifactRepository artifactRepository = new MavenArtifactRepository(repo.getId(), repo.getUrl(), layout,
        snapshotsPolicy, releasesPolicy);
    this.settings.getServers().stream().filter(server -> Objects.equals(server.getId(), repo.getId())).findFirst()
        .ifPresent(server -> artifactRepository.setAuthentication(createServerAuthentication(server)));
    return RepositoryUtils.toRepo(artifactRepository);
  }

  private Authentication createServerAuthentication(Server server) {
    Authentication authentication = new Authentication(server.getUsername(), server.getPassword());
    authentication.setPrivateKey(server.getPrivateKey());
    authentication.setPassphrase(server.getPassphrase());
    return authentication;
  }

  public List<Artifact> getAllArtifacts() {
    List<Artifact> artifacts = new ArrayList<>();
    addPomArtifact(artifacts);
    addProjectArtifact(artifacts);
    addAttachedArtifacts(artifacts);
    return artifacts;
  }

  private void addPomArtifact(List<Artifact> artifacts) {
    DefaultArtifact a = new DefaultArtifact(this.project.getGroupId(), this.project.getArtifactId(), null, "pom",
        this.project.getVersion(), null, this.project.getFile());
    artifacts.add(a);
  }

  private void addProjectArtifact(List<Artifact> artifacts) {
    org.apache.maven.artifact.Artifact projectArtifact = this.project.getArtifact();
    if (projectArtifact.getFile() != null && projectArtifact.getFile().isFile()) {
      artifacts.add(RepositoryUtils.toArtifact(projectArtifact));
    }
  }

  private void addAttachedArtifacts(List<Artifact> artifacts) {
    this.project.getAttachedArtifacts().stream().map(a -> RepositoryUtils.toArtifact(a)).forEach(a -> artifacts.add(a));
  }
}
