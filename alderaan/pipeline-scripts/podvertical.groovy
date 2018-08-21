#!/usr/bin/env groovy

def pipeline_id = env.BUILD_ID
println "Current pipeline job build id is '${pipeline_id}'"
def node_label = 'CCI && ansible-2.4'
def podvertical = PODVERTICAL.toString().toUpperCase()

// run podvertical scale test
stage ('podvertical_scale_test') {
	if (podvertical == "TRUE") {
		currentBuild.result = "SUCCESS"
		node('CCI && US') {
			// get properties file
			if (fileExists("podvertical.properties")) {
				println "Looks like podvertical.properties file already exists, erasing it"
				sh "rm podvertical.properties"
			}
			// get properties file
			//sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/podvertical.properties"
			sh "wget ${PODVERTICAL_PROPERTY_FILE} -O podvertical.properties"
			sh "cat podvertical.properties"
			def podvertical_properties = readProperties file: "podvertical.properties"
			def jump_host = podvertical_properties['JUMP_HOST']
			def user = podvertical_properties['USER']
			def tooling_inventory_path = podvertical_properties['TOOLING_INVENTORY']
			def clear_results = podvertical_properties['CLEAR_RESULTS']
			def move_results = podvertical_properties['MOVE_RESULTS']
			def use_proxy = podvertical_properties['USE_PROXY']
			def proxy_user = podvertical_properties['PROXY_USER']
			def proxy_host = podvertical_properties['PROXY_HOST']
			def containerized = podvertical_properties['CONTAINERIZED']
			def pods = podvertical_properties['PODS']
			def iterations = podvertical_properties['ITERATIONS']
	
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

			// Run podvertical job
			try {
				podvertical_build = build job: 'PODVERTICAL',
				parameters: [   [$class: 'LabelParameterValue', name: 'node', label: node_label ],
						[$class: 'StringParameterValue', name: 'JUMP_HOST', value: jump_host ],
						[$class: 'StringParameterValue', name: 'USER', value: user ],
						[$class: 'StringParameterValue', name: 'TOOLING_INVENTORY', value: tooling_inventory_path ],
						[$class: 'BooleanParameterValue', name: 'CLEAR_RESULTS', value: Boolean.valueOf(clear_results) ],
						[$class: 'BooleanParameterValue', name: 'MOVE_RESULTS', value: Boolean.valueOf(move_results) ],
						[$class: 'BooleanParameterValue', name: 'USE_PROXY', value: Boolean.valueOf(use_proxy) ],
						[$class: 'StringParameterValue', name: 'PROXY_USER', value: proxy_user ],
						[$class: 'StringParameterValue', name: 'PROXY_HOST', value: proxy_host ],
						[$class: 'StringParameterValue', name: 'PODS', value: pods ],
						[$class: 'StringParameterValue', name: 'ITERATIONS', value: iterations ],
						[$class: 'BooleanParameterValue', name: 'CONTAINERIZED', value: Boolean.valueOf(containerized) ]]
			} catch ( Exception e) {
				echo "PODVERTICAL Job failed with the following error: "
				echo "${e.getMessage()}"
				echo "Sending an email"
				mail(
					to: 'nelluri@redhat.com',
					subject: 'Podvertical-scale-test job failed',
					body: """\
						Encoutered an error while running the podvertical-scale-test job: ${e.getMessage()}\n\n
						Jenkins job: ${env.BUILD_URL}
				""")
				currentBuild.result = "FAILURE"
                        	sh "exit 1"
                        }
                        println "POD-VERTICAL build ${podvertical_build.getNumber()} completed successfully"
		}
	}
}
