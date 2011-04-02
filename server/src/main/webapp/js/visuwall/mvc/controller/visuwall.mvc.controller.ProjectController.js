visuwall.mvc.controller.ProjectController = {
		
	projectsView : null,
	
	projectService : null,
	wallService : null,

	projectDAO : null,
	
	wallName : 'orange-vallee',
	
	init : function() {
		this.projectsView = visuwall.mvc.view.Projects;
		this.projectService = visuwall.business.service.Project;
		this.wallService = visuwall.business.service.Wall;
		this.projectDAO = visuwall.persistence.dao.ProjectDAO;
	},
	
	buildProjects : function() {
		var $this = this;
		this.projectService.projects(this.wallName, function(projects) {
			for (var i = 0; i < projects.length; i++) {
				$this.addProject(projects[i]);
			}

//			for (var i = 0; i < projects.length; i++) {
//				$this.addProject(projects[i]);
//			}			
		
		//	updateUTDiff
			
		});

		// TODO remove
		$('ul#projectsTable li:last-child').live('click', function() {
		    $(this).prependTo('ul#projectsTable');
		});
	},
	
	addProject : function(projectData) {
		this.projectDAO.saveProject(projectData);
		var project = this.projectDAO.getProject(projectData.name);
		
		this.projectsView.addProject(project.name, project.description);
		this._updateProject(project);
	},

	updateStatus : function() {
		var $this = this;
		$this.wallService.status(this.wallName,function (projectsStatus) {
			var projectDone = [];
			for (var i = 0; i < projectsStatus.length; i++) {
				var status = projectsStatus[i];

				if ($this.projectDAO.isProject(status.name)) {
					// this is a new project
					$this.projectService.project($this.wallName, status.name, function(newProjectData) {
						$this.addProject(newProjectData);
					});
					continue;
				}
				var project = $this.projectDAO.getProject(status.name);
				LOG.debug('Update status for project ' + status.name);
				$this._updateBuilding(project, status.building);
				$this._checkVersionChange(project, status);

				projectDone.push(status.name);
			}
			
			// looking for project to delete
			for (var key in $this.projectDAO.getProjects()) {
				if (!projectDone.contains(key)) {
					$this._removeProject(key);
				}
			}
		});
	},
	
	_updateProject : function(project) {
		this.projectDAO.saveProject(project);

		this._updateLastBuild(project);
		this._updateAgo(project);
		
		// call updateBuilding like we just receive the status
		var wasBuilding = project.building;
		project.building = false;
		this._updateBuilding(project, wasBuilding);
		this._updateState(project);
	},

	/////////////////////////////////////////////////////////////////////////
	
	
	_updateLastBuild : function(project) {
		if (project.completedBuild == null) {
			return
		}
		this.projectsView.updateBuildTime(project.name, project.completedBuild.duration);
		this.projectsView.updateCommiters(project.name, project.completedBuild.commiters);
		this.projectsView.updateQuality(project.name, project.qualityResult.measures);
		this.projectsView.updateUTCoverage(project.name, this._getCoverageFromQualityMeasures(project.qualityResult.measures));
		this.projectsView.updateUT(project.name, 
				project.completedBuild.testResult.failCount,
				project.completedBuild.testResult.passCount,
				project.completedBuild.testResult.skipCount,
				project.coverage);

		this.projectsView.updateITCoverage(project.name, 0);
		this.projectsView.updateIT(project.name, 0,0,0);
	
		var $this = this;
		var completedBuild = project.completedBuild;
		
		this.projectDAO.callbackPreviousCompletedBuild(project.name, function(previousBuild) {
			if (completedBuild == null || previousBuild == null) {
				return;
			}
			
			var failDiff = completedBuild.testResult.failCount - previousBuild.testResult.failCount;
			var successDiff = completedBuild.testResult.totalCount - previousBuild.testResult.totalCount;
			var skipDiff = completedBuild.testResult.skipCount - previousBuild.testResult.skipCount;
			
			$this.projectsView.updateUTDiff(project.name, failDiff, successDiff, skipDiff);
		});
	},
	
	_updateAgo : function(project) {
		if (project.completedBuild != null) {
			this.projectsView.updateAgo(project.name, new Date(project.completedBuild.startTime + project.completedBuild.duration));					
		} else {
			this.projectsView.updateAgo(project.name, 0);
		}
	},
	
	_getCoverageFromQualityMeasures : function(measures) {
		for (var i = 0; i < measures.length; i++) {
			if (measures[i].key == 'coverage') {
				return measures[i].value.value;
			}
		}
		return;
	},

	_removeProject : function(projectName) {
		this.projectDAO.removeProject(projectName);
		this.projectsView.removeProject(projectName);
	},
	
	_updateState : function(project) {
		switch(project.state) {
		case 'SUCCESS':
			this.projectsView.displaySuccess(project.name);
			break;
		case 'NEW':
			this.projectsView.displayNew(project.name);
			break;
		case 'ABORTED':
			this.projectsView.displayAborted(project.name);
			break;
		case 'FAILURE':
			this.projectsView.displayFailure(project.name);			
			break;
		case 'UNSTABLE':
			this.projectsView.displayUnstable(project.name);
			break;
		default:
			LOG.error('Unknown project state : ' + project.state);
		}
	},
	
	_updateBuilding : function(project, isBuilding) {
		if (isBuilding) {
			if (!project.building) {
				var $this = this;
				visuwall.business.service.Processing.finishTime(this.wallName, project.name, function(data) {
					$this.projectsView.updateCountdown(project.name, new Date(data));
				});
				LOG.info("project is now building : " + project.name);
				this.projectsView.showBuilding(project.name);
				project.building = true;
			}
		} else if (project.building) {
			LOG.info("building is now over for project : " + project.name);
			this.projectsView.stopBuilding(project.name);
			project.building = false;

			var $this = this;
			this.projectService.project(this.wallName, project.name, function(newProjectData) {
				$this._updateProject(newProjectData);
			});			
		} else {
			this.projectsView.stopBuilding(project.name);
		}
	},
	
	_checkVersionChange : function(project, status) {
		if (this._checkVersionChangeAndNotBuilding(project, status)) {
			LOG.info("Server is not building and version has change, we need an update");
			var $this = this;
			this.projectService.project(this.wallName, project.name, function(newProjectData) {
				$this._updateProject(newProjectData);
			});
		}
	},	
	
	_checkVersionChangeAndNotBuilding : function(projectData, projectStatus) {
		// if ever build && not building && last build on server != last completed in js 
		if (projectStatus.lastBuildId != -1 && !projectStatus.building
				&& projectStatus.lastBuildId != projectData.completedBuild.buildNumber) {
			return true;
		}
		return false;
	}
	
};

$(function (){
	visuwall.mvc.controller.ProjectController.init();
});