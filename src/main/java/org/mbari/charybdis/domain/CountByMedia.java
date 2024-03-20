package org.mbari.charybdis.domain;

import org.mbari.vars.services.model.AnnotationCount;

import java.util.List;

public record CountByMedia(long count, List<AnnotationCount> annotationCounts) {}
