# File created to get complexity, percentage metrics (%IntegratorKnowledge and %FormattingChanges), and timeDiff for each merge scenario

get.percentage.metrics.of.merge.conflict.metrics.by.ms = function(ms.id) {
  merge.conflict.metrics.data.frame <-
    get.list.of.merge.conflict.metrics.by.ms(mydb, ms.id[1, 1])
  dev.knew <- 0
  formatting <- 0
  merge.time <- 0
  perc.dev.knew <- 0
  perc.formating <- 0
  code.complexity <- 0
  
  result <- NULL
  
  if (nrow(merge.conflict.metrics.data.frame) > 0) {
    for (row in 1:nrow(merge.conflict.metrics.data.frame)) {
      code.complexity <-
        code.complexity + merge.conflict.metrics.data.frame[row, 1] + merge.conflict.metrics.data.frame[row, 2]
      
      if (merge.conflict.metrics.data.frame[row, 3] == 1) {
        dev.knew = dev.knew + 1
      }
      if (merge.conflict.metrics.data.frame[row, 4] == "F") {
        formatting <- formatting + 1
      }
      
    }
    perc.dev.knew <-
      (dev.knew * 100) / nrow(merge.conflict.metrics.data.frame)
    perc.formating <-
      (formatting * 100) / nrow(merge.conflict.metrics.data.frame)
    
    if (merge.conflict.metrics.data.frame[1, 5] > merge.conflict.metrics.data.frame[1, 6]) {
      merge.time <- merge.conflict.metrics.data.frame[1, 6]
    } else {
      merge.time <- merge.conflict.metrics.data.frame[1, 5]
    }
    
    ms.id$complexity <- code.complexity
    ms.id$integrator.knew <- perc.dev.knew
    ms.id$formatting <- perc.formating
    ms.id$merge.time.diff <- merge.time
    
    result <- ms.id
    
  }
  return(result)
}

# Map ids in the db and ids used in the paper
get.right.project.id = function (data, column) {
  row <- 1
  for (row in 1:nrow(data)) {
    if (data[row, column] == 5) {
      data[row, column] <- 28
    } else if (data[row, column] == 6) {
      data[row, column] <- 26
    } else if (data[row, column] == 7) {
      data[row, column] <- 10
    } else if (data[row, column] == 9) {
      data[row, column] <- 1
    } else if (data[row, column] == 10) {
      data[row, column] <- 36
    } else if (data[row, column] == 11) {
      data[row, column] <- 3
    } else if (data[row, column] == 13) {
      data[row, column] <- 4
    } else if (data[row, column] == 14) {
      data[row, column] <- 27
    } else if (data[row, column] == 15) {
      data[row, column] <- 63
    } else if (data[row, column] == 16) {
      data[row, column] <- 19
    } else if (data[row, column] == 17) {
      data[row, column] <- 5
    } else if (data[row, column] == 18) {
      data[row, column] <- 21
    } else if (data[row, column] == 19) {
      data[row, column] <- 68
    } else if (data[row, column] == 21) {
      data[row, column] <- 16
    } else if (data[row, column] == 23) {
      data[row, column] <- 44
    } else if (data[row, column] == 24) {
      data[row, column] <- 52
    } else if (data[row, column] == 25) {
      data[row, column] <- 53
    } else if (data[row, column] == 28) {
      data[row, column] <- 50
    } else if (data[row, column] == 29) {
      data[row, column] <- 58
    } else if (data[row, column] == 30) {
      data[row, column] <- 45
    } else if (data[row, column] == 31) {
      data[row, column] <- 25
    } else if (data[row, column] == 32) {
      data[row, column] <- 35
    } else if (data[row, column] == 33) {
      data[row, column] <- 67
    } else if (data[row, column] == 36) {
      data[row, column] <- 20
    } else if (data[row, column] == 39) {
      data[row, column] <- 17
    } else if (data[row, column] == 40) {
      data[row, column] <- 61
    } else if (data[row, column] == 43) {
      data[row, column] <- 66
    } else if (data[row, column] == 44) {
      data[row, column] <- 30
    } else if (data[row, column] == 45) {
      data[row, column] <- 84
    } else if (data[row, column] == 46) {
      data[row, column] <- 88
    } else if (data[row, column] == 48) {
      data[row, column] <- 57
    } else if (data[row, column] == 50) {
      data[row, column] <- 59
    } else if (data[row, column] == 54) {
      data[row, column] <- 46
    } else if (data[row, column] == 55) {
      data[row, column] <- 38
    } else if (data[row, column] == 57) {
      data[row, column] <- 49
    } else if (data[row, column] == 59) {
      data[row, column] <- 65
    } else if (data[row, column] == 63) {
      data[row, column] <- 80
    } else if (data[row, column] == 66) {
      data[row, column] <- 31
    } else if (data[row, column] == 68) {
      data[row, column] <- 78
    } else if (data[row, column] == 70) {
      data[row, column] <- 64
    } else if (data[row, column] == 71) {
      data[row, column] <- 76
    } else if (data[row, column] == 72) {
      data[row, column] <- 29
    } else if (data[row, column] == 76) {
      data[row, column] <- 82
    } else if (data[row, column] == 77) {
      data[row, column] <- 69
    } else if (data[row, column] == 78) {
      data[row, column] <- 2
    } else if (data[row, column] == 80) {
      data[row, column] <- 23
    } else if (data[row, column] == 81) {
      data[row, column] <- 24
    } else if (data[row, column] == 84) {
      data[row, column] <- 75
    } else if (data[row, column] == 85) {
      data[row, column] <- 41
    } else if (data[row, column] == 87) {
      data[row, column] <- 48
    } else if (data[row, column] == 88) {
      data[row, column] <- 51
    } else if (data[row, column] == 89) {
      data[row, column] <- 33
    } else if (data[row, column] == 90) {
      data[row, column] <- 56
    } else if (data[row, column] == 94) {
      data[row, column] <- 60
    } else if (data[row, column] == 95) {
      data[row, column] <- 83
    } else if (data[row, column] == 96) {
      data[row, column] <- 86
    } else if (data[row, column] == 98) {
      data[row, column] <- 89
    } else if (data[row, column] == 99) {
      data[row, column] <- 90
    } else if (data[row, column] == 100) {
      data[row, column] <- 92
    } else if (data[row, column] == 101) {
      data[row, column] <- 93
    } else if (data[row, column] == 103) {
      data[row, column] <- 95
    } else if (data[row, column] == 104) {
      data[row, column] <- 96
    } else if (data[row, column] == 105) {
      data[row, column] <- 97
    }
  }
  return(data)
}
