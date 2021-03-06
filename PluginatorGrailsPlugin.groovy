import org.slf4j.Logger
import org.slf4j.LoggerFactory

import grails.util.BuildSettings

class PluginatorGrailsPlugin {
	def version = '0.2.2'
	def grailsVersion = '2.0 > *'
	def author = 'Sergey Bondarenko'
	def authorEmail = 'enterit@gmail.com'
	def title = 'Pluginator'
	def description = 'This plugin lets you define callbacks and properties in an application that are normally only available in plugins, e.g. doWithWebDescriptor, doWithDynamicMethods, loadAfter, observe, etc.'
	def documentation = 'http://grails.org/plugin/pluginator'
	def license = 'LGPL'
	def issueManagement = [system: 'GitHub', url: 'http://github.com/bluesliverx/grails-pluginator/issues']
	def scm = [url: 'https://github.com/bluesliverx/grails-pluginator']
	def developers = [
		[name: 'Brian Saville', email: 'bksaville@gmail.com'],
		[name: 'Burt Beckwith', email: 'burt@burtbeckwith.com']
	]

	private Logger log = LoggerFactory.getLogger('grails.plugin.pluginator.PluginatorGrailsPlugin')
	private applicationPlugin

	def artefacts = []
	def evict = []
	def loadAfter = []
	def loadBefore = []
	def observe = []
	def providedArtefacts = []
	def watchedResources = []

	PluginatorGrailsPlugin() {
		initApplicationPlugin()
		if (!applicationPlugin) return

		for (String name in ['artefacts', 'evict', 'loadAfter', 'loadBefore',
		                     'observe', 'providedArtefacts', 'watchedResources']) {
			if (applicationPlugin.properties[name]) {
				def value = applicationPlugin."$name"
				if (log.debugEnabled) {
					log.debug "Setting '$name' value -> '$value'"
				}
				this."$name" = value
			}
		}
	}

	def doWithWebDescriptor = { xml ->
		callApplicationPluginAction 'doWithWebDescriptor', delegate, [xml]
	}

	def doWithSpring = {
		callApplicationPluginAction 'doWithSpring', delegate
	}

	def doWithDynamicMethods = { ctx ->
		callApplicationPluginAction 'doWithDynamicMethods', delegate, [ctx]
	}

	def doWithApplicationContext = { ctx ->
		callApplicationPluginAction 'doWithApplicationContext', delegate, [ctx]
	}

	def onChange = { event ->
		callApplicationPluginAction 'onChange', delegate, [event]
	}

	def onConfigChange = { event ->
		callApplicationPluginAction 'onConfigChange', delegate, [event]
	}

	def onShutdown = { event ->
		callApplicationPluginAction 'onShutdown', delegate, [event]
	}

	private void initApplicationPlugin() {
		if (applicationPlugin) {
			return
		}

		try {
			applicationPlugin = Class.forName("ApplicationPlugin", true, getClass().classLoader)?.newInstance()
		} catch(ClassNotFoundException e) {}
	}

	private callApplicationPluginAction(String actionName, delegate, args = []) {
		initApplicationPlugin()
		if (!applicationPlugin) return

		if (!(applicationPlugin.properties[actionName] instanceof Closure)) {
			return
		}

		if (log.traceEnabled) {
			log.trace "Calling '$actionName' with args $args"
		}
		else if (log.debugEnabled) {
			log.debug "Calling '$actionName'"
		}

		Closure action = applicationPlugin."$actionName"
		action.delegate = delegate
		action.call(*args)
	}
}
