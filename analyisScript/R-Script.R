#installed.packages()

# install.packages("DBI")
# install.packages("RMySQL")

library("DBI")
library("RMySQL")

source("util-init.R")

mydb <- get.database.connection.server()

#Set the id of the projects in the database that you want to analyse
# 66 projects to analyze
target.projects <- c(5,6,7,9,10,11,12,13,14,15,16,17,18,19,21,23,24,25,28,29,
                     30,31,32,33,36,39,40,42,43,44,45,46,48,50,54,55,57,59,63,64,
                     66,68,70,71,72,76,77,78,80,81,84,85,87,88,89,90,94,95,96,98,
                     99,100,101,103,104,105)

msMetric.list <- data.frame()
project.list <- data.frame()
studyMetrics <- data.frame()

#---------------------------------------------------------------------------------------------------------------------------
# Computing Project metrics 
for(project.id in target.projects){
  project.data <- get.list.of.projects(mydb, project.id)
  project.data2 <- get.list.of.projects.metrics(mydb, project.id)
  project.data3 <- cbind.data.frame(project.data, project.data2)
  project.list <- rbind(project.list, project.data3)
}

rm(project.data)
rm(project.data2)
rm(project.data3)

project.list <- subset(project.list, select = c(name, project_id, loc, ms_computed, ms_ignored, ms_conflicted, number_of_files,
                                                  number_of_chunks, number_of_commits, number_of_developers, num_of_distinct_files))

colnames(project.list) <- c("name", "proj_id", "loc", "ms_comp", "ms_ign","ms_cnfl", "files", "chunks", "commits", "devs", "dist.fi")

proj.names <- names(project.list)
proj.names <- proj.names[!proj.names %in% c("loc", "ms_ign", "dist.fi")]
project.list <- project.list[, proj.names]

project.list <- get.right.project.id(project.list, 2)

#write CSV
write.csv(project.list, file = "projectMetrics.csv", row.names = FALSE)

#---------------------------------------------------------------------------------------------------------------------------
# GETTING AND ORGANIZING MERGE SCENARIOS

# Get merge scenarios-related metrics for all merge scenarios of the target projects
for (project.id in target.projects){
  ms.investigated.metrics <- get.list.of.ms.metrics(mydb, project.id)
  msMetric.list <- rbind(msMetric.list, ms.investigated.metrics)
}
rm(ms.investigated.metrics)

msMetric.list$complexity <- 0
msMetric.list$integrator.knew <- 0.0
msMetric.list$formatting <-0.0
msMetric.list$merge.time.diff <- 0

for (row in 1:nrow(msMetric.list)){
  aux.data.frame <- get.percentage.metrics.of.merge.conflict.metrics.by.ms(msMetric.list[row,])
  
  if (!is.null(dim(aux.data.frame))){
    studyMetrics <- rbind(studyMetrics, aux.data.frame)
  }
}

studyMetrics <- get.right.project.id(studyMetrics, 2)

rm(aux.data.frame)

print("the number of conflicting merge scenarios: ")
print(nrow(msMetric.list))
print("the number of conflicting merge scenarios we are able to investigate: ")
print(nrow(studyMetrics))

write.csv(studyMetrics, file = "InvestigatedMetrics.csv", row.names = FALSE)

#---------------------------------------------------------------
# Integrator knowledge analysis

noKnowledge <- subset(studyMetrics, integrator.knew==0)
someknowledge <- subset(studyMetrics, integrator.knew>0)
allknowledge <- subset(studyMetrics, integrator.knew==100)

write.csv(noKnowledge, file = "IntegratorHasNoKnowledge.csv", row.names = FALSE)
write.csv(someknowledge, file = "IntegratorHasSomeKnowledge.csv", row.names = FALSE)


allFormatting <- subset(studyMetrics, formatting==100)


selectedSystems <- read.csv(file = "SelectedSystems.csv", header = TRUE)
selectedSystems$old.pr.id <- selectedSystems$id
selectedSystems.new.id <- get.right.project.id(selectedSystems, 1)

write.csv(selectedSystems.new.id, file = "SelectedProjects.csv", row.names = FALSE)
selectedSystems <- read.csv(file = "SelectedProjects.csv", header = TRUE)


print("FINISHED")

close.db.connection(mydb)

