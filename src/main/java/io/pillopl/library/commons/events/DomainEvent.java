package io.pillopl.library.commons.events;

import java.time.Instant;
import java.util.UUID;

/**
 * 领域事件
 */
public interface DomainEvent {

    UUID getEventId();

    UUID getAggregateId();

    // 事件发生时间
    Instant getWhen();
}
