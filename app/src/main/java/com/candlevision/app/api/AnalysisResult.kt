package com.candlevision.app.api

/** Parsed, structured output returned by the Claude Vision analysis. */
data class AnalysisResult(
    val direction: String,
    val trend: String,
    val risk: String,
    val analysis: String,
    val zones: String,
    val scenario: String,
    val advice: String,
)
