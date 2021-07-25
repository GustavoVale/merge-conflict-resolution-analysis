


get.list.of.projects = function(mydb, project.id) {
  #Query
  rs <-
    dbSendQuery(mydb,
                paste("SELECT * FROM projects WHERE id=(", project.id, ")"))
  #fetch query
  return(fetch(rs, n = -1))
}

get.list.of.projects.metrics = function(mydb, project.id) {
  #Query
  rs <-
    dbSendQuery(mydb,
                paste(
                  "SELECT * FROM project_metrics WHERE project_id=(",
                  project.id,
                  ")"
                ))
  #fetch query
  return(fetch(rs, n = -1))
}

get.list.of.ms.metrics = function(mydb, project.id) {
  #Query
  rs <-
    dbSendQuery(
      mydb,
      paste(
        "select merge_scenario_id as ms_id, project_id as pr_id, number_of_files as files, number_conflicted_files as confFiles,
        number_chunks as chunks, number_conflicted_chunks as confChunks, number_developers as devs, code_churn, conflict_code_churn as confCodeChurn
        from ms_metrics inner join merge_scenarios on ms_metrics.merge_scenario_id=merge_scenarios.id where has_conflict=1 AND project_id=(",
        project.id,
        ")"
      )
    )
  #fetch query
  return(fetch(rs, n = -1))
}

get.list.of.merge.conflict.metrics.by.ms = function(mydb, ms.id) {
  #Query
  rs <-
    dbSendQuery(
      mydb,
      paste(
        "select left_cyclomatic_complexity as left_cc, right_cyclomatic_complexity as right_cc, dev_has_knowledge, change_type, left_merge_time_diff,
        right_merge_time_diff from merge_scenarios inner join files on merge_scenarios.id=files.merge_scenarios_id inner join chunks on files.id=chunks.file_id
        inner join merge_conflict_info on chunks.id=merge_conflict_info.chunk_id inner join merge_conflict_metrics on 
        merge_conflict_info.id=merge_conflict_metrics.merge_conflict_info_id where merge_scenarios.id=(",
        ms.id,
        ")"
      )
    )
  #fetch query
  return(fetch(rs, n = -1))
}