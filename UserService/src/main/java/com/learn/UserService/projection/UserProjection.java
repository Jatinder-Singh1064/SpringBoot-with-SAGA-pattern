package com.learn.UserService.projection;

import com.learn.CommonService.model.CardDetails;
import com.learn.CommonService.model.User;
import com.learn.CommonService.queries.GetUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserProjection {

    @QueryHandler
    public User getUserPaymentDetails(GetUserPaymentDetailsQuery query){
        //Ideally get the details from the DB
        CardDetails cardDetails
                = CardDetails
                .builder()
                .name("Jatinder Singh")
                .validUntilYear(2023)
                .validUntilMonth(01)
                .cardNumber("123456789")
                .cvv(111)
                .build();

        return User.builder()
                .userId(query.getUserId())
                .firstName("Jatinder")
                .lastName("Singh")
                .cardDetails(cardDetails)
                .build();
    }
}
