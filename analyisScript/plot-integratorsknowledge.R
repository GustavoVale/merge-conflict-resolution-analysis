## Libraries ----

requireNamespace("ggplot2")
requireNamespace("plyr")
requireNamespace("reshape2")
requireNamespace("scales")


## Load data ----

## data on MS with integrators *with* knowledge (#IntegratorsKnowledge > 0)
data.knowledge.with = read.table(file = "IntegratorHasSomeKnowledge.csv", header = TRUE, sep = ",", row.names = NULL)
data.knowledge.with["integrators.knowledge.binary"] = TRUE  # add dichotomous variable for easier filtering
data.knowledge.with = data.knowledge.with[data.knowledge.with[["merge.time.diff"]] > 0, ] # remove data with 'merge.time.diff' <= 0

## data on MS with integrators *without* knowledge (#IntegratorsKnowledge = 0)
data.knowledge.without = read.table(file = "IntegratorHasNoKnowledge.csv", header = TRUE, sep = ",", row.names = NULL)
data.knowledge.without["integrators.knowledge.binary"] = FALSE  # add dichotomous variable for easier filtering
data.knowledge.without = data.knowledge.without[data.knowledge.without[["merge.time.diff"]] > 0, ] # remove data with 'merge.time.diff' <= 0

## combine both data sources
data.knowledge = plyr::rbind.fill(data.knowledge.with, data.knowledge.without)

## rename columns
colnames(data.knowledge) = c(
    "ms_id", "pr_id", # identifier
    "#Files", "#ConfFiles", "#Chunks", "#ConfChunks", "#Devs",
    "#LOC", "#ConfLOC", "CodeComplexity", "%IntegratorKnowledge",
    "%FormattingChanges", "#SecondsToMerge",
    "integrators.knowledge.binary"
)
colnames(data.knowledge.with) = colnames(data.knowledge)
colnames(data.knowledge.without) = colnames(data.knowledge)


## Statistical test ----

## Wicoxon test

test.filter.column = "#ConfFiles"
test.filter = function(df) {
    # df[df[[test.filter.column]] <= quantile(df[[test.filter.column]], 0.1), ]
    df[df[[test.filter.column]] >= quantile(df[[test.filter.column]], 0.9), ]
    # df # no filtering
}
test.column = "%IntegratorKnowledge" # "CodeComplexity" # "#Devs" # #LOC "#Files" 

wt = wilcox.test(
        x = test.filter(data.knowledge.with)[[test.column]],
    y = test.filter(data.knowledge.without)[[test.column]],
    alternative = "greater",
    conf.level = 0.95
)

if (wt$p.value > 0.05) {
    cat(paste(
        "There *is no* significant difference between integrators with and without",
        "previous knowledge w.r.t. merge-conflict resolution time.\n",
        "p =", wt$p.value, ", W =", wt$statistic
    ))
} else {
    cat(paste(
        "There *is* significant difference between integrators with and without",
        "previous knowledge w.r.t. merge-conflict resolution time.\n",
        "p =", wt$p.value, ", W =", wt$statistic
    ))
}


## correlation

test.column = "#ConfFiles"
ct = cor(x = data.knowledge[[test.column]], y = data.knowledge[["%IntegratorKnowledge"]])
print(ct)


## Basic plot ----

data.knowledge.basic = data.knowledge[c("integrators.knowledge.binary", "#SecondsToMerge")]
colnames(data.knowledge.basic) = c("integrators.knowledge.binary", "SecondsToMerge")
g = ggplot2::ggplot(data = data.knowledge.basic, mapping = ggplot2::aes(x = integrators.knowledge.binary, y = SecondsToMerge)) +
    # ggplot2::geom_hline(yintercept = 5000, linetype = "dashed") +  # only for testing purposes
    ## geometry
    ggplot2::geom_violin(ggplot2::aes(colour = integrators.knowledge.binary), draw_quantiles = c(0.5), size = 1.2) +
    # scales + ticks
    ggplot2::scale_y_log10(breaks = scales::trans_breaks("log10", function(x) 10^x, n = 10),
                  labels = scales::trans_format("log10", scales::math_format(10^.x)),
                  expand = ggplot2::expand_scale(mult = c(0.01, 0.075))) +
    ggplot2::annotation_logticks(sides = "l") +
    ## descriptions
    ggplot2::xlab(expression(italic("%IntegratorKnowledge") > 0)) +
    ggplot2::ylab(expression(italic("#SecondsToMerge"))) +
    ## themeing
    ggplot2::scale_color_viridis_d(begin = 0.3, end = 0.7, guide = FALSE) +
    ggplot2::theme_bw()

print(g)
ggplot2::ggsave("figure-integrators-knowledge.pdf", width = NA, height = NA)


## Plot with all variables ----

## 1) remove obsolete column(s)
data.knowledge["%IntegratorKnowledge"] = NULL

## 2) meld data
data.knowledge.melt = reshape2::melt(
    data.knowledge,
    id.vars = c("ms_id", "pr_id", "integrators.knowledge.binary")
)

## 3) plot!
g = ggplot2::ggplot(data = data.knowledge.melt, mapping = ggplot2::aes(x = integrators.knowledge.binary, y = value)) +
    ggplot2::facet_wrap(. ~ variable, ncol = 5) +
    # ggplot2::geom_hline(yintercept = 5000, linetype = "dashed") +  # only for testing purposes
    ## geometry
    ggplot2::geom_violin(ggplot2::aes(colour = integrators.knowledge.binary), draw_quantiles = c(0.5), size = 1.2) +
    ## scales + ticks
    ggplot2::scale_y_log10(breaks = scales::trans_breaks("log10", function(x) 10^x, n = 10),
                           labels = scales::trans_format("log10", scales::math_format(10^.x)),
                           expand = ggplot2::expand_scale(mult = c(0.01, 0.075))) +
    ggplot2::annotation_logticks(sides = "l") +
    ## descriptions
    ggplot2::xlab(expression(italic("%IntegratorKnowledge") > 0)) +
    ggplot2::ylab(NULL) +
    ## themeing
    ggplot2::scale_color_viridis_d(begin = 0.3, end = 0.7, guide = FALSE) +
    ggplot2::theme_bw()

print(g)
ggplot2::ggsave("figure-integrators-knowledge-all.pdf", width = NA, height = NA)
