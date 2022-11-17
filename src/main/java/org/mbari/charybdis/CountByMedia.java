package org.mbari.charybdis;

import org.mbari.vars.services.model.AnnotationCount;
import org.mbari.vars.services.model.Media;

import java.util.List;

public record CountByMedia(long count, List<AnnotationCount> annotationCounts) {}
