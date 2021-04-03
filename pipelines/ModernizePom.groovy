import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlSlurper
import groovy.xml.XmlUtil
import java.nio.file.StandardCopyOption;
import static java.nio.file.Paths.get
import static java.nio.file.Files.copy
import static java.nio.file.Files.delete


String workspace = "F:\\multi-module-project"

def propertyUpgradeMap = [
        'maven.compiler.source': '1.8',
        'maven.compiler.target': '1.8',
        'java.version': '1.8'
];

def pluginUpgradeMap = [
        'maven-surefire-plugin': '2.22.0',
        'maven-dependency-plugin': '3.1.1',
        'maven-war-plugin': '3.3.1'

];

def dependenciesUpgradeMap = [
        'spring-context': '5.3.5',
        'spring-webmvc':'5.3.5',
        'spring-test':'5.3.5',
        'junit-jupiter-engine':'5.7.1',
        'junit-jupiter-params':'5.7.1',
        'logback-classic':'1.2.3',
        'hamcrest-library':'1.3',
        'javax.servlet-api':'4.0.1',
        'thymeleaf':'3.0.12.RELEASE',
        'thymeleaf-spring5':'3.0.12.RELEASE'

];

def projectTraversal(workspace,propertyUpgradeMap,dependenciesUpgradeMap,pluginUpgradeMap){
    println "----- Release Script Start -----"
   File rootDirectory = new File(workspace)
    rootDirectory.eachFileRecurse {
        if(it.name.endsWith("pom.xml")){
            createBackupPom(it)
            def pom = new XmlSlurper(false, false).parse(it)
                print "update versions in properties "
                pom.properties.childNodes().each{
                    if(propertyUpgradeMap.containsKey(it.name()))
                        it.replaceBody(propertyUpgradeMap.get(it.name()))
                }

                print "updating versions in dependencies "
                pom.dependencies.dependency.each{
                    if(dependenciesUpgradeMap.containsKey(it.artifactId.text()))
                        it.version.replaceBody(dependenciesUpgradeMap.get(it.artifactId.text()))
                }

                print "updating plugins in dependencies "
                pom.build.plugins.plugin.each{
                    if(pluginUpgradeMap.containsKey(it.artifactId.text()))
                        it.version.replaceBody(pluginUpgradeMap.get(it.artifactId.text()))
                }

            print "Writing new pom.xml "
            def outputBuilder = new StreamingMarkupBuilder()
            String result = outputBuilder.bind{
                mkp.yield pom
            }
            def writer = it.newWriter()
            writer << XmlUtil.serialize(result)
            writer.close()
            println "OK"
            println "Release Script End -----"

            //deleteBackupPom(it)
        }
    }

}


def createBackupPom(file){
    println "A copy of the old pom for processing"
    copy(file.toPath(), get(file.toString().replaceAll("pom.xml",""),'pom.backup.xml') ,StandardCopyOption.REPLACE_EXISTING)
}

def deleteBackupPom(file){
    println "Back up pom is deleted at every module"
    delete(get(file.toString().replaceAll("pom.xml",""),'pom.backup.xml'))
}

projectTraversal(workspace,propertyUpgradeMap,dependenciesUpgradeMap,pluginUpgradeMap)
