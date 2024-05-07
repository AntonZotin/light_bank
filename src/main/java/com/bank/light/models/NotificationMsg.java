package com.bank.light.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record NotificationMsg(@JsonProperty("paymentAccount") String paymentAccount,
                              @JsonProperty("message") String message) implements Serializable {
}
