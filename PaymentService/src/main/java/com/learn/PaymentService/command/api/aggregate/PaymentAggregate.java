package com.learn.PaymentService.command.api.aggregate;

import com.learn.CommonService.commands.CancelPaymentCommand;
import com.learn.CommonService.commands.ValidatePaymentCommand;
import com.learn.CommonService.events.PaymentCancelledEvent;
import com.learn.CommonService.events.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
@Slf4j
public class PaymentAggregate {
    @AggregateIdentifier
    private String paymentId;
    private String orderId;
    private String paymentStatus;

    public PaymentAggregate(){

    }

    @CommandHandler
    public PaymentAggregate(ValidatePaymentCommand command){
        //Validate the payment details
        //Publish the Payment Processed Event
        log.info("Executing ValidatePaymentCommand for Order Id : {} and Payment Id : {}",
                        command.getOrderId(), command.getPaymentId());

        PaymentProcessedEvent paymentProcessedEvent
                = new PaymentProcessedEvent(command.getPaymentId(), command.getOrderId());

        AggregateLifecycle.apply(paymentProcessedEvent);
        log.info("PaymentProcessedEvent Applied");
    }

    @EventSourcingHandler
    public void on(PaymentProcessedEvent event){
        this.paymentId = event.getPaymentId();
        this.orderId = event.getOrderId();
    }

    @CommandHandler
    public void handle(CancelPaymentCommand command){
        PaymentCancelledEvent paymentCancelledEvent = new PaymentCancelledEvent();
        BeanUtils.copyProperties(command, paymentCancelledEvent);
        AggregateLifecycle.apply(paymentCancelledEvent);
    }

    @EventSourcingHandler
    public void on(PaymentCancelledEvent event){
        this.paymentStatus = event.getPaymentStatus();
    }

}
