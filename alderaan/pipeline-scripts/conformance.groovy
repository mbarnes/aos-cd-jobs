#!/usr/bin/env groovy

def pipeline_id = env.BUILD_ID
println "Current pipeline job build id is '${pipeline_id}'"
def node_label = 'CCI && ansible-2.4'
def run_conformance = CONFORMANCE.toString().toUpperCase()

// run conformance
// use master host instead of jump host for conformance tests
stage ('conformance') {
		if (run_conformance == "TRUE") {
		currentBuild.result = "SUCCESS"
		node('CCI && US') {
			// get properties file
			if (fileExists("conformance.properties")) {
				println "Looks like conformance.properties file already exists, erasing it"
				sh "rm conformance.properties"
			}
			// get properties file
			//sh "wget http://file.rdu.redhat.com/~nelluri/pipeline/conformance.properties"
			sh "wget ${CONFORMANCE_PROPERTY_FILE} -O conformance.properties"
			sh "cat conformance.properties"
			def conformance_properties = readProperties file: "conformance.properties"
			def master_hostname = conformance_properties['MASTER_HOSTNAME']
			def user = conformance_properties['USER']
			def enable_pbench = conformance_properties['ENABLE_PBENCH']
			def use_proxy = conformance_properties['USE_PROXY']
			def proxy_user = conformance_properties['PROXY_USER']
			def proxy_host = conformance_properties['PROXY_HOST']

			// debug info
			println "----------USER DEFINED OPTIONS-------------------"
			println "-------------------------------------------------"
			println "-------------------------------------------------"
			println "MASTER_HOSTNAME: '${master_hostname}'"
			println "USER: '${user}'"
			println "ENABLE_PBENCH: '${enable_pbench}'"
			println "USE_PROXY: '${use_proxy}'"
			println "PROXY_USER: '${proxy_user}'"
			println "PROXY_HOST: '${proxy_host}'"
			println "-------------------------------------------------"
			println "-------------------------------------------------"	
		
			// Run conformance job
			try {
				conformance_build = build job: 'SVT_conformance',
				parameters: [   [$class: 'LabelParameterValue', name: 'node', label: node_label ],
						[$class: 'StringParameterValue', name: 'MASTER_HOSTNAME', value: master_hostname ],
						[$class: 'StringParameterValue', name: 'MASTER_USER', value: user ],
						[$class: 'StringParameterValue', name: 'ENABLE_PBENCH', value: enable_pbench ],
						[$class: 'BooleanParameterValue', name: 'USE_PROXY', value: Boolean.valueOf(use_proxy) ],
						[$class: 'StringParameterValue', name: 'PROXY_USER', value: proxy_user ],
						[$class: 'StringParameterValue', name: 'PROXY_HOST', value: proxy_host ]]
			} catch ( Exception e) {
				echo "CONFORMANCE Job failed with the following error: "
				echo "${e.getMessage()}"
				mail(
					to: 'nelluri@redhat.com',
					subject: 'Conformance job failed',
					body: """\
						Encoutered an error while running the conformance job: ${e.getMessage()}\n\n
						Jenkins job: ${env.BUILD_URL}
				""")
			currentBuild.result = "FAILURE"
			sh "exit 1"
			}
			println "CONFORMANCE build ${conformance_build.getNumber()} completed successfully"
		}
	}
}
