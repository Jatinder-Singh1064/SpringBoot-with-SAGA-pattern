package com.learn.OrderService.command.api.saga;

import com.learn.CommonService.commands.*;
import com.learn.CommonService.events.*;
import com.learn.CommonService.model.User;
import com.learn.CommonService.queries.GetUserPaymentDetailsQuery;
import com.learn.OrderService.command.api.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

@Saga
@Slf4j
public class OrderProcessingSaga {

    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;

    public OrderProcessingSaga(){

    }

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent event){
        log.info("OrderCreatedEvent in Saga for Order Id : {}" + event.getOrderId());

        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery
                = new GetUserPaymentDetailsQuery(event.getUserId());

        User user = null;

        try {
            user = queryGateway.query(
                    getUserPaymentDetailsQuery,
                    ResponseTypes.instanceOf(User.class)
            ).join();
        }
        catch(Exception ex){
            log.error(ex.getMessage());
            //Start the Compensating transaction
            cancelOrderCommand(event.getOrderId());
        }

        ValidatePaymentCommand validatePaymentCommand =
                ValidatePaymentCommand.builder()
                        .cardDetails(user.getCardDetails())
                        .orderId(event.getOrderId())
                        .paymentId(UUID.randomUUID().toString())
                        .build();
        commandGateway.sendAndWait(validatePaymentCommand);
    }

    private void cancelOrderCommand(String orderId) {
        CancelOrderCommand cancelOrderCommand = new CancelOrderCommand(orderId);
        commandGateway.sendAndWait(cancelOrderCommand);
    }


    @SagaEventHandler(associationProperty = "orderId")
    private void handle(PaymentProcessedEvent event){
        log.info("PaymentProcessedEvent in Saga for Order Id : {}" + event.getOrderId());
        try{
            //to mimic the negative scenario - where shipment is erroneous - then cancel the payment
//            if(true)
//                throw new Exception();

            ShipOrderCommand shipOrderCommand =
                    ShipOrderCommand.builder()
                            .shipmentId(UUID.randomUUID().toString())
                            .orderId(event.getOrderId())
                            .build();

            commandGateway.sendAndWait(shipOrderCommand);
        }
        catch(Exception e){
            log.error(e.getMessage());
            //Start the compensating transaction
            cancelPaymentCommand(event);
        }
    }

    private void cancelPaymentCommand(PaymentProcessedEvent event) {
        CancelPaymentCommand cancelPaymentCommand = new CancelPaymentCommand(event.getPaymentId(), event.getOrderId());
        commandGateway.sendAndWait(cancelPaymentCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderShippedEvent event){
        log.info("OrderShippedEvent in Saga for Order Id : {}" + event.getOrderId());

        try{
            CompleteOrderCommand completeOrderCommand =
                    CompleteOrderCommand.builder()
                            .orderId(event.getOrderId())
                            .orderStatus("APPROVED")
                            .build();

            commandGateway.sendAndWait(completeOrderCommand);
        }
        catch(Exception e){
            log.error(e.getMessage());
            //Start the compensating transaction
        }
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCompletedEvent event){
        log.info("OrderCompletedEvent in Saga for Order Id : {}" + event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCancelledEvent event){
        log.info("OrderCancelledEvent in Saga for Order Id : {}" + event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentCancelledEvent event){
        log.info("PaymentCancelledEvent in Saga for Order Id : {}" + event.getOrderId());

        cancelOrderCommand(event.getOrderId());
    }
}
