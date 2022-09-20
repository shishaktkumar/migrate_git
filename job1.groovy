job('Job1') {
  description('setup for Delhpix project')

  //disabled()

  logRotator {
    numToKeep(5)
    daysToKeep(7)
    }
  environmentVariables {
    envs(EMAIL : "shishakt",
         GIT_REPO : 'https://github.com/shishakt/migrate_git.git',
         BRANCH : 'master',
         REPO_ALIAS : 'delphix_task'
    )
  }
  label('master')

  wrappers {
    logSizeChecker()
  }

  wrappers {
    timeout {
      absolute(360)
    }
  }

  scm {
    git {
      remote {
        name('$REPO_ALIAS')
        url('$GIT_REPO')
        credentials('GITHUB')
        refspec('+refs/heads/$BRANCH:refs/remotes/$REPO_ALIAS/$BRANCH')
       }
       branch('$BRANCH')
       extensions {
         cleanAfterCheckout()
         cloneOptions {
           honorRefspec(true)
         }
     }
   }
  }
  triggers {
      scm('H/3 * * * *')
  }
  steps{
    shell('''#!/bin/bash -x 
tar -cvf artifacts.tar *
mv job1.groovy job1_${BUILD_NUMBER}.groovy
echo buildnumber=${BUILD_NUMBER} > prop.properties
''')
    
  }
   
   publishers {
     postBuildScripts {
      archiveArtifacts {
        pattern('artifacts.tar')
        defaultExcludes(defaultExcludes = true)
        onlyIfSuccessful()
      }
    }
   }
}
