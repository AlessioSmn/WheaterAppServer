package it.unipi.lsmsd.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    // TODO
    //  Put here all sort of analytics stuff, i say the most, the better

    // TODO
    //  Surely the aggregations must be done here, a note on them:
    //      Java driver are available only on MongoCollection entities, so we have
    //      to make a service that works on them instead of using the normal repositories

    // TODO
    //  Then maybe the most basic queries can be directly done calling repository methods (like find most ...)
}
