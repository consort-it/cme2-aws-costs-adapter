package de.consortit.application;

import de.consortit.application.actuator.ActuatorRouteController;
import de.consortit.application.cost.CostController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwsCostAdapter {

    public static void main(String[] args) {

        log.info("AWS cost adapter started!");

        // aws costs
        CostController costController = new CostController();
        costController.initRoutes();

        // actuator
        ActuatorRouteController routeController = new ActuatorRouteController();
        routeController.initRoutes();
    }
}
