#!/usr/bin/env groovy

def pipeline_id = env.BUILD_ID
println "Current pipeline job build id is '${pipeline_id}'"
def node_label = 'CCI && ansible-2.4'
def deployments_per_ns = DEPLOYMENTS_PER_NS.toString().toUpperCase()

// run deployments_per_ns scale test
stage ('deployments_per_ns_scale_test') {
	if (deployments_per_ns == "TRUE") {
		currentBuild.result = "SUCCESS"
		node('CCI && US') {
			// get properties file
			if (fileExists("deployments_per_ns.properties")) {
				println "Looks like deployments_per_ns.properties file already exists, erasing it"
				sh "rm deployments_per_ns.properties"
			}
			// get properties file
			//sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/deployments_per_ns.properties"
			sh "wget ${DEPLOYMENTS_PER_NS_PROPERTY_FILE} -O deployments_per_ns.properties"
			sh "cat deployments_per_ns.properties"
			def deployments_per_ns_properties = readProperties file: "deployments_per_ns.properties"
			def jump_host = deployments_per_ns_properties['JUMP_HOST']
			def user = deployments_per_ns_properties['USER']
			def tooling_inventory_path = deployments_per_ns_properties['TOOLING_INVENTORY']
			def clear_results = deployments_per_ns_properties['CLEAR_RESULTS']
			def move_results = deployments_per_ns_properties['MOVE_RESULTS']
			def use_proxy = deployments_per_ns_properties['USE_PROXY']
			def proxy_user = deployments_per_ns_properties['PROXY_USER']
			def proxy_host = deployments_per_ns_properties['PROXY_HOST']
			def containerized = deployments_per_ns_properties['CONTAINERIZED']
			def deployments = deployments_per_ns_properties['DEPLOYMENTS']
	
			// debug info
			println "----------USER DEFINED OPTIONS-------------------"
			println "-------------------------------------------------"
			println "-------------------------------------------------"
			println "JUMP_HOST: '${jump_host}'"
			println "USER: '${user}'"
			println "TOOLING_INVENTORY_PATH: '${tooling_inventory_path}'"
			println "CLEAR_RESULTS: '${clear_results}'"
			println "MOVE_RESULTS: '${move_results}'"
			println "USE_PROXY: '${use_proxy}'"
			println "PROXY_USER: '${proxy_user}'"
			println "PROXY_HOST: '${proxy_host}'"
			println "CONTAINERIZED: '${containerized}'"
			println "-------------------------------------------------"
			println "-------------------------------------------------"

			// Run deployments_per_ns job
			try {
				deployments_per_ns_build = build job: 'DEPLOYMENTS_PER_NS',
				parameters: [   [$class: 'LabelParameterValue', name: 'node', label: node_label ],
						[$class: 'StringParameterValue', name: 'JUMP_HOST', value: jump_host ],
						[$class: 'StringParameterValue', name: 'USER', value: user ],
						[$class: 'StringParameterValue', name: 'TOOLING_INVENTORY', value: tooling_inventory_path ],
						[$class: 'BooleanParameterValue', name: 'CLEAR_RESULTS', value: Boolean.valueOf(clear_results) ],
						[$class: 'BooleanParameterValue', name: 'MOVE_RESULTS', value: Boolean.valueOf(move_results) ],
						[$class: 'BooleanParameterValue', name: 'USE_PROXY', value: Boolean.valueOf(use_proxy) ],
						[$class: 'StringParameterValue', name: 'PROXY_USER', value: proxy_user ],
						[$class: 'StringParameterValue', name: 'PROXY_HOST', value: proxy_host ],
						[$class: 'StringParameterValue', name: 'DEPLOYMENTS', value: deployments ],
						[$class: 'BooleanParameterValue', name: 'CONTAINERIZED', value: Boolean.valueOf(containerized) ]]
			} catch ( Exception e) {
				echo "DEPLOYMENTS_PER_NS Job failed with the following error: "
				echo "${e.getMessage()}"
				echo "Sending an email"
				mail(
					to: 'nelluri@redhat.com',
					subject: 'Podvertical-scale-test job failed',
					body: """\
						Encoutered an error while running the deployments_per_ns-scale-test job: ${e.getMessage()}\n\n
						Jenkins job: ${env.BUILD_URL}
				""")
				currentBuild.result = "FAILURE"
                        	sh "exit 1"
                        }
                        println "Deployments per ns build ${deployments_per_ns_build.getNumber()} completed successfully"
		}
	}
}
