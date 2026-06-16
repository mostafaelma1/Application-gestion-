package com.candlevision.app.api

/** The seven result sections produced by the on-device chart analysis. */
data class AnalysisResult(
    val direction: String,
    val trend: String,
    val risk: String,
    val analysis: String,
    val zones: String,
    val scenario: String,
    val advice: String,
)
