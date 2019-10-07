package org.mbari.charybdis.services;

import io.helidon.webserver.Routing.Rules;
import io.helidon.common.http.MediaType;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import org.mbari.charybdis.DataGroup;

import java.util.logging.Logger;

/**
 * Kakani2019Nature
 */
public class Kakani2019Nature implements Service {

  private final AnnosaurusUtil annosaurusUtil;
  private final VampireSquidUtil vampireSquidUtil;
  private final Logger log = Logger.getLogger(getClass().getName());

  public Kakani2019Nature(AnnosaurusUtil annosaurusUtil, VampireSquidUtil vampireSquidUtil) {
    this.annosaurusUtil = annosaurusUtil;
    this.vampireSquidUtil = vampireSquidUtil;
  }

  @Override
  public void update(Rules rules) {
    rules.get("/", this::defaultHandler);
  }

  private void defaultHandler(ServerRequest request, ServerResponse response) {
    // Get annotations, then get media, then package them together
    response.headers().contentType(MediaType.APPLICATION_JSON);
    annosaurusUtil.findByLinkNameAndLinkValue("comment", "Nature20190609559")
        .thenApply(as -> vampireSquidUtil.findMediaForAnnotations(as)
                .thenApply(ms -> new DataGroup(as, ms))
                .thenApply(obj -> annosaurusUtil.getGson().toJson(obj))
                .thenAccept(response::send));

  }



}