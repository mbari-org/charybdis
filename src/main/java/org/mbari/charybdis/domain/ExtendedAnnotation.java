package org.mbari.charybdis.domain;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;

import java.net.URI;

/**
 * Adds media parameters to an annotation
 */
public class ExtendedAnnotation extends Annotation {
    private String videoSequenceName;
    private URI uri;

    public ExtendedAnnotation(Annotation a, Media m) {
        super(a);
        this.videoSequenceName = m.getVideoSequenceName();
        this.uri = m.getUri();
    }

    public String getVideoSequenceName() {
        return videoSequenceName;
    }

    public void setVideoSequenceName(String videoSequenceName) {
        this.videoSequenceName = videoSequenceName;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }
}
