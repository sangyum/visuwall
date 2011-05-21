/*
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

visuwall.theme.def.event.wallFormEvent = new function() {
	var $this = this;

	this.__inject__ = ['wallFormView', 'wallFormController'];
	
	
//	this.context = 'DIV#modal';
	this.softTabsCount = 1;
	this.tabs;
	
	
	this.init = function() {
		$this.tabs = $("#softTabs", this);
		var context = this;
		
		$this.tabs.tabs({
					tabTemplate : "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>",
					add : function(event, ui) {
						var contents = $('div[id^="tabs-"]', context);

						// -1 is the last but the last is the current added one,
						// so its -2
						var newContent = $(contents[contents.length - 2])
								.clone();

						ajsl.view.resetFormValues(newContent);
						ajsl.view.incrementFormIndexes(newContent);

						var childrens = newContent.children();
						for (var i = 0; i < childrens.length; i++) {
							$(ui.panel).append(childrens[i]);
						}
						var tt;
					},
					remove : function(event, ui) {
						// removeFunction(event, ui.panel);
					},
					preremove : function(event, ui) {
						if ($this.tabs.tabs('length') > 1) {
							return false;
						} else {
							return true;
						}
					}
				});
	};

	this['#softAdd|mouseenter'] = function() {
		$(this).addClass('ui-state-hover');
	};
	this['#softAdd|mouseleave'] = function() {
		$(this).removeClass('ui-state-hover');
	};

	this["#softTabs span.ui-icon-close|click|live"] = function() {
		var index = $("li", $this.tabs).index($(this).parent());
		$this.tabs.tabs("remove", index);
	};

	this["#wallForm|submit"] = function() {
		$("#modal .success").empty();
		$("#modal .failure").empty();
		$("#wallForm .loader").empty().html('<img src="res/img/ajax-loader.gif" />');
		$this.wallFormController.submitWallData(this, function() { // success
			$("#wallForm .loader").empty();
			$("#modal .success").html("Success");
			$("#modal").delay(2000).dialog('close');
		}, function (msg) { //failure
			$("#wallForm .loader").empty();
			$("#modal .failure").html("Error : " + msg);
		});
		return false;
	};

	// TODO
	// keydown
	// keyup
	// mousedown
	// mouseup
	// mousemove

	this['INPUT:regex(id,softwareAccesses.*\.url)|change|live'] = function() {
		var softTabs = $('#softTabs');
		var tabIdFull = $('DIV[id^="tabs-"]', softTabs).has(this).attr('id');
		var hostname = getHostname($(this).val());
		if (!hostname) {
			hostname = $(this).val();
		}
		if (!hostname) {
			hostname = 'New';
		}
		$('UL LI A[href="#' + tabIdFull + '"]', softTabs).html(hostname);
	};

	this['DIV#softAdd|click'] = function(event) {
		$this.wallFormView.addFormSoftwareAccesses();
	};

};