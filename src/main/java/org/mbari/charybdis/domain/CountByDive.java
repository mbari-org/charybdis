package org.mbari.charybdis.domain;

import org.mbari.vars.services.model.AnnotationCount;

import java.util.List;

public record CountByDive(Long count, String videoSequenceName, List<AnnotationCount> annotationCounts) {}
