package eu.meterpoint.producer.domain.outbox;

import eu.meterpoint.producer.domain.reading.Reading;
import eu.meterpoint.producer.domain.session.Session;

public class OutboxEventFactory {

    public OutboxEvent sessionStarted(Session session) {
        // TODO
        return null;
    }

    public OutboxEvent readingRecorded(Reading reading) {
        // TODO
        return null;
    }

    public OutboxEvent sessionStopped(Session session) {
        // TODO
        return null;
    }
}