package org.mbari.charybdis.domain;

import org.mbari.vars.annosaurus.sdk.r1.models.AnnotationCount;

import java.util.List;

public record CountByMedia(long count, List<AnnotationCount> annotationCounts) {}
