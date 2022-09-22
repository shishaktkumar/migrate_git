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
    groovy ('''
import hudson.*
import jenkins.*
import jenkins.model.*
import hudson.model.*
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.Domain;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;

def jenkinsCredentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        com.cloudbees.plugins.credentials.Credentials.class,
        Jenkins.instance,null,null);

def mapList=[:]
for (creds in jenkinsCredentials) {
  if(creds.id == "GITHUB"){
        mapList['GITHUB_USERNAME']= creds.username.toString()
        mapList['GITHUB_PASSWORD']= creds.password.toString()
    }
}

return(mapList)
''')
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
    environmentVariables {
      propertiesFile('prop.properties')
    }
  }
   
  /* publishers {
     postBuildScripts {
      archiveArtifacts {
        pattern('artifacts.tar')
        defaultExcludes(defaultExcludes = true)
        onlyIfSuccessful()
      }
    }
    postBuildScripts {
      steps {
        shell('''#!/bin/bash
REPO_NAME=$(echo $GIT_REPO | cut -d'/' -f5 | cut -d'.' -f1)
MOD_REPO=$(echo $GIT_REPO | cut -d'/' -f3-)

PUSH_REPO="https://${GITHUB_USERNAME}:${GITHUB_PASSWORD}@${MOD_REPO}"
git pull ${PUSH_REPO} ${BRANCH}
git add job1.groovy_${buildnumber}
git commit -m"Updating file from jenkins job" --no-verify
git push ${PUSH_REPO} ${BRANCH}
''')
      }
    }
   }*/
}
