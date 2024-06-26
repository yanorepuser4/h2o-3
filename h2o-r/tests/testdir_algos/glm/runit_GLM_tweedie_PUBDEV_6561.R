setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source("../../../scripts/h2o-r-test-setup.R")
##
# In this test, we are going to generate real tweedie dataset, run H2O GLM and R GLM and compare the results for
# accuracy and others
##

test_glm_tweedies <- function() {
  if (requireNamespace("tweedie")) {
    num_rows <- 10000
    num_cols <- 5
    f1 <- random_dataset_real_only(num_rows, num_cols) # generate dataset containing the predictors.
    f1R <- as.data.frame(h2o.abs(f1))
    weights <- c(0.1, 0.2, 0.3, 0.4, 0.5, 1) # weights to generate the mean
    mu <- generate_mean(f1R, num_rows, num_cols, weights)
    pow <- c(1.1, 1.2,1.5,1.8, 2,1,1.54) # variance power range
    phi <- c(1, 2,3,4,5,558.6085,764.37)   # dispersion factor range
    y <- "resp"      # response column
    x <- c("abs.C1.", "abs.C2.", "abs.C3.", "abs.C4.", "abs.C5.")
    for (ind in c(1:length(pow))) { # generate dataset with each variance power and dispersion factor
      trainF <- generate_dataset(f1R, num_rows, num_cols, pow[ind], phi[ind], mu)
      print(paste("Compare H2O, R GLM model coefficients and standard error for var_power=", pow[ind], "link_power=",1-pow[ind], sep=" "))
      compareH2ORGLM(pow[ind], 1-pow[ind], x, y, trainF, as.data.frame(trainF), phi[ind])
    }
  } else {
    print("test_glm_tweedies is skipped.  Need to install tweedie package.")
  }
}

generate_dataset<-function(f1R, numRows, numCols, pow, phi, mu) {
  # resp <- c(1:numRows)
  # for (rowIndex in c(1:numRows)) {
  #   resp[rowIndex] <- tweedie::rtweedie(1,xi=pow,mu[rowIndex], phi, power=pow)
  # }
  resp <- tweedie::rtweedie(numRows, xi=pow, mu, phi, power=pow)
  f1h2o <- as.h2o(f1R)
  resph2o <- as.h2o(as.data.frame(resp))
  finalFrame <- h2o.cbind(f1h2o, resph2o)
  return(finalFrame)
}

generate_mean<-function(f1R, numRows, numCols, weights) {
  y <- c(1:numRows)
  for (rowIndex in c(1:numRows)) {
    tempResp = 0.0
    for (colIndex in c(1:numCols)) {
      tempResp = tempResp+weights[colIndex]*f1R[rowIndex, colIndex]
    }
    y[rowIndex] = tempResp
  }
  return(y)
}

compareH2ORGLM <-
  function(vpower,
           lpower,
           x,
           y,
           hdf,
           df,
           truedisp,
           tolerance = 2e-4) {
    print("Define formula for R")
    formula <- (df[, "resp"] ~ .)
    rmodel <- glm(
      formula = formula,
      data = df[, x],
      family = tweedie(var.power = vpower, link.power =
                         lpower),
      na.action = na.omit
    )
    h2omodel <-
      h2o.glm(
        x = x,
        y = y,
        training_frame = hdf,
        family = "tweedie",
        link = "tweedie",
        tweedie_variance_power = vpower,
        tweedie_link_power = lpower,
        alpha = 0.5,
        lambda = 0,
        nfolds = 0,
        compute_p_values = TRUE
      )
    print("Comparing H2O and R GLM model coefficients....")
    compareCoeffs(rmodel, h2omodel, tolerance, x)
    print("Comparing H2O and R GLM model dispersion estimations.  H2O performs better ....")
    print(paste(
      "H2O model dispersion estimate",
      h2omodel@model$dispersion,
      sep = ":"
    ))
    print(paste(
      "R model dispersion estimate",
      summary(rmodel)$dispersion,
      sep = ":"
    ))
    h2oDiff = abs(h2omodel@model$dispersion - truedisp)
    rDiff = abs(summary(rmodel)$dispersion - truedisp)
    if (rDiff < h2oDiff) {
      val = (h2oDiff - rDiff)/truedisp
      expect_true(val < 2e-3, info = "H2O performance degradation is too high.")
    } else {
      expect_true(
        h2oDiff <= rDiff,
        info = paste(
          "H2O dispersion estimation error",
          h2oDiff,
          "R dispersion estimation error",
          rDiff,
          sep = ":"
        )
      )
    }
  }

compareCoeffs <- function(rmodel, h2omodel, tolerance, x) {
  print("H2O GLM model....")
  print(h2omodel)
  print("R GLM model....")
  print(summary(rmodel))
  h2oCoeff <- h2omodel@model$coefficients
  rCoeff <- coef(rmodel)
  for (ind in c(1:length(x))) {
    expect_true(abs(h2oCoeff[x[ind]]-rCoeff[x[ind]]) < tolerance, info = paste0(
      "R coefficient: ",
      rCoeff[x[ind]],
      " but h2o Coefficient: ",
      h2oCoeff[x[ind]],
      sep = " "
    ))
  }
  expect_true(abs(h2oCoeff[x[ind]]-rCoeff[x[ind]]) < tolerance, info = paste0(
    "R coefficient: ",
    rCoeff["(Intercept)"],
    " but h2o Coefficient: ",
    h2oCoeff["(Intercept)"],
    sep = " "
  ))
}

doTest("Comparison of H2O to R TWEEDIE family coefficients and disperson with tweedie dataset", test_glm_tweedies)
