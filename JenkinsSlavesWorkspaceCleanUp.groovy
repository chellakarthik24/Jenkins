import hudson.model.*;
import hudson.util.*;
import jenkins.model.*;
import hudson.FilePath.FileCallable;
import hudson.slaves.OfflineCause;
import hudson.node_monitors.*;

//job deletes the slaves workspace if they are less than threshold limit specified
thresholdInMB = 1500

for (node in Jenkins.instance.nodes) {
    computer = node.toComputer()
    if (computer.getChannel() == null) continue

    rootPath = node.getRootPath()
    size = DiskSpaceMonitor.DESCRIPTOR.get(computer).size
    roundedSize = size / (1024 * 1024) as int

    println("node: " + node.getDisplayName() + ", free space: " + roundedSize + " MB")
  
if (roundedSize < thresholdInMB) {
  
    computer.setTemporarilyOffline(true, new hudson.slaves.OfflineCause.ByCLI("disk cleanup"))
  
    performCleanup(node, Jenkins.instance.items)
  
    computer.setTemporarilyOffline(false, null)
  }
}

def performCleanup(def node, def items) {
  
  for (item in items) {
    jobName = item.getFullDisplayName()
    
    println("Cleaning " + jobName)
    
    if(item instanceof com.cloudbees.hudson.plugins.folder.AbstractFolder) {
      	performCleanup(node, item.items)
    	continue
    }
    
    if (item.isBuilding()) {
      println(".. job " + jobName + " is currently running, skipped")
      continue
    }
    
    println(".. wiping out workspaces of job " + jobName)
    
    workspacePath = node.getWorkspaceFor(item)
    if (workspacePath == null) {
      println(".... could not get workspace path")
      continue
    }
    
    println(".... workspace = " + workspacePath)
    
    pathAsString = workspacePath.getRemote()
    if (workspacePath.exists()) {
      workspacePath.deleteRecursive()
      println(".... deleted from location " + pathAsString)
    } else {
      println(".... nothing to delete at " + pathAsString)
    }
  }  
}

