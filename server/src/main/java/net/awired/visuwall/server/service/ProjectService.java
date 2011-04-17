package net.awired.visuwall.server.service;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import net.awired.visuwall.api.domain.Build;
import net.awired.visuwall.api.domain.Project;
import net.awired.visuwall.api.domain.ProjectId;
import net.awired.visuwall.api.domain.ProjectStatus.State;
import net.awired.visuwall.api.exception.BuildNotFoundException;
import net.awired.visuwall.api.exception.ProjectNotFoundException;
import net.awired.visuwall.api.plugin.BuildConnectionPlugin;
import net.awired.visuwall.api.plugin.QualityConnectionPlugin;
import net.awired.visuwall.server.domain.PluginHolder;
import net.awired.visuwall.server.domain.Wall;
import net.awired.visuwall.server.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    ProjectMergeService projectMergeService;

    private static final int PROJECT_NOT_BUILT_ID = -1;

    @Transient
    private String[] metrics = new String[]{"coverage", "ncloc", "violations_density"};


    //	public Collection<Project> getProjects(Wall wall) {
    //		List<Project> allProjects = new ArrayList<Project>();
    //		for (Project project : wall.getProjects()) {
    //			try {
    //				allProjects.add(findFreshProject(wall, project.getProjectId().getName()));
    //			} catch (ProjectNotFoundException e) {
    //				LOG.warn(e.getMessage());
    //			}
    //		}
    //		return allProjects;
    //	}

    public void updateWallProjects(Wall wall) {
        for(BuildConnectionPlugin buildService : wall.getPluginHolder().getBuildServices()) {
            List<ProjectId> discoveredProjects = buildService.findAllProjects();
            for(ProjectId discoveredProjectId : discoveredProjects) {

                Project project;
                try {
                    project = wall.getProjectFromProjectId(discoveredProjectId);

                } catch (NotFoundException e) {
                    project = new Project(discoveredProjectId);
                    wall.getProjects().add(project);
                }

                updateProject(wall.getPluginHolder(), project);
            }
        }
    }

    public void updateWallProject(Wall wall, String projectName) throws ProjectNotFoundException {
        Project project = wall.getProjectFromName(projectName);
        updateProject(wall.getPluginHolder(), project);
    }



    private void updateProject(PluginHolder pluginHolder, Project project) {
        for(BuildConnectionPlugin service : pluginHolder.getBuildServices()) {
            projectMergeService.merge(project, service);
        }
        for(QualityConnectionPlugin service : pluginHolder.getQualityServices()) {
            projectMergeService.merge(project, service, metrics);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(project.toString());
        }
    }

    //    public Project findFreshProject(Wall wall, String projectName) throws ProjectNotFoundException {
    //        Project project = wall.getProjectFromName(projectName);
    //        if (project.getProjectId() == null) {
    //            throw new ProjectNotFoundException("Project [name="+projectName+"] has not been found.");
    //        }
    //        Project freshProject = new Project();
    //        freshProject.setProjectId(project.getProjectId());
    //        for(BuildConnectionPlugin service : wall.getPluginHolder().getBuildServices()) {
    //        	projectMergeService.merge(freshProject, service);
    //        }
    //        for(QualityConnectionPlugin service : wall.getPluginHolder().getQualityServices()) {
    //        	projectMergeService.merge(freshProject, service, "coverage");
    //        }
    //
    //        if (LOG.isDebugEnabled()) {
    //            LOG.debug(project.toString());
    //        }
    //
    //        if (project.getName() == null) {
    //        	wall.getProjects().remove(project);
    //        	throw new ProjectNotFoundException("Project [projectId=" + project.getProjectId() + "] has no name.");
    //        }
    //
    //        return project;
    //    }

    /**
     * @return null if no date could be estimated
     * @throws ProjectNotFoundException
     */
    public Date getEstimatedFinishTime(Wall wall, String projectName) throws ProjectNotFoundException {
        ProjectId projectId = wall.getProjectFromName(projectName).getProjectId();
        for(BuildConnectionPlugin service : wall.getPluginHolder().getBuildServices()) {
            try {
                Date estimatedFinishTime = service.getEstimatedFinishTime(projectId);
                if (estimatedFinishTime != null) {
                    return estimatedFinishTime;
                }
            } catch(ProjectNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            }
        }
        return null;
    }

    public int getLastBuildNumber(PluginHolder pluginHolder, ProjectId projectId) {
        for (BuildConnectionPlugin service : pluginHolder.getBuildServices()) {
            try {
                return service.getLastBuildNumber(projectId);
            } catch (ProjectNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            } catch (BuildNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            }
        }
        return PROJECT_NOT_BUILT_ID;
    }

    public State getState(PluginHolder pluginHolder, ProjectId projectId) {
        for (BuildConnectionPlugin service : pluginHolder.getBuildServices()) {
            try {
                return service.getState(projectId);
            } catch (ProjectNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            }
        }
        throw new RuntimeException("Project must have a state.");
    }

    public boolean isBuilding(PluginHolder pluginHolder, ProjectId projectId) {
        for (BuildConnectionPlugin service : pluginHolder.getBuildServices()) {
            try {
                return service.isBuilding(projectId);
            } catch (ProjectNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            }
        }
        return false;
    }

    public Build findBuildByBuildNumber(Wall wall, String projectName, int buildNumber) throws BuildNotFoundException {
        ProjectId projectId = wall.getProjectFromName(projectName).getProjectId();
        for (BuildConnectionPlugin service : wall.getPluginHolder().getBuildServices()) {
            try {
                Build build = service.findBuildByBuildNumber(projectId, buildNumber);
                if (build != null) {
                    return build;
                }
            } catch (BuildNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            } catch (ProjectNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e.getMessage());
                }
            }
        }
        throw new BuildNotFoundException("No build #"+buildNumber+" for project "+projectId);
    }

}