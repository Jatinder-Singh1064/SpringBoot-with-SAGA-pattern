package com.learn.ShipmentService.command.api.aggregate;

import com.learn.CommonService.commands.ShipOrderCommand;
import com.learn.CommonService.events.OrderShippedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.hibernate.criterion.Order;

@Aggregate
public class ShipmentAggregate {

    @AggregateIdentifier
    private String shipmentId;
    private String orderId;
    private String shipmentStatus;

    public ShipmentAggregate(){

    }

    @CommandHandler
    public ShipmentAggregate(ShipOrderCommand command){
        //Validate the command
        //Publish the Order Shipment Event
        OrderShippedEvent orderShippedEvent =
                OrderShippedEvent.builder()
                        .shipmentId(command.getShipmentId())
                        .orderId(command.getOrderId())
                        .shipmentStatus("COMPLETED")
                        .build();

        AggregateLifecycle.apply(orderShippedEvent);
    }

    @EventSourcingHandler
    public void on(OrderShippedEvent event){
        this.orderId = event.getOrderId();
        this.shipmentId = event.getShipmentId();
        this.shipmentStatus = event.getShipmentStatus();
    }
}
