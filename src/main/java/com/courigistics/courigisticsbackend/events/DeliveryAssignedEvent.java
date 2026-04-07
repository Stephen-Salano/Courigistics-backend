package com.courigistics.courigisticsbackend.events;

import com.courigistics.courigisticsbackend.entities.Courier;
import com.courigistics.courigisticsbackend.entities.Delivery;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

/**
 * Event published when a courier is successfully assigned to a delivery.
 */
@Getter
public class DeliveryAssignedEvent extends ApplicationEvent implements Serializable {
    private final transient Delivery delivery;
    private final transient Courier courier;

    public DeliveryAssignedEvent(Object source, Delivery delivery, Courier courier) {
        super(source);
        this.delivery = delivery;
        this.courier = courier;
    }
}
