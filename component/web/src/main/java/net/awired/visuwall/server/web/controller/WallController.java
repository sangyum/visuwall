/**
 * Copyright (C) 2010 Julien SMADJA <julien dot smadja at gmail dot com> - Arnaud LEMAIRE <alemaire at norad dot fr>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.awired.visuwall.server.web.controller;

import java.util.List;
import java.util.Set;

import net.awired.visuwall.api.domain.ProjectStatus;
import net.awired.visuwall.core.domain.Wall;
import net.awired.visuwall.core.exception.NotFoundException;
import net.awired.visuwall.core.service.PluginService;
import net.awired.visuwall.core.service.WallHolderService;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/wall")
public class WallController {

	private static final String WALL_JSP = "wall/wallForm";

	@Autowired
	private WallHolderService wallService;

	@Autowired
	private PluginService pluginService;

	@RequestMapping
	public String getWallNames(ModelMap modelMap) {
		Set<String> wallNames = wallService.getWallNames();
		modelMap.put("data", wallNames);
		return "wall/wallList";
	}

	@RequestMapping("{wallName}")
	public String getProjects(@PathVariable String wallName, ModelMap modelMap)
			throws NotFoundException {
		Wall wall = wallService.find(wallName);
		modelMap.put("data", wall);
		return WALL_JSP;
	}

	@RequestMapping("{wallName}/status")
	public @ResponseBody
	List<ProjectStatus> getStatus(@PathVariable String wallName,
			ModelMap modelMap) throws NotFoundException {
		List<ProjectStatus> status = wallService.getStatus(wallName);
		return status;
	}

	@RequestMapping(value = "create", method = RequestMethod.GET)
	public String getCreate(ModelMap modelMap) {
		Wall wall = new Wall();
		modelMap.put("data", wall);
		modelMap.put("softwares", pluginService.getPluginsInfo());
		return WALL_JSP;
	}

	// @RequestMapping(value = "create", method = RequestMethod.POST)
	// public ModelAndView create(Wall wall) throws NotCreatedException {
	// wallService.persist(wall);
	// return null;
	// }

	@RequestMapping(method = RequestMethod.POST)
	public @ResponseBody Object update(Wall wall) {
		wallService.update(wall);
		return true;
	}

	@RequestMapping(value = "{wallName}", method = RequestMethod.DELETE)
	public void DeleteWall(@PathVariable String wallName) {
		throw new NotImplementedException();
	}

	@InitBinder
	public void initBinder(WebDataBinder binder, WebRequest request) {
		binder.setAutoGrowNestedPaths(false);
	}

}