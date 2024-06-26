setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source("../../scripts/h2o-r-test-setup.R")



test.pubdev.1654 <- function() {
  k <- 5
  use_all_factor_levels <- FALSE
  
  Log.info("Importing birds.csv data...")
  birds.dat <- read.csv(locate("smalldata/pca_test/birds.csv"), header = TRUE, stringsAsFactors = TRUE)
  birds.hex <- h2o.importFile(locate("smalldata/pca_test/birds.csv"))
  print(summary(birds.hex))
  
  Log.info("Reshuffling R data to match H2O and removing rows with NAs...")
  birds.mm <- alignData(birds.dat, center = TRUE, scale = TRUE, use_all_factor_levels = TRUE)
  
  # Move Ref factors from patch column to front since H2O does same
  refs <- grepl("patch.Ref", colnames(birds.mm))
  birds.cmp <- birds.mm[,c(which(refs), which(!refs))]
  
  # Drop first factor from patch and landscape columns if use_all_factor_levels = FALSE
  if(!use_all_factor_levels) {
    FAC_LEVS <- function(x) { sapply(x[,sapply(x, is.factor)], function(z) { length(levels(z)) })}
    fac_offs <- cumsum(c(1, FAC_LEVS(birds.dat)))
    fac_offs <- fac_offs[-length(fac_offs)]
    birds.cmp <- data.frame(birds.cmp[,-fac_offs])
  }
  
  # Drop rows with any missing values
  birds.cmp <- birds.cmp[complete.cases(birds.cmp),]
  print(summary(birds.cmp))
  
  Log.info("Building PCA model...")
  fitR <- prcomp(birds.cmp, center = FALSE, scale. = FALSE)
  fitH2O <- h2o.prcomp(birds.hex, k = k, transform = "STANDARDIZE", max_iterations = 1000, use_all_factor_levels = use_all_factor_levels)
  checkPCAModel(fitH2O, fitR, tolerance = 1e-5, sort_rows = FALSE)
  
  
}

doTest("PUBDEV-1654: PCA handling of Missing Values", test.pubdev.1654)
