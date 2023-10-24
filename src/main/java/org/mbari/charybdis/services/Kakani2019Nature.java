package org.mbari.charybdis.services;



import org.mbari.charybdis.DataGroup;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;


/**
 * Kakani2019Nature
 */
public class Kakani2019Nature implements HttpService {

  private final Annosaurus annosaurus;
  private final VampireSquid vampireSquid;

  public Kakani2019Nature(Annosaurus annosaurus, VampireSquid vampireSquid) {
    this.annosaurus = annosaurus;
    this.vampireSquid = vampireSquid;
  }

  @Override
  public void routing(HttpRules rules) {
    rules.get("/", this::defaultHandler);
  }

  private void defaultHandler(ServerRequest request, ServerResponse response) {
    // Get annotations, then get media, then package them together
    response.headers().contentType(MediaTypes.APPLICATION_JSON);
    var result = annosaurus.findByLinkNameAndLinkValue("comment", "Nature20190609559")
        .thenCompose(as -> vampireSquid.findMediaForAnnotations(as)
                .thenApply(ms -> new DataGroup(as, ms))
                .thenApply(obj -> annosaurus.getGson().toJson(obj))
        ).join();
    response.send(result);

  }



}